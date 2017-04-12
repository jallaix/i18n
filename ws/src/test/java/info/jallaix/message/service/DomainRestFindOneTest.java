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
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * The Domain REST controller must verify tests provided by {@link BaseRestElasticsearchTestCase} linked to the {@link RestTestedMethod.FindOne} category.
 * It also defines custom tests:
 * <ul>
 * <li>Getting a domain returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.</li>
 * <li>Getting a domain for an existing supported language returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.</li>
 * <li>Getting a domain for a missing supported language returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.</li>
 * <li>Getting a domain for a missing complex language (with an existing message for the simple language) returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.</li>
 * <li>Getting a domain for an existing complex language returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.</li>
 * <li>Getting a domain for a missing complex language (without an existing message for the simple language) returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.</li>
 * <li>Getting a domain for an unsupported language returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.</li>
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
     * Getting a domain returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.
     * The existing entity is defined by the {@link ElasticsearchTestFixture#newExistingDocument()} method.
     * Its description is localized for the default I18N domain's language and specify this language tag in the HTTP headers.
     */
    @Override
    public void findExistingEntity() {

        ResponseEntity<Resource<Domain>> entity = getEntity(getTestFixture().newExistingDocument(), HttpStatus.OK, false);
        assertThat(entity.getHeaders().getFirst(HttpHeaders.CONTENT_LANGUAGE), is("en"));
    }

    /**
     * Getting a domain for an existing supported language returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.
     * The existing entity is defined by the {@link ElasticsearchTestFixture#newExistingDocument()} method.
     * Its description is localized for the specified supported language and specify this language tag in the HTTP headers.
     */
    @Test
    public void findExistingDomainWithExistingSupportedLanguage() {
        assertFindOne("fr;q=1", DomainTestFixture.DOMAIN3_FR_DESCRIPTION, "fr");
    }

    /**
     * Getting a domain for a missing supported language returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.
     * The existing entity is defined by the {@link ElasticsearchTestFixture#newExistingDocument()} method.
     * Its description is localized for the default I18N domain's language and specify this language tag in the HTTP headers.
     */
    @Test
    public void findExistingDomainWithMissingSupportedLanguage() {
        assertFindOne("es", null, "es");
    }

    /**
     * Getting a domain for a missing complex language (with an existing message for the simple language) returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.
     * The existing entity is defined by the {@link ElasticsearchTestFixture#newExistingDocument()} method.
     * Its description is localized for the linked simple language and specify the complex language tag in the HTTP headers.
     */
    @Test
    public void findExistingDomainWithMissingComplexLanguageButExistingSimpleLanguage() {
        assertFindOne("fr;q=0.5,fr-BE;q=1,en;q=0.1", DomainTestFixture.DOMAIN3_FR_DESCRIPTION, "fr-BE");
    }

    /**
     * Getting a domain for an existing complex language returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.
     * The existing entity is defined by the {@link ElasticsearchTestFixture#newExistingDocument()} method.
     * Its description is localized for the complex language and specify this language tag in the HTTP headers.
     */
    @Test
    public void findExistingDomainWithExistingComplexLanguage() {
        assertFindOne("en;q=0.5,en-US;q=1", DomainTestFixture.DOMAIN3_EN_US_DESCRIPTION, "en-US");
    }

    /**
     * Getting a domain for a missing complex language (without an existing message for the simple language) returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.
     * The existing entity is defined by the {@link ElasticsearchTestFixture#newExistingDocument()} method.
     * Its description is localized for the default I18N domain's language and specify the complex language tag in the HTTP headers.
     */
    @Test
    public void findExistingDomainWithMissingComplexAndSimpleLanguage() {
        assertFindOne("es-ES,es", null, "es-ES");
    }

    /**
     * Getting a domain for an unsupported language returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.
     * The existing entity is defined by the {@link ElasticsearchTestFixture#newExistingDocument()} method.
     * Its description is localized for the default I18N domain's language and specify this language tag in the HTTP headers.
     */
    @Test
    public void findExistingDomainWithUnsupportedLanguage() {
        assertFindOne("de-DE,de", null, "en");
    }


    /**
     * Assert that a domain found by identifier matches the expected one.
     * The domain description is returned in a localized language depending on the provided language tag.
     *
     * @param languageRanges      The language ranges that involves a localized description
     * @param descriptionFixture  The localized description matching the provided language tag
     * @param expectedLanguageTag The expected language tag
     */
    private void assertFindOne(String languageRanges, String descriptionFixture, String expectedLanguageTag) {

        // Get domain fixture for the language tag
        Domain domain = getTestFixture().newExistingDocument();
        if (descriptionFixture != null)
            domain.setDescription(descriptionFixture);

        // Assert the found domain matches the expected one
        final ResponseEntity<Resource<Domain>> entity = getEntity(domain, HttpStatus.OK, false, languageRanges);
        assertThat(entity.getHeaders().getFirst(HttpHeaders.CONTENT_LANGUAGE), is(expectedLanguageTag));
    }
}
