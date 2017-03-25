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
import info.jallaix.message.dao.EntityMessageDao;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import info.jallaix.spring.data.es.test.fixture.ElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.fixture.RestElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.testcase.BaseRestElasticsearchTestCase;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.hateoas.Link;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Julien on 22/01/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestDomainRestConfiguration.class)
@WebIntegrationTest(randomPort = true)
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


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Tests lifecycle                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

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
        return new Link(new URI(getServerUri() + "/domains/search").toString(), "search");
    }
}
