package info.jallaix.message.dao;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.config.TestDomainDaoConfiguration;
import info.jallaix.message.dao.interceptor.MissingSimpleMessageException;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import info.jallaix.message.dao.interceptor.UnsupportedLanguageException;
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

import static org.junit.Assert.fail;

/**
 * The Domain DAO must verify some tests provided by {@link BaseDaoElasticsearchTestCase}.
 */
@Configuration
@Import(TestDomainDaoConfiguration.class)
@EnableElasticsearchRepositories(basePackageClasses = DomainDao.class)
@EnableAspectJAutoProxy
@ContextConfiguration(classes = DomainDaoIndexTest.class)
public class DomainDaoIndexTest extends BaseDaoElasticsearchTestCase<Domain, String, DomainDao> {

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

    /**
     * Domain testing checks
     */
    private DomainDaoChecker domainDaoChecker;


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Tests lifecycle                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Constructor defining the "index one" tests
     */
    public DomainDaoIndexTest() {
        super(DaoTestedMethod.Index.class);
    }

    /**
     * Initialize custom testing objects.
     */
    @Before
    public void initTest() {

        // Utility object that performs DAO checks
        domainDaoChecker = new DomainDaoChecker(i18nDomainHolder, esOperations, kryo);

        // Domain customizer for default DAO tests
        setCustomizer(new DomainDaoTestsCustomizer(domainDaoChecker, threadLocaleHolder, getTestFixture(), kryo));
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
     * Indexing a new domain inserts the document in the index.
     * A domain description should be inserted in the message's index type for the I18N domain's default language,
     * even if the input locale is defined with a different language.
     */
    @Test
    public void indexNewDomainWithAnyLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("de-DE"));
        indexNewDocument();
    }

    /**
     * Indexing an existing domain replaces the document in the index.
     * A domain description should be modified in the message's index type for an existing supported language.
     */
    @Test
    public void indexExistingDomainWithExistingSupportedLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("fr"));
        indexExistingDocument();
    }

    /**
     * Indexing an existing domain replaces the document in the index.
     * A domain description should be inserted in the message's index type for a missing supported language.
     */
    @Test
    public void indexExistingDomainWithMissingSupportedLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("es"));
        indexExistingDocument();
    }

    /**
     * Indexing an existing domain replaces the document in the index.
     * A domain description should be inserted in the message's index type for a missing complex language tag
     * when a message already exists for its simple language tag.
     */
    @Test
    public void indexExistingDomainWithMissingComplexLanguageButExistingSimpleLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("fr-BE"));
        indexExistingDocument();
    }

    /**
     * Indexing an existing domain replaces the document in the index.
     * A domain description should be inserted in the message's index type for an existing complex language tag.
     */
    @Test
    public void indexExistingDomainWithExistingComplexLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("en-US"));
        indexExistingDocument();
    }

    /**
     * Indexing an existing domain replaces the document in the index.
     * An error should occur when trying to insert a domain description for a missing complex language tag
     * when a message doesn't already exist for its simple language tag.
     */
    @Test
    public void indexExistingDomainWithMissingComplexAndSimpleLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("es-ES"));
        try {
            indexExistingDocument();
            fail("MissingSimpleMessageException should be thrown.");
        } catch (MissingSimpleMessageException e) {

            // Check the domain to update has not been updated
            domainDaoChecker.checkDomainUnmodified(getTestFixture().newExistingDocument());
        }
    }

    /**
     * Indexing an existing domain replaces the document in the index.
     * An error should occur when trying to insert a domain description for a missing language tag
     * when this language tag is not supported by the I18N domain.
     */
    @Test
    public void indexExistingDomainWithUnsupportedLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("de-DE"));
        try {
            indexExistingDocument();
            fail("UnsupportedLanguageException should be thrown.");
        } catch (UnsupportedLanguageException e) {

            // Check the domain to update has not been updated
            domainDaoChecker.checkDomainUnmodified(getTestFixture().newExistingDocument());
        }
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
