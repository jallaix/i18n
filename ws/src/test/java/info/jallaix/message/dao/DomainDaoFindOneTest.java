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

import java.util.Locale;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * The Domain DAO must verify some tests provided by {@link BaseDaoElasticsearchTestCase}.
 */
@Configuration
@Import({SpringDataEsTestConfiguration.class, DomainDaoTestConfiguration.class})
@EnableElasticsearchRepositories(basePackageClasses = DomainDao.class)
@EnableAspectJAutoProxy
@ContextConfiguration(classes = DomainDaoFindOneTest.class)
public class DomainDaoFindOneTest extends BaseDaoElasticsearchTestCase<Domain, String, DomainDao> {

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


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Tests lifecycle                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Constructor defining the "find one" tests
     */
    public DomainDaoFindOneTest() {
        super(DaoTestedMethod.FindOne.class);
    }

    /**
     * Initialize custom testing objects.
     */
    @Before
    public void initTest() {

        // Domain customizer for default DAO tests
        setCustomizer(
                new DomainDaoTestsCustomizer(
                        new DomainDaoChecker(i18nDomainHolder, esOperations, kryo),
                        threadLocaleHolder,
                        getTestFixture(),
                        kryo));
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
     * Finding an existing domain for an existing supported language returns this domain.
     * Its description is localized for the specified supported language.
     */
    @Test
    public void findOneExistingDomainWithExistingSupportedLanguage() {
        assertFindOne("fr;q=1", DomainTestFixture.DOMAIN3_FR_DESCRIPTION);
    }

    /**
     * Finding an existing domain for a missing supported language returns this domain.
     * Its description is localized for the default I18N domain's language.
     */
    @Test
    public void findOneExistingDomainWithMissingSupportedLanguage() {
        assertFindOne("es");
    }

    /**
     * Finding an existing domain for a missing complex language (with an existing message for the simple language) returns this domain.
     * Its description is localized for the linked simple language.
     */
    @Test
    public void findOneExistingDomainWithMissingComplexLanguageButExistingSimpleLanguage() {
        assertFindOne("fr;q=0.5,fr-BE;q=1,en;q=0.1", DomainTestFixture.DOMAIN3_FR_DESCRIPTION);
    }

    /**
     * Finding an existing domain for an existing complex language returns this domain.
     * Its description is localized for the linked complex language.
     */
    @Test
    public void findOneExistingDomainWithExistingComplexLanguage() {
        assertFindOne("en;q=0.5,en-US;q=1", DomainTestFixture.DOMAIN3_EN_US_DESCRIPTION);
    }

    /**
     * Finding an existing domain for a missing complex language (without an existing message for the simple language) returns this domain.
     * Its description is localized for the default I18N domain's language.
     */
    @Test
    public void findOneExistingDomainWithMissingComplexAndSimpleLanguage() {
        assertFindOne("es-ES,es");
    }

    /**
     * Finding an existing domain for an unsupported language returns this domain.
     * Its description is localized for the default I18N domain's language.
     */
    @Test
    public void findOneExistingDomainWithUnsupportedLanguage() {
        assertFindOne("de-DE,de");
    }


    /**
     * Assert that a domain found by identifier matches the expected one.
     * The domain description is returned in the default domain's language whatever the provided language tag is.
     *
     * @param languageTag Language tag that doesn't involve a matching description in this language
     */
    private void assertFindOne(String languageTag) {
        assertFindOne(languageTag, null);
    }

    /**
     * Assert that a domain found by identifier matches the expected one.
     * The domain description is returned in a localized language depending on the provided language tag.
     *
     * @param languageTag        The language tag that involves a localized description
     * @param descriptionFixture The localized description matching the provided language tag
     */
    private void assertFindOne(String languageTag, String descriptionFixture) {

        // Get domain fixture for the language tag
        threadLocaleHolder.setOutputLocales(Locale.LanguageRange.parse(languageTag));
        Domain domain = getTestFixture().newExistingDocument();
        if (descriptionFixture != null)
            domain.setDescription(descriptionFixture);

        // Assert the found domain matches the expected one
        assertThat(getRepository().findOne(domain.getId()), is(domain));
    }
}
