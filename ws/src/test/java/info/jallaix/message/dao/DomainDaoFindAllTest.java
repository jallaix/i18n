package info.jallaix.message.dao;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.config.TestDomainDaoConfiguration;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import info.jallaix.spring.data.es.test.fixture.ElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.testcase.BaseDaoElasticsearchTestCase;
import info.jallaix.spring.data.es.test.testcase.DaoTestedMethod;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.Locale;
import java.util.Map;

/**
 * The Domain DAO must verify some tests provided by {@link BaseDaoElasticsearchTestCase}.
 */
@Configuration
@Import(TestDomainDaoConfiguration.class)
@EnableElasticsearchRepositories(basePackageClasses = DomainDao.class)
@EnableAspectJAutoProxy
@ContextConfiguration(classes = DomainDaoFindAllTest.class)
public class DomainDaoFindAllTest extends BaseDaoElasticsearchTestCase<Domain, String, DomainDao> {

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
     * Domain DAO customizer
     */
    private DomainDaoTestsCustomizer domainDaoTestsCustomizer;


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Tests lifecycle                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Constructor defining the "find all" tests
     */
    public DomainDaoFindAllTest() {
        super(DaoTestedMethod.FindAll.class, DaoTestedMethod.FindAllSorted.class);
    }

    /**
     * Initialize custom testing objects.
     */
    @Before
    public void initTest() {

        // Utility object that performs DAO checks
        DomainDaoChecker domainDaoChecker = new DomainDaoChecker(i18nDomainHolder, esOperations, kryo);

        // Domain customizer for DAO tests
        domainDaoTestsCustomizer = new DomainDaoTestsCustomizer(domainDaoChecker, threadLocaleHolder, getTestFixture(), kryo);
        setCustomizer(domainDaoTestsCustomizer);
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
     * Finding a list of all existing domains returns an iterable with all these domains.
     * Their descriptions are localized for the specified supported language.
     */
    @Test
    public void findAllWithExistingSupportedLanguage() {
        assertFindAll("fr;q=1", DomainDaoTestUtils.getFrenchDescriptions());
    }

    /**
     * Finding a sorted list of all existing domains returns an iterable with all these domains.
     * Their descriptions are localized for the specified supported language.
     */
    @Test
    public void findAllSortedWithExistingSupportedLanguage() {
        assertFindAllSorted("fr;q=1", DomainDaoTestUtils.getFrenchDescriptions());
    }

    /**
     * Finding a list of all existing domains for a missing supported language returns an iterable with all these domains.
     * Their descriptions are localized for the default I18N domain's language.
     */
    @Test
    public void findAllExistingDomainWithMissingSupportedLanguage() {
        assertFindAll("es");
    }

    /**
     * Finding a sorted list of all existing domains for a missing supported language returns an iterable with all these domains.
     * Their descriptions are localized for the default I18N domain's language.
     */
    @Test
    public void findAllSortedExistingDomainWithMissingSupportedLanguage() {
        assertFindAllSorted("es");
    }

    /**
     * Finding a list of all existing domains for a missing complex language (with existing messages for the simple language) returns an iterable with all these domains.
     * Their descriptions are localized for the linked simple language.
     */
    @Test
    public void findAllExistingDomainWithMissingComplexLanguageButExistingSimpleLanguage() {
        assertFindAll("fr;q=0.5,fr-BE;q=1,en;q=0.1", DomainDaoTestUtils.getFrenchDescriptions());
    }

    /**
     * Finding a sorted list of all existing domains for a missing complex language (with existing messages for the simple language) returns an iterable with all these domains.
     * Their descriptions are localized for the linked simple language.
     */
    @Test
    public void findAllSortedExistingDomainWithMissingComplexLanguageButExistingSimpleLanguage() {
        assertFindAllSorted("fr;q=0.5,fr-BE;q=1,en;q=0.1", DomainDaoTestUtils.getFrenchDescriptions());
    }

    /**
     * Finding a list of all existing domains for an existing complex language returns an iterable with all these domains.
     * Their descriptions are localized for the complex language if available, else the linked simple language is used.
     */
    @Test
    public void findAllExistingDomainWithExistingComplexLanguage() {
        assertFindAll("en;q=0.5,en-US;q=1", DomainDaoTestUtils.getEnglishUsDescriptions());
    }

    /**
     * Finding a sorted list of all existing domains for an existing complex language returns an iterable with all these domains.
     * Their descriptions are localized for the complex language if available, else the linked simple language is used.
     */
    @Test
    public void findAllSortedExistingDomainWithExistingComplexLanguage() {
        assertFindAllSorted("en;q=0.5,en-US;q=1", DomainDaoTestUtils.getEnglishUsDescriptions());
    }

    /**
     * Finding a list of all existing domains for a missing complex language (without any existing message for the simple language) returns an iterable with all these domains.
     * Their descriptions are localized for the default I18N domain's language.
     */
    @Test
    public void findAllExistingDomainWithMissingComplexAndSimpleLanguage() {
        assertFindAll("es-ES,es");
    }

    /**
     * Finding a sorted list of all existing domains for a missing complex language (without any existing message for the simple language) returns an iterable with all these domains.
     * Their descriptions are localized for the default I18N domain's language.
     */
    @Test
    public void findAllSortedExistingDomainWithMissingComplexAndSimpleLanguage() {
        assertFindAllSorted("es-ES,es");
    }

    /**
     * Finding a list of all existing domains for an unsupported language returns an iterable with all these domains.
     * Their descriptions are localized for the default I18N domain's language.
     */
    @Test
    public void findAllExistingDomainWithUnsupportedLanguage() {
        assertFindAll("de-DE,de");
    }

    /**
     * Finding a sorted list of all existing domains for an unsupported language returns an iterable with all these domains.
     * Their descriptions are localized for the default I18N domain's language.
     */
    @Test
    public void findAllSortedExistingDomainWithUnsupportedLanguage() {
        assertFindAllSorted("de-DE,de");
    }


    /**
     * Assert that all domains found match the expected ones.
     * The domain descriptions are returned in a localized language depending on the provided language tag.
     * The expected domain descriptions are localized for the default I18N language.
     *
     * @param languageTag The language tag that involves a localized description
     */
    private void assertFindAll(String languageTag) {
        assertFindAll(languageTag, DomainDaoTestUtils.getEnglishDescriptions());
    }

    /**
     * Assert that all domains found match the expected ones.
     * The domain descriptions must be returned in a localized language depending on the provided language tag.
     *
     * @param languageTag         The language tag that involves a localized description
     * @param descriptionsFixture The localized descriptions matching the provided language tag
     */
    private void assertFindAll(String languageTag, Map<String, String> descriptionsFixture) {

        domainDaoTestsCustomizer.setDescriptionsFixture(descriptionsFixture);
        threadLocaleHolder.setOutputLocales(Locale.LanguageRange.parse(languageTag));
        findAllDocuments();
    }

    /**
     * Assert that all domains found match the expected ones.
     * The domain descriptions are returned in a localized language depending on the provided language tag.
     * The expected domain descriptions are localized for the default I18N language.
     *
     * @param languageTag The language tag that involves a localized description
     */
    private void assertFindAllSorted(String languageTag) {
        assertFindAllSorted(languageTag, DomainDaoTestUtils.getEnglishDescriptions());
    }

    /**
     * Assert that all domains found match the expected ones.
     * The domain descriptions must be returned in a localized language depending on the provided language tag.
     *
     * @param languageTag         The language tag that involves a localized description
     * @param descriptionsFixture The localized descriptions matching the provided language tag
     */
    private void assertFindAllSorted(String languageTag, Map<String, String> descriptionsFixture) {

        domainDaoTestsCustomizer.setDescriptionsFixture(descriptionsFixture);
        threadLocaleHolder.setOutputLocales(Locale.LanguageRange.parse(languageTag));
        findAllDocumentsSorted();
    }
}
