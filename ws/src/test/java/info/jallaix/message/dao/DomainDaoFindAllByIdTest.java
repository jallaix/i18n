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
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.HashMap;
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
@ContextConfiguration(classes = DomainDaoFindAllByIdTest.class)
public class DomainDaoFindAllByIdTest extends BaseDaoElasticsearchTestCase<Domain, String, DomainDao> {

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
     * Constructor defining the "find all by identifier" tests
     */
    public DomainDaoFindAllByIdTest() {
        super(DaoTestedMethod.FindAllById.class);
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
     * Finding a list of existing documents by identifier returns an iterable with all these documents.
     * Their descriptions are localized for the specified supported language.
     */
    @Test
    public void findAllByIdWithExistingSupportedLanguage() {

        // Expected descriptions for all domains
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("1", DomainTestFixture.DOMAIN1_FR_DESCRIPTION);
        descriptions.put("2", DomainTestFixture.DOMAIN2_FR_DESCRIPTION);
        descriptions.put("3", DomainTestFixture.DOMAIN3_FR_DESCRIPTION);
        descriptions.put("4", DomainTestFixture.DOMAIN4_FR_DESCRIPTION);

        assertFindAllById("fr", descriptions);
    }

    /**
     * Finding a list of existing documents for a missing supported language returns an iterable with all these documents.
     * Their descriptions are localized for the default I18N domain's language.
     */
    @Test
    public void findAllByIdExistingDomainWithMissingSupportedLanguage() {
        assertFindAllById("es");
    }

    /**
     * Finding a list of existing documents by identifier for a missing complex language (with existing messages for the simple language) returns an iterable with all these documents.
     * Their descriptions are localized for the linked simple language.
     */
    @Test
    public void findAllByIdExistingDomainWithMissingComplexLanguageButExistingSimpleLanguage() {

        // Expected descriptions for all domains
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("1", DomainTestFixture.DOMAIN1_FR_DESCRIPTION);
        descriptions.put("2", DomainTestFixture.DOMAIN2_FR_DESCRIPTION);
        descriptions.put("3", DomainTestFixture.DOMAIN3_FR_DESCRIPTION);
        descriptions.put("4", DomainTestFixture.DOMAIN4_FR_DESCRIPTION);

        assertFindAllById("fr-BE", descriptions);
    }

    /**
     * Finding a list of existing documents by identifier for an existing complex language returns an iterable with all these documents.
     * Their descriptions are localized for the complex language if available, else the linked simple language is used.
     */
    @Test
    public void findAllByIdExistingDomainWithExistingComplexLanguage() {

        // Expected descriptions for all domains
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("1", DomainTestFixture.DOMAIN1_EN_US_DESCRIPTION);
        descriptions.put("2", DomainTestFixture.DOMAIN2_EN_DESCRIPTION);
        descriptions.put("3", DomainTestFixture.DOMAIN3_EN_US_DESCRIPTION);
        descriptions.put("4", DomainTestFixture.DOMAIN4_EN_DESCRIPTION);

        assertFindAllById("en-US", descriptions);
    }

    /**
     * Finding an existing domain for a missing complex language (without an existing message for the simple language) returns this domain.
     * Its description is localized for the default I18N domain's language.
     */
    @Test
    public void findAllByIdExistingDomainWithMissingComplexAndSimpleLanguage() {
        assertFindAllById("es-ES");
    }

    /**
     * Finding an existing domain for an unsupported language returns this domain.
     * Its description is localized for the default I18N domain's language.
     */
    @Test
    public void findAllByIdExistingDomainWithUnsupportedLanguage() {
        assertFindAllById("de-DE");
    }


    /**
     * Assert that domains found by identifier match the expected ones.
     * The domain descriptions are returned in a localized language depending on the provided language tag.
     * The expected domain descriptions are localized for the default I18N language.
     *
     * @param languageTag The language tag that involves a localized description
     */
    private void assertFindAllById(String languageTag) {

        // Expected descriptions for all domains
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("1", DomainTestFixture.DOMAIN1_EN_DESCRIPTION);
        descriptions.put("2", DomainTestFixture.DOMAIN2_EN_DESCRIPTION);
        descriptions.put("3", DomainTestFixture.DOMAIN3_EN_DESCRIPTION);
        descriptions.put("4", DomainTestFixture.DOMAIN4_EN_DESCRIPTION);

        assertFindAllById(languageTag, descriptions);
    }

    /**
     * Assert that domains found by identifier match the expected ones.
     * The domain descriptions are returned in a localized language depending on the provided language tag.
     *
     * @param languageTag         The language tag that involves a localized description
     * @param descriptionsFixture The localized description matching the provided language tag
     */
    private void assertFindAllById(String languageTag, Map<String, String> descriptionsFixture) {

        // Get all domain identifiers from the index with specified descriptions
        final List<Domain> initialList = domainDaoChecker.internationalizeDomains(
                testClientOperations.findAllDocumentsPaged(
                        getDocumentMetadata(),
                        0,
                        (int) this.getTestDocumentsLoader().getLoadedDocumentCount()),
                descriptionsFixture);

        // Repository search for the specified language
        threadLocaleHolder.setOutputLocale(Locale.forLanguageTag(languageTag));
        final List<String> initialKeys = initialList.stream()
                .map(this::getIdFieldValue)
                .collect(Collectors.toList());
        final List<Domain> foundList =
                StreamSupport.stream(
                        getRepository().findAll(initialKeys).spliterator(), false)
                        .collect(Collectors.toList());

        assertArrayEquals(initialList.toArray(), foundList.toArray());
    }
}
