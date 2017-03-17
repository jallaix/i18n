package info.jallaix.message.service;

import info.jallaix.message.ApplicationMock;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.bean.DomainRestTestFixture;
import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.message.bean.EntityMessage;
import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.dao.EntityMessageDao;
import info.jallaix.spring.data.es.test.fixture.ElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.fixture.RestElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.testcase.BaseRestElasticsearchTestCase;
import lombok.SneakyThrows;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by Julien on 22/01/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ApplicationMock.class)
@WebIntegrationTest(randomPort = true)
public class DomainRestTest extends BaseRestElasticsearchTestCase<Domain, String, DomainDao> {

    @Autowired
    private DomainHolder i18nDomainHolder;

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                      Abstract methods implementation                                           */
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

    @Override
    protected ElasticsearchTestFixture<Domain> getTestFixture() {
        return new DomainTestFixture();
    }

    @Override
    protected RestElasticsearchTestFixture<Domain> getRestTestFixture() {
        return new DomainRestTestFixture();
    }


    @Autowired
    private EntityMessageDao entityMessageDao;

    @Override
    public void createValidEntity() {
        ResponseEntity<Resource<Domain>> httpResponse = postEntity(getTestFixture().newDocumentToInsert(), HttpStatus.CREATED, false);

        Domain savedDomain = httpResponse.getBody().getContent();
        String languageTag = savedDomain.getDefaultLanguageTag();
        String messageType = savedDomain.getDescription();

        EntityMessage message = entityMessageDao.findOne(i18nDomainHolder.getDomain().getId(), messageType, savedDomain.getId(), languageTag);
        assertThat(message, is(notNullValue()));
    }

    /**
     * <p>Get expected HATEOAS links when requesting language resources.</p>
     * <p>Add the search link because the domain repository enables search by code.</p>
     *
     * @return A list of HATEOAS links
     */
    @Override
    protected List<Link> getLanguagesLinks() {

        List<Link> links = new ArrayList<>(super.getLanguagesLinks());
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
    protected List<Link> getPagedLanguagesLinks(boolean sorted, Integer page) {

        List<Link> links = new ArrayList<>(super.getPagedLanguagesLinks(sorted, page));
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
        return new Link(new URI("http", null, "localhost", serverPort, "/domains/search", null, null).toString(), "search");
    }
}
