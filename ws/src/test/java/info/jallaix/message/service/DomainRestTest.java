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
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
public class DomainRestTest extends BaseRestElasticsearchTestCase<Domain, String, DomainDao> {

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
    public DomainRestTest() {
        super();
        /*super(
                RestTestedMethod.Create.class,
                RestTestedMethod.Update.class,
                RestTestedMethod.Patch.class,
                RestTestedMethod.FindAll.class,
                RestTestedMethod.Exist.class,
                RestTestedMethod.Delete.class);*/
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
    /*                                      Abstract methods implementation                                           */
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
     * <p>Get expected HATEOAS links when requesting paged language resources.</p>
     * <p>Add the search link because the domain repository enables search by code.</p>
     *
     * @param sorted {@code true} if entities are sorted
     * @param page   {@code null} if no page is request, else a page number starting from 0
     * @return A list of HATEOAS links
     */
    @Override
    protected List<Link> getPagedResourcesLinks(boolean sorted, Integer page) {

        List<Link> links = new ArrayList<>(super.getPagedResourcesLinks(sorted, page));
        links.add(getSearchLink());

        return links;
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                     Custom tests                                               */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Getting a domain by code returns a {@code 404 Not Found} HTTP status code if there is no domain found.
     * The missing domain is defined by the {@link ElasticsearchTestFixture#newDocumentToInsert()} method.
     */
    @Test
    public void findMissingEntityByCode() {

        final HttpEntity<?> httpEntity = convertToHttpEntity(null);             // Define Hal+Json HTTP entity

        try {
            // Send a GET request
            restTemplate.exchange(
                    getSearchLink().getHref() + "/findByCode?code=" + getTestFixture().newDocumentToInsert().getCode(),
                    HttpMethod.GET,
                    httpEntity,
                    getRestTestFixture().getResourceType());

            fail("Should return a " + HttpStatus.NOT_FOUND.value() + " " + HttpStatus.NOT_FOUND.name() + " response");
        }

        // The POST request results in an error response
        catch (HttpStatusCodeException e) {
            assertThat(e.getStatusCode(), is(HttpStatus.NOT_FOUND));    // Verify the expected HTTP status code
        }
    }

    /**
     * Getting a domain returns this entity in HATEOAS format and a {@code 200 Ok} HTTP status code if the domain is found.
     * The existing domain is defined by the {@link ElasticsearchTestFixture#newExistingDocument()} method.
     */
    @Test
    public void findExistingEntityByCode() {

        final HttpEntity<?> httpEntity = convertToHttpEntity(null);             // Define Hal+Json HTTP entity

        Domain existingDomain = getTestFixture().newExistingDocument();
        final Resource<Domain> expectedResource = convertToResource(existingDomain);

        try {
            // Send a GET request
            final ResponseEntity<Resource<Domain>> responseEntity =
                    restTemplate.exchange(
                            getSearchLink().getHref() + "/findByCode?code=" + existingDomain.getCode(),
                            HttpMethod.GET,
                            httpEntity,
                            getRestTestFixture().getResourceType());

            // Verify the expected HTTP status code and response body then return the response
            assertThat(responseEntity, is(notNullValue()));
            assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
            assertThat(responseEntity.getBody(), is(expectedResource));
        }

        // The POST request results in an error response
        catch (HttpStatusCodeException e) {
            fail("An unexpected exception was thrown.\n" + e);
        }
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
}
