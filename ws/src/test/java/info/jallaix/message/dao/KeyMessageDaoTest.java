package info.jallaix.message.dao;

import info.jallaix.message.bean.KeyMessage;
import info.jallaix.message.bean.KeyMessageTestFixture;
import info.jallaix.message.config.TestDomainDaoConfiguration;
import info.jallaix.spring.data.es.test.fixture.ElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.testcase.BaseDaoElasticsearchTestCase;
import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

/**
 * <p>The Key Message DAO must verify some tests provided by {@link BaseDaoElasticsearchTestCase}.</p>
 */
@Configuration
@Import(TestDomainDaoConfiguration.class)
@EnableElasticsearchRepositories(basePackageClasses = DomainDao.class)
@EnableAspectJAutoProxy
@ContextConfiguration(classes = KeyMessageDaoTest.class)
public class KeyMessageDaoTest extends BaseDaoElasticsearchTestCase<KeyMessage, String, KeyMessageDao> {

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


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Tests lifecycle                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Constructor defining some basic tests
     */
    public KeyMessageDaoTest() {
        super();/*
                DaoTestedMethod.Exist.class,
                DaoTestedMethod.Count.class,
                DaoTestedMethod.DeleteAll.class,
                DaoTestedMethod.DeleteAllById.class,
                DaoTestedMethod.Delete.class,
                DaoTestedMethod.DeleteById.class);*/
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                     Test fixture                                               */
    /*----------------------------------------------------------------------------------------------------------------*/

    @Override
    protected ElasticsearchTestFixture<KeyMessage> getTestFixture() {
        return new KeyMessageTestFixture();
    }

    /**
     * Get the persistent document class
     *
     * @return The persistent document class
     */
    @Override
    protected Class<KeyMessage> getDocumentClass() {
        return KeyMessage.class;
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                     Custom tests                                               */
    /*----------------------------------------------------------------------------------------------------------------*/

    // TODO Prepare tests for custom DAO functions
}
