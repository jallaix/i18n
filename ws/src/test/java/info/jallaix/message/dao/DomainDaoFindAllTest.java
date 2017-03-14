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
     * Domain testing checks
     */
    private DomainDaoChecker domainDaoChecker;


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Tests lifecycle                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Constructor defining the "find one" tests
     */
    public DomainDaoFindAllTest() {
        super(DaoTestedMethod.FindAll.class);
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
     * Finding a list of all existing domains returns an iterable with all these domains.
     * Their descriptions are localized for the specified supported language.
     */
    @Test
    public void findAllWithExistingSupportedLanguage() {

        // Expected descriptions for all domains
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("1", DomainTestFixture.DOMAIN1_FR_DESCRIPTION);
        descriptions.put("2", DomainTestFixture.DOMAIN2_FR_DESCRIPTION);
        descriptions.put("3", DomainTestFixture.DOMAIN3_FR_DESCRIPTION);
        descriptions.put("4", DomainTestFixture.DOMAIN4_FR_DESCRIPTION);

        assertFindAll("fr", descriptions);
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
     * Finding a list of all existing domains for a missing complex language (with existing messages for the simple language) returns an iterable with all these domains.
     * Their descriptions are localized for the linked simple language.
     */
    @Test
    public void findAllExistingDomainWithMissingComplexLanguageButExistingSimpleLanguage() {

        // Expected descriptions for all domains
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("1", DomainTestFixture.DOMAIN1_FR_DESCRIPTION);
        descriptions.put("2", DomainTestFixture.DOMAIN2_FR_DESCRIPTION);
        descriptions.put("3", DomainTestFixture.DOMAIN3_FR_DESCRIPTION);
        descriptions.put("4", DomainTestFixture.DOMAIN4_FR_DESCRIPTION);

        assertFindAll("fr-BE", descriptions);
    }

    /**
     * Finding a list of all existing domains for an existing complex language returns an iterable with all these domains.
     * Their descriptions are localized for the complex language if available, else the linked simple language is used.
     */
    @Test
    public void findAllExistingDomainWithExistingComplexLanguage() {

        // Expected descriptions for all domains
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("1", DomainTestFixture.DOMAIN1_EN_US_DESCRIPTION);
        descriptions.put("2", DomainTestFixture.DOMAIN2_EN_DESCRIPTION);
        descriptions.put("3", DomainTestFixture.DOMAIN3_EN_US_DESCRIPTION);
        descriptions.put("4", DomainTestFixture.DOMAIN4_EN_DESCRIPTION);

        assertFindAll("en-US", descriptions);
    }

    /**
     * Finding a list of all existing domains for a missing complex language (without any existing message for the simple language) returns an iterable with all these domains.
     * Their descriptions are localized for the default I18N domain's language.
     */
    @Test
    public void findAllExistingDomainWithMissingComplexAndSimpleLanguage() {
        assertFindAll("es-ES");
    }

    /**
     * Finding a list of all existing domains for an unsupported language returns an iterable with all these domains.
     * Their descriptions are localized for the default I18N domain's language.
     */
    @Test
    public void findAllExistingDomainWithUnsupportedLanguage() {
        assertFindAll("de-DE");
    }


    /**
     * Assert that all domains found match the expected ones.
     * The domain descriptions are returned in a localized language depending on the provided language tag.
     * The expected domain descriptions are localized for the default I18N language.
     *
     * @param languageTag The language tag that involves a localized description
     */
    private void assertFindAll(String languageTag) {

        // Expected descriptions for all domains
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("1", DomainTestFixture.DOMAIN1_EN_DESCRIPTION);
        descriptions.put("2", DomainTestFixture.DOMAIN2_EN_DESCRIPTION);
        descriptions.put("3", DomainTestFixture.DOMAIN3_EN_DESCRIPTION);
        descriptions.put("4", DomainTestFixture.DOMAIN4_EN_DESCRIPTION);

        assertFindAll(languageTag, descriptions);
    }

    /**
     * Assert that all domains found match the expected ones.
     * The domain descriptions must be returned in a localized language depending on the provided language tag.
     *
     * @param languageTag         The language tag that involves a localized description
     * @param descriptionsFixture The localized descriptions matching the provided language tag
     */
    private void assertFindAll(String languageTag, Map<String, String> descriptionsFixture) {

        // Get all domains from the index with specified descriptions
        final List<Domain> initialList = domainDaoChecker.internationalizeDomains(
                testClientOperations.findAllDocumentsPaged(
                        getDocumentMetadata(),
                        0,
                        (int) this.getTestDocumentsLoader().getLoadedDocumentCount()),
                descriptionsFixture);

        // Repository search for the specified language
        threadLocaleHolder.setOutputLocale(Locale.forLanguageTag(languageTag));
        final List<Domain> foundList =
                StreamSupport.stream(
                        getRepository().findAll().spliterator(), false)
                        .collect(Collectors.toList());

        assertArrayEquals(initialList.toArray(), foundList.toArray());
    }
}
