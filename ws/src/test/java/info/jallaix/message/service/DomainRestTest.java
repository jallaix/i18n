package info.jallaix.message.service;

import info.jallaix.message.ApplicationMock;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.bean.DomainRestTestFixture;
import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.message.bean.EntityMessage;
import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.dao.EntityMessageDao;
import info.jallaix.spring.data.es.test.fixture.ElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.fixture.RestElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.testcase.BaseRestElasticsearchTestCase;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    private Domain messageDomain;

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

        EntityMessage message = entityMessageDao.findOne(messageDomain.getId(), messageType, savedDomain.getId(), languageTag);
        assertThat(message, is(notNullValue()));
    }
}
