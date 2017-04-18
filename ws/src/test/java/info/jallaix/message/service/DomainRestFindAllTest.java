package info.jallaix.message.service;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.bean.DomainRestTestFixture;
import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.config.TestDomainRestConfiguration;
import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.dao.DomainDaoChecker;
import info.jallaix.message.dao.DomainDaoTestUtils;
import info.jallaix.message.dao.DomainDaoTestsCustomizer;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import info.jallaix.spring.data.es.test.fixture.ElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.fixture.RestElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.testcase.BaseRestElasticsearchTestCase;
import info.jallaix.spring.data.es.test.testcase.RestTestedMethod;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public class DomainRestFindAllTest extends BaseRestElasticsearchTestCase<Domain, String, DomainDao> {

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
     * Domain DAO customizer
     */
    private DomainDaoTestsCustomizer domainDaoTestsCustomizer;


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Tests lifecycle                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Constructor that defines the tests to pass
     */
    public DomainRestFindAllTest() {
        super(RestTestedMethod.FindAll.class);
    }

    @Before
    public void initTest() throws Exception {

        // Utility object that performs DAO checks
        DomainDaoChecker domainDaoChecker = new DomainDaoChecker(i18nDomainHolder, esOperations, kryo);

        // Domain customizer for DAO tests
        domainDaoTestsCustomizer = new DomainDaoTestsCustomizer(domainDaoChecker, threadLocaleHolder, getTestFixture(), kryo);
        setCustomizer(domainDaoTestsCustomizer);
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
    /*                                                  Method overrides                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * <p>Get expected HATEOAS links when requesting language resources.</p>
     * <p>Add the search link because the domain repository enables search by code.</p>
     *
     * @return A list of HATEOAS links
     */
    @Override
    protected List<Link> getResourcesLinks() {

        List<Link> links = new ArrayList<>(super.getResourcesLinks());
        links.add(getSearchLink());

        return links;
    }

    /**
     * Get the search HATEOAS link.
     *
     * @return The the search HATEOAS link
     */
    @SneakyThrows
    private Link getSearchLink() {
        return new Link(new URI(getServerUri() + "/domains/search").toString(), "search");
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                     Custom tests                                               */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Getting all domains returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     * Their descriptions are localized for the default I18N domain's language and specify this language tag in the HTTP headers.
     */
    @Override
    public void findEntities() {
        assertFindAll(null, DomainDaoTestUtils.getEnglishDescriptions(), "en");
    }

    /**
     * Getting all domains sorted returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     * Their descriptions are localized for the default I18N domain's language and specify this language tag in the HTTP headers.
     */
    @Override
    public void findEntitiesSorted() {
        assertFindAllSorted(null, DomainDaoTestUtils.getEnglishDescriptions(), "en");
    }

    /**
     * Getting all domains for an existing supported language returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     * Their descriptions are localized for the specified supported language and specify this language tag in the HTTP headers.
     */
    @Test
    public void findDomainsWithExistingSupportedLanguage() {
        assertFindAll("fr;q=1", DomainDaoTestUtils.getFrenchDescriptions(), "fr");
    }

    /**
     * Getting all domains sorted for an existing supported language returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     * Their descriptions are localized for the specified supported language and specify this language tag in the HTTP headers.
     */
    @Test
    public void findDomainsSortedWithExistingSupportedLanguage() {
        assertFindAllSorted("fr;q=1", DomainDaoTestUtils.getFrenchDescriptions(), "fr");
    }

    /**
     * Getting all domains for a missing supported language returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     * Their descriptions are localized for the default I18N domain's language and specify this language tag in the HTTP headers.
     */
    @Test
    public void findDomainsWithMissingSupportedLanguage() {
        assertFindAll("es", "es");
    }

    /**
     * Getting all domains sorted for a missing supported language returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     * Their descriptions are localized for the default I18N domain's language and specify this language tag in the HTTP headers.
     */
    @Test
    public void findDomainsSortedWithMissingSupportedLanguage() {
        assertFindAllSorted("es", "es");
    }

    /**
     * Getting all domains for a missing complex language (with an existing message for the simple language) returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     * Their descriptions are localized for the linked simple language and specify the complex language tag in the HTTP headers.
     */
    @Test
    public void findDomainsWithMissingComplexLanguageButExistingSimpleLanguage() {
        assertFindAll("fr;q=0.5,fr-BE;q=1,en;q=0.1", DomainDaoTestUtils.getFrenchDescriptions(), "fr-BE");
    }

    /**
     * Getting all domains sorted for a missing complex language (with an existing message for the simple language) returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     * Their descriptions are localized for the linked simple language and specify the complex language tag in the HTTP headers.
     */
    @Test
    public void findDomainsSortedWithMissingComplexLanguageButExistingSimpleLanguage() {
        assertFindAllSorted("fr;q=0.5,fr-BE;q=1,en;q=0.1", DomainDaoTestUtils.getFrenchDescriptions(), "fr-BE");
    }

    /**
     * Getting all domains for an existing complex language returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     * Their descriptions are localized for the complex language and specify this language tag in the HTTP headers.
     */
    @Test
    public void findDomainsWithExistingComplexLanguage() {
        assertFindAll("en;q=0.5,en-US;q=1", DomainDaoTestUtils.getEnglishUsDescriptions(), "en-US");
    }

    /**
     * Getting all domains sorted for an existing complex language returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     * Their descriptions are localized for the complex language and specify this language tag in the HTTP headers.
     */
    @Test
    public void findDomainsSortedWithExistingComplexLanguage() {
        assertFindAllSorted("en;q=0.5,en-US;q=1", DomainDaoTestUtils.getEnglishUsDescriptions(), "en-US");
    }

    /**
     * Getting all domains for a missing complex language (without an existing message for the simple language) returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     * Their descriptions are localized for the default I18N domain's language and specify the complex language tag in the HTTP headers.
     */
    @Test
    public void findDomainsWithMissingComplexAndSimpleLanguage() {
        assertFindAll("es-ES,es", "es-ES");
    }

    /**
     * Getting all domains sorted for a missing complex language (without an existing message for the simple language) returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     * Their descriptions are localized for the default I18N domain's language and specify the complex language tag in the HTTP headers.
     */
    @Test
    public void findDomainsSortedWithMissingComplexAndSimpleLanguage() {
        assertFindAllSorted("es-ES,es", "es-ES");
    }

    /**
     * Getting all domains for an unsupported language returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     * Their descriptions are localized for the default I18N domain's language and specify this language tag in the HTTP headers.
     */
    @Test
    public void findDomainsWithUnsupportedLanguage() {
        assertFindAll("de-DE,de", "en");
    }

    /**
     * Getting all domains sorted for an unsupported language returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     * Their descriptions are localized for the default I18N domain's language and specify this language tag in the HTTP headers.
     */
    @Test
    public void findDomainsSortedWithUnsupportedLanguage() {
        assertFindAllSorted("de-DE,de", "en");
    }


    /**
     * Assert that all domains found match the expected ones.
     * The domain descriptions are returned in a localized language depending on the provided language tag.
     * The expected domain descriptions are localized for the default I18N language.
     *
     * @param languageRanges      The language ranges that involves a localized description
     * @param expectedLanguageTag The expected language tag
     */
    private void assertFindAll(String languageRanges, String expectedLanguageTag) {
        assertFindAll(languageRanges, DomainDaoTestUtils.getEnglishDescriptions(), expectedLanguageTag);
    }

    /**
     * Assert that all domains found match the expected ones.
     * The domain descriptions must be returned in a localized language depending on the provided language tag.
     *
     * @param languageRanges      The language ranges that involves a localized description
     * @param descriptionsFixture The localized descriptions matching the provided language tag
     * @param expectedLanguageTag The expected language tag
     */
    private void assertFindAll(String languageRanges, Map<String, String> descriptionsFixture, String expectedLanguageTag) {

        domainDaoTestsCustomizer.setDescriptionsFixture(descriptionsFixture);

        // Assert the found domain matches the expected one
        final ResponseEntity<PagedResources<Resource<Domain>>> entity = getEntities(languageRanges);
        assertThat(entity.getHeaders().getFirst(HttpHeaders.CONTENT_LANGUAGE), is(expectedLanguageTag));
    }

    /**
     * Assert that all domains found match the expected ones.
     * The domain descriptions are returned in a localized language depending on the provided language tag.
     * The expected domain descriptions are localized for the default I18N language.
     *
     * @param languageRanges      The language ranges that involves a localized description
     * @param expectedLanguageTag The expected language tag
     */
    private void assertFindAllSorted(String languageRanges, String expectedLanguageTag) {
        assertFindAllSorted(languageRanges, DomainDaoTestUtils.getEnglishDescriptions(), expectedLanguageTag);
    }

    /**
     * Assert that all domains found match the expected ones.
     * The domain descriptions must be returned in a localized language depending on the provided language tag.
     *
     * @param languageRanges      The language ranges that involves a localized description
     * @param descriptionsFixture The localized descriptions matching the provided language tag
     * @param expectedLanguageTag The expected language tag
     */
    private void assertFindAllSorted(String languageRanges, Map<String, String> descriptionsFixture, String expectedLanguageTag) {

        domainDaoTestsCustomizer.setDescriptionsFixture(descriptionsFixture);

        // Assert the found domain matches the expected one
        final ResponseEntity<PagedResources<Resource<Domain>>> entity = getEntities(true, languageRanges);
        assertThat(entity.getHeaders().getFirst(HttpHeaders.CONTENT_LANGUAGE), is(expectedLanguageTag));
    }

    /**
     * Get the persistent document class
     *
     * @return The persistent document class
     */
    @Override
    protected Class<Domain> getDocumentClass() {
        return Domain.class;
    }
}
