package info.jallaix.message.dao;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.message.config.DomainDaoTestConfiguration;
import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import info.jallaix.spring.data.es.test.SpringDataEsTestConfiguration;
import info.jallaix.spring.data.es.test.fixture.ElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.testcase.BaseDaoElasticsearchTestCase;
import info.jallaix.spring.data.es.test.testcase.DaoTestedMethod;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertArrayEquals;

/**
 * The Domain DAO must verify some tests provided by {@link BaseDaoElasticsearchTestCase}.
 */
@Configuration
@Import({SpringDataEsTestConfiguration.class, DomainDaoTestConfiguration.class})
@EnableElasticsearchRepositories(basePackageClasses = DomainDao.class)
@EnableAspectJAutoProxy
@ContextConfiguration(classes = DomainDaoFindAllPageableTest.class)
public class DomainDaoFindAllPageableTest extends BaseDaoElasticsearchTestCase<Domain, String, DomainDao> {

    /**
     * Spring class rule
     */
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    /**
     * Spring method rule
     */
    @Rule
    public final SpringMethodRule SPRING_METHOD_RULE = new SpringMethodRule();

    /**
     * I18n domain
     */
    @Autowired
    private DomainHolder i18nDomainHolder;

    /**
     * Elasticsearch operations
     */
    @Autowired
    private ElasticsearchOperations esOperations;

    /**
     * Serialization framework
     */
    @Autowired
    private Kryo kryo;

    /**
     * Locale data holder
     */
    @Autowired
    private ThreadLocaleHolder threadLocaleHolder;

    /**
     * Domain testing checks
     */
    private DomainDaoChecker domainDaoChecker;


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Tests lifecycle                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Constructor defining the "find all by page" tests
     */
    public DomainDaoFindAllPageableTest() {
        super(DaoTestedMethod.FindAllPageable.class);
    }

    /**
     * Initialize custom testing objects.
     */
    @Before
    public void initTest() {


        // Utility object that performs DAO checks
        domainDaoChecker = new DomainDaoChecker(i18nDomainHolder, esOperations, kryo);

        // Domain customizer for default DAO tests
        setCustomizer(new DomainDaoTestsCustomizer(domainDaoChecker, threadLocaleHolder, getTestFixture(), kryo));
    }

    /**
     * Clear locale data
     */
    @After
    public void clearLocales() {
        threadLocaleHolder.clear();
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                     Test fixture                                               */
    /*----------------------------------------------------------------------------------------------------------------*/

    @Override
    protected ElasticsearchTestFixture<Domain> getTestFixture() {
        return new DomainTestFixture();
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                     Custom tests                                               */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Finding a list of all existing domains by page returns an iterable with all these domains.
     * Their descriptions are localized for the specified supported language.
     */
    @Test
    public void findAllPageableWithExistingSupportedLanguage() {
        assertFindAllPageable("fr;q=1", DomainDaoTestUtils.getFrenchDescriptions());
    }

    /**
     * Finding a sorted list of all existing domains by page returns an iterable with all these domains.
     * Their descriptions are localized for the specified supported language.
     */
    @Test
    public void findAllPageableSortedWithExistingSupportedLanguage() {
        assertFindAllPageableSorted("fr;q=1", DomainDaoTestUtils.getFrenchDescriptions());
    }

    /**
     * Finding a list of all existing domains by page for a missing supported language returns an iterable with all these domains.
     * Their descriptions are localized for the default I18N domain's language.
     */
    @Test
    public void findAllPageableExistingDomainWithMissingSupportedLanguage() {
        assertFindAllPageable("es");
    }

    /**
     * Finding a sorted list of all existing domains by page for a missing supported language returns an iterable with all these domains.
     * Their descriptions are localized for the default I18N domain's language.
     */
    @Test
    public void findAllPageableSortedExistingDomainWithMissingSupportedLanguage() {
        assertFindAllPageableSorted("es");
    }

    /**
     * Finding a list of all existing domains by page for a missing complex language (with existing messages for the simple language) returns an iterable with all these domains.
     * Their descriptions are localized for the linked simple language.
     */
    @Test
    public void findAllPageableExistingDomainWithMissingComplexLanguageButExistingSimpleLanguage() {
        assertFindAllPageable("fr;q=0.5,fr-BE;q=1,en;q=0.1", DomainDaoTestUtils.getFrenchDescriptions());
    }

    /**
     * Finding a sorted list of all existing domains by page for a missing complex language (with existing messages for the simple language) returns an iterable with all these domains.
     * Their descriptions are localized for the linked simple language.
     */
    @Test
    public void findAllPageableSortedExistingDomainWithMissingComplexLanguageButExistingSimpleLanguage() {
        assertFindAllPageableSorted("fr;q=0.5,fr-BE;q=1,en;q=0.1", DomainDaoTestUtils.getFrenchDescriptions());
    }

    /**
     * Finding a list of all existing domains by page for an existing complex language returns an iterable with all these domains.
     * Their descriptions are localized for the complex language if available, else the linked simple language is used.
     */
    @Test
    public void findAllPageableExistingDomainWithExistingComplexLanguage() {
        assertFindAllPageable("en;q=0.5,en-US;q=1", DomainDaoTestUtils.getEnglishUsDescriptions());
    }

    /**
     * Finding a list of all existing domains by page for an existing complex language returns an iterable with all these domains.
     * Their descriptions are localized for the complex language if available, else the linked simple language is used.
     */
    @Test
    public void findAllPageableSortedExistingDomainWithExistingComplexLanguage() {
        assertFindAllPageableSorted("en;q=0.5,en-US;q=1", DomainDaoTestUtils.getEnglishUsDescriptions());
    }

    /**
     * Finding a list of all existing domains by page for a missing complex language (without any existing message for the simple language) returns an iterable with all these domains.
     * Their descriptions are localized for the default I18N domain's language.
     */
    @Test
    public void findAllPageableExistingDomainWithMissingComplexAndSimpleLanguage() {
        assertFindAllPageable("es-ES,es");
    }

    /**
     * Finding a sorted list of all existing domains by page for a missing complex language (without any existing message for the simple language) returns an iterable with all these domains.
     * Their descriptions are localized for the default I18N domain's language.
     */
    @Test
    public void findAllPageableSortedExistingDomainWithMissingComplexAndSimpleLanguage() {
        assertFindAllPageableSorted("es-ES,es");
    }

    /**
     * Finding a list of all existing domains by page for an unsupported language returns an iterable with all these domains.
     * Their descriptions are localized for the default I18N domain's language.
     */
    @Test
    public void findAllPageableExistingDomainWithUnsupportedLanguage() {
        assertFindAllPageable("de-DE,de");
    }

    /**
     * Finding a sorted list of all existing domains by page for an unsupported language returns an iterable with all these domains.
     * Their descriptions are localized for the default I18N domain's language.
     */
    @Test
    public void findAllPageableSortedExistingDomainWithUnsupportedLanguage() {
        assertFindAllPageableSorted("de-DE");
    }


    /**
     * Assert that all domains by page found match the expected ones.
     * The domain descriptions are returned in a localized language depending on the provided language tag.
     * The expected domain descriptions are localized for the default I18N language.
     *
     * @param languageTag The language tag that involves a localized description
     */
    private void assertFindAllPageable(String languageTag) {
        assertFindAllPageable(languageTag, DomainDaoTestUtils.getEnglishDescriptions());
    }

    /**
     * Assert that all domains by page found match the expected ones.
     * The domain descriptions must be returned in a localized language depending on the provided language tag.
     *
     * @param languageTag         The language tag that involves a localized description
     * @param descriptionsFixture The localized descriptions matching the provided language tag
     */
    private void assertFindAllPageable(String languageTag, Map<String, String> descriptionsFixture) {

        // Define the page parameters
        long documentsCount = testClientOperations.countDocuments(getDocumentMetadata());
        org.springframework.util.Assert.isTrue(documentsCount > 0, "No document loaded");
        int pageSize = getTestFixture().getPageSize();
        org.springframework.util.Assert.isTrue(pageSize > 0, "Page size must be positive");
        int nbPages = (int) documentsCount / pageSize + (documentsCount % pageSize == 0 ? 0 : 1);

        // Get typed documents from the index for the first page
        // Get all domains from the index with specified descriptions
        List<Domain> initialList = domainDaoChecker.internationalizeDomains(
                testClientOperations.findAllDocumentsPaged(
                        getDocumentMetadata(),
                        0,
                        getTestFixture().getPageSize()),
                descriptionsFixture);

        // Repository search for the specified language
        threadLocaleHolder.setOutputLocales(Locale.LanguageRange.parse(languageTag));
        List<Domain> foundList =
                StreamSupport.stream(
                        getRepository().findAll(new PageRequest(0, pageSize)).spliterator(), false)
                        .collect(Collectors.toList());

        assertArrayEquals(initialList.toArray(), foundList.toArray());

        // Get typed documents from the index for the last page
        initialList = domainDaoChecker.internationalizeDomains(
                testClientOperations.findAllDocumentsPaged(
                        getDocumentMetadata(),
                        nbPages - 1,
                        getTestFixture().getPageSize()),
                descriptionsFixture);

        // Repository search for the specified language
        foundList.clear();
        foundList =
                StreamSupport.stream(
                        getRepository().findAll(new PageRequest(nbPages - 1, pageSize)).spliterator(), false)
                        .collect(Collectors.toList());

        assertArrayEquals(initialList.toArray(), foundList.toArray());
    }

    /**
     * Assert that all sorted domains by page found match the expected ones.
     * The domain descriptions are returned in a localized language depending on the provided language tag.
     * The expected domain descriptions are localized for the default I18N language.
     *
     * @param languageTag The language tag that involves a localized description
     */
    private void assertFindAllPageableSorted(String languageTag) {
        assertFindAllPageableSorted(languageTag, DomainDaoTestUtils.getEnglishDescriptions());
    }

    /**
     * Assert that all sorted domains by page found match the expected ones.
     * The domain descriptions must be returned in a localized language depending on the provided language tag.
     *
     * @param languageTag         The language tag that involves a localized description
     * @param descriptionsFixture The localized descriptions matching the provided language tag
     */
    private void assertFindAllPageableSorted(String languageTag, Map<String, String> descriptionsFixture) {

        // Define the page parameters
        long documentsCount = testClientOperations.countDocuments(getDocumentMetadata());
        org.springframework.util.Assert.isTrue(documentsCount > 0, "No document loaded");
        int pageSize = getTestFixture().getPageSize();
        org.springframework.util.Assert.isTrue(pageSize > 0, "Page size must be positive");
        int nbPages = (int) documentsCount / pageSize + (documentsCount % pageSize == 0 ? 0 : 1);

        // Get typed documents from the index for the first page
        // Get all domains from the index with specified descriptions
        List<Domain> initialList = domainDaoChecker.internationalizeDomains(
                testClientOperations.findAllDocumentsPagedSorted(
                        getDocumentMetadata(),
                        getTestFixture().getSortField(),
                        0,
                        getTestFixture().getPageSize()),
                descriptionsFixture);

        // Repository search for the specified language
        threadLocaleHolder.setOutputLocales(Locale.LanguageRange.parse(languageTag));
        Sort sorting = new Sort(Sort.Direction.DESC, getTestFixture().getSortField().getName());
        List<Domain> foundList =
                StreamSupport.stream(
                        getRepository().findAll(new PageRequest(0, pageSize, sorting)).spliterator(), false)
                        .collect(Collectors.toList());

        assertArrayEquals(initialList.toArray(), foundList.toArray());

        // Get typed documents from the index for the last page
        initialList = domainDaoChecker.internationalizeDomains(
                testClientOperations.findAllDocumentsPagedSorted(
                        getDocumentMetadata(),
                        getTestFixture().getSortField(),
                        nbPages - 1,
                        getTestFixture().getPageSize()),
                descriptionsFixture);

        // Repository search for the specified language
        foundList.clear();
        foundList =
                StreamSupport.stream(
                        getRepository().findAll(new PageRequest(nbPages - 1, pageSize, sorting)).spliterator(), false)
                        .collect(Collectors.toList());

        assertArrayEquals(initialList.toArray(), foundList.toArray());
    }
}
