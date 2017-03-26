package info.jallaix.message.service;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.bean.DomainRestTestFixture;
import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.config.TestDomainRestConfiguration;
import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.dao.DomainDaoChecker;
import info.jallaix.message.dao.DomainDaoTestsCustomizer;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import info.jallaix.spring.data.es.test.fixture.ElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.fixture.RestElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.testcase.BaseRestElasticsearchTestCase;
import info.jallaix.spring.data.es.test.testcase.RestTestedMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

/**
 * The Domain REST controller must verify some tests provided by {@link BaseRestElasticsearchTestCase}.
 * It also defines custom tests:
 * <ul>
 * <li>Getting a domain by code returns a {@code 404 Not Found} HTTP status code if there is no domain found.</li>
 * <li>Getting a domain returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.</li>
 * </ul>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestDomainRestConfiguration.class)
@WebIntegrationTest(randomPort = true)
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class DomainRestFindOneTest extends BaseRestElasticsearchTestCase<Domain, String, DomainDao> {

    /**
     * I18N domain holder
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
     * REST template for calling server operations
     */
    @Autowired
    private RestTemplate restTemplate;


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Tests lifecycle                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Constructor that defines the tests to pass
     */
    public DomainRestFindOneTest() {
        super(RestTestedMethod.FindOne.class);
    }

    @Before
    public void initTest() throws Exception {

        // Domain customizer for default DAO tests
        setCustomizer(
                new DomainDaoTestsCustomizer(
                        new DomainDaoChecker(i18nDomainHolder, esOperations, kryo),
                        threadLocaleHolder,
                        getTestFixture(),
                        kryo));
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                     Test fixture                                               */
    /*----------------------------------------------------------------------------------------------------------------*/

    @Override
    protected ElasticsearchTestFixture<Domain> getTestFixture() {
        return new DomainTestFixture();
    }

    @Override
    protected RestElasticsearchTestFixture<Domain> getRestTestFixture() {
        return new DomainRestTestFixture();
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                     Custom tests                                               */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Finding an existing domain for an existing supported language returns this domain.
     * Its description is localized for the specified supported language.
     */
    @Test
    public void findExistingDomainWithExistingSupportedLanguage() {
        assertFindOne("fr;q=1", DomainTestFixture.DOMAIN3_FR_DESCRIPTION);
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
        Domain domain = getTestFixture().newExistingDocument();
        if (descriptionFixture != null)
            domain.setDescription(descriptionFixture);

        // Assert the found domain matches the expected one
        getEntity(domain, HttpStatus.OK, false, languageTag);
    }
}
