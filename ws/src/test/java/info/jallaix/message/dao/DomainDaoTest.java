package info.jallaix.message.dao;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.config.TestDomainDaoConfiguration;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import info.jallaix.spring.data.es.test.fixture.ElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.testcase.BaseDaoElasticsearchTestCase;
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

import static org.junit.Assert.*;

/**
 * <p>The Domain DAO must verify some tests provided by {@link BaseDaoElasticsearchTestCase}.</p>
 * <p>No language is used for input and output data.</p>
 * <p>Data exist for the default language.</p>
 */
@Configuration
@Import(TestDomainDaoConfiguration.class)
@EnableElasticsearchRepositories(basePackageClasses = DomainDao.class)
@EnableAspectJAutoProxy
@ContextConfiguration(classes = DomainDaoTest.class)
public class DomainDaoTest extends BaseDaoElasticsearchTestCase<Domain, String, DomainDao> {

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
    protected ThreadLocaleHolder threadLocaleHolder;

    /**
     * Utility object that performs DAO checks
     */
    protected DomainDaoChecker domainDaoChecker;

    /**
     * Domain DAO customizer
     */
    protected DomainDaoTestsCustomizer domainDaoTestsCustomizer;


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Tests lifecycle                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Constructor defining some basic tests
     */
    public DomainDaoTest() {
        super();/*
                DaoTestedMethod.Exist.class,
                DaoTestedMethod.Count.class,
                DaoTestedMethod.DeleteAll.class,
                DaoTestedMethod.DeleteAllById.class,
                DaoTestedMethod.Delete.class,
                DaoTestedMethod.DeleteById.class);*/
    }

    /**
     * Initialize custom testing objects.
     */
    @Before
    public void initTest() {

        // Utility object that performs DAO checks
        domainDaoChecker = new DomainDaoChecker(i18nDomainHolder, esOperations, kryo);

        // Domain customizer for DAO tests
        domainDaoTestsCustomizer = new DomainDaoTestsCustomizer(domainDaoChecker, threadLocaleHolder, getTestFixture(), kryo);
        setCustomizer(domainDaoTestsCustomizer);
        customizeTest(domainDaoTestsCustomizer);
    }

    /**
     * Apply customization before executing a test.
     *
     * @param domainDaoTestsCustomizer Domain customizer for DAO tests
     */
    public void customizeTest(DomainDaoTestsCustomizer domainDaoTestsCustomizer) {

        // Descriptions fixture for default language
        domainDaoTestsCustomizer.setDescriptionFixture(DomainTestFixture.DOMAIN3_EN_DESCRIPTION);
        domainDaoTestsCustomizer.setDescriptionsFixture(DomainDaoTestUtils.getEnglishDescriptions());
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

    /**
     * Get the persistent document class
     *
     * @return The persistent document class
     */
    @Override
    protected Class<Domain> getDocumentClass() {
        return Domain.class;
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                     Custom tests                                               */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Getting a domain by code returns {@code null} if there is no domain found.
     * The missing domain is defined by the {@link ElasticsearchTestFixture#newDocumentToInsert()} method.
     */
    @Test
    public void findMissingEntityByCode() {
        assertNull(getRepository().findByCode(getTestFixture().newDocumentToInsert().getCode()));
    }

    /**
     * Getting a domain returns this entity if the domain is found.
     * The existing domain is defined by the {@link ElasticsearchTestFixture#newExistingDocument()} method.
     */
    @Test
    public void findExistingEntityByCode() {

        Domain fixture = getCustomizer().customizeFindOneFixture(getTestFixture().newExistingDocument());
        Domain found = getRepository().findByCode(fixture.getCode());

        assertNotNull(found);
        assertEquals(fixture, found);
    }
}
