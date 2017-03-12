package info.jallaix.message.dao;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.message.config.DomainDaoTestConfiguration;
import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.dao.interceptor.MissingSimpleMessageException;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import info.jallaix.message.dao.interceptor.UnsupportedLanguageException;
import info.jallaix.spring.data.es.test.SpringDataEsTestConfiguration;
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

import java.util.Locale;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * The Domain DAO must verify some tests provided by {@link BaseDaoElasticsearchTestCase}.
 */
@Configuration
@Import({SpringDataEsTestConfiguration.class, DomainDaoTestConfiguration.class})
@EnableElasticsearchRepositories(basePackageClasses = DomainDao.class)
@EnableAspectJAutoProxy
@ContextConfiguration(classes = DomainDaoTest.class)
public class DomainDaoTest extends BaseDaoElasticsearchTestCase<Domain, String, DomainDao> {


    /**
     * Domain 3's english description (for use by find tests)
     */
    public static final String DOMAIN3_EN_DESCRIPTION = "Test project 2's description";

    /**
     * Domain 3's french description (for use by find tests)
     */
    public static final String DOMAIN3_FR_DESCRIPTION = "Description du projet de test 2";

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
    /*                                                  Overridden methods                                            */
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
    public void indexExistingDomainWithComplexLanguageWithSimpleLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("fr-BE"));
        indexExistingDocument();
    }

    /**
     * Indexing an existing domain replaces the document in the index.
     * An error should occur when trying to insert a domain description for a missing complex language tag
     * when a message doesn't already exist for its simple language tag.
     */
    @Test
    public void indexExistingDomainWithComplexLanguageWithoutSimpleLanguage() {

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
     * Saving a new domain inserts the document in the index.
     * A domain description should be inserted in the message's index type for the I18N domain's default language,
     * even if the input locale is defined with a different language.
     */
    @Test
    public void saveNewDomainWithAnyLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("de-DE"));
        saveNewDocument();
    }

    /**
     * Saving an existing domain replaces the document in the index.
     * A domain description should be modified in the message's index type for an existing supported language.
     */
    @Test
    public void saveExistingDomainWithExistingSupportedLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("fr"));
        saveExistingDocument();
    }

    /**
     * Saving an existing domain replaces the document in the index.
     * A domain description should be inserted in the message's index type for a missing supported language.
     */
    @Test
    public void saveExistingDomainWithMissingSupportedLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("es"));
        saveExistingDocument();
    }

    /**
     * Saving an existing domain replaces the document in the index.
     * A domain description should be inserted in the message's index type for a missing complex language tag
     * when a message already exists for its simple language tag.
     */
    @Test
    public void saveExistingDomainWithComplexLanguageWithSimpleLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("fr-BE"));
        saveExistingDocument();
    }

    /**
     * Saving an existing domain replaces the document in the index.
     * An error should occur when trying to insert a domain description for a missing complex language tag
     * when a message doesn't already exist for its simple language tag.
     */
    @Test
    public void saveExistingDomainWithComplexLanguageWithoutSimpleLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("es-ES"));
        try {
            saveExistingDocument();
            fail("MissingSimpleMessageException should be thrown.");
        }
        catch (MissingSimpleMessageException e) {

            // Check the domain to update has not been updated
            domainDaoChecker.checkDomainUnmodified(getTestFixture().newExistingDocument());
        }
    }

    /**
     * Saving an existing domain replaces the document in the index.
     * An error should occur when trying to insert a domain description for a missing language tag
     * when this language tag is not supported by the I18N domain.
     */
    @Test
    public void saveExistingDomainWithUnsupportedLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("de-DE"));
        try {
            saveExistingDocument();
            fail("UnsupportedLanguageException should be thrown.");
        }
        catch (UnsupportedLanguageException e) {

            // Check the domain to update has not been updated
            domainDaoChecker.checkDomainUnmodified(getTestFixture().newExistingDocument());
        }
    }

    /**
     * Saving a list of domains inserts and updates the documents in the index.
     * For creation, a domain description should be inserted in the message's index type for the I18N domain's default language,
     * even if the input locale is defined with a different language.
     * For update, a domain description should be modified in the message's index type for an existing supported language.
     */
    @Test
    public void saveDomainsWithExistingSupportedLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("fr"));
        saveDocuments();
    }

    /**
     * Saving a list of domains inserts and updates the documents in the index.
     * For creation, a domain description should be inserted in the message's index type for the I18N domain's default language,
     * even if the input locale is defined with a different language.
     * For update, a domain description should be inserted in the message's index type for a missing supported language.
     */
    @Test
    public void saveDomainsWithMissingSupportedLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("es"));
        saveDocuments();
    }

    /**
     * Saving a list of domains inserts and updates the documents in the index.
     * For creation, a domain description should be inserted in the message's index type for the I18N domain's default language,
     * even if the input locale is defined with a different language.
     * For update, a domain description should be inserted in the message's index type for a missing complex language tag
     * when a message already exists for its simple language tag.
     */
    @Test
    public void saveDomainsWithComplexLanguageWithSimpleLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("fr-BE"));
        saveDocuments();
    }

    /**
     * Saving a list of domains inserts and updates the documents in the index.
     * For creation, a domain description should be inserted in the message's index type for the I18N domain's default language,
     * even if the input locale is defined with a different language.
     * For update, an error should occur when trying to insert a domain description for a missing complex language tag
     * when a message doesn't already exist for its simple language tag.
     */
    @Test
    public void saveDomainsWithComplexLanguageWithoutSimpleLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("es-ES"));
        try {
            saveDocuments();
            fail("MissingSimpleMessageException should be thrown.");
        }
        catch (MissingSimpleMessageException e) {

            // Check no new domain was inserted
            domainDaoChecker.checkDomainNotExist(getTestFixture().newDocumentToInsert());
            // Check the domain to update has not been updated
            domainDaoChecker.checkDomainUnmodified(getTestFixture().newExistingDocument());
        }
    }

    /**
     * Saving a list of domains inserts and updates the documents in the index.
     * For creation, a domain description should be inserted in the message's index type for the I18N domain's default language,
     * even if the input locale is defined with a different language.
     * For update, an error should occur when trying to insert a domain description for a missing language tag
     * when this language tag is not supported by the I18N domain.
     */
    @Test
    public void saveDomainsWithUnsupportedLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("de-DE"));
        try {
            saveDocuments();
            fail("UnsupportedLanguageException should be thrown.");
        }
        catch (UnsupportedLanguageException e) {

            // Check no new domain was inserted
            domainDaoChecker.checkDomainNotExist(getTestFixture().newDocumentToInsert());
            // Check the domain to update has not been updated
            domainDaoChecker.checkDomainUnmodified(getTestFixture().newExistingDocument());
        }
    }

    /**
     * Finding a missing domain by code returns {@code null}.
     */
    @Test
    public void findMissingDomainByCode() {
        assertThat(getRepository().findByCode(getTestFixture().newDocumentToInsert().getCode()), is(nullValue()));
    }

    /**
     * Finding an existing domain by code returns this domain.
     * Its description is localized for the default I18N domain's language.
     */
    @Test
    public void findExistingDomainByCode() {

        // Get domain fixture
        Domain domain = getTestFixture().newExistingDocument();

        Domain foundDomain = getRepository().findByCode(domain.getCode());
        assertThat(foundDomain, is(domain));
    }

    /**
     * Finding an existing domain by code for an existing supported language returns this domain.
     * Its description is localized for the specified supported language.
     */
    @Test
    public void findExistingDomainByCodeWithExistingSupportedLanguage() {

        // Get domain fixture with "fr" language
        threadLocaleHolder.setOutputLocale(Locale.forLanguageTag("fr"));
        Domain domain = getTestFixture().newExistingDocument();
        domain.setDescription(DOMAIN3_FR_DESCRIPTION);

        Domain foundDomain = getRepository().findByCode(domain.getCode());
        assertThat(foundDomain, is(domain));
    }

    /**
     * Finding an existing domain by code for a missing supported language returns this domain.
     * Its description is localized for the default I18N domain's language.
     */
    @Test
    public void findExistingDomainByCodeWithMissingSupportedLanguage() {

        // Get domain fixture with "es" language
        threadLocaleHolder.setOutputLocale(Locale.forLanguageTag("es"));
        Domain domain = getTestFixture().newExistingDocument();

        Domain foundDomain = getRepository().findByCode(domain.getCode());
        assertThat(foundDomain, is(domain));
    }

    /**
     * Finding an existing domain by code for a missing complex language (with an existing message for the simple language) returns this domain.
     * Its description is localized for the linked simple language.
     */
    @Test
    public void findExistingDomainByCodeWithComplexLanguageWithSimpleLanguage() {

        // Get domain fixture with "fr-BE" language
        threadLocaleHolder.setOutputLocale(Locale.forLanguageTag("fr-BE"));
        Domain domain = getTestFixture().newExistingDocument();
        domain.setDescription(DOMAIN3_FR_DESCRIPTION);

        Domain foundDomain = getRepository().findByCode(domain.getCode());
        assertThat(foundDomain, is(domain));
    }

    /**
     * Finding an existing domain by code for a missing complex language (without an existing message for the simple language) returns this domain.
     * Its description is localized for the default I18N domain's language.
     */
    @Test
    public void findExistingDomainByCodeWithComplexLanguageWithoutSimpleLanguage() {

        // Get domain fixture with "fr-BE" language
        threadLocaleHolder.setOutputLocale(Locale.forLanguageTag("es-ES"));
        Domain domain = getTestFixture().newExistingDocument();

        Domain foundDomain = getRepository().findByCode(domain.getCode());
        assertThat(foundDomain, is(domain));
    }

    /**
     * Finding an existing domain by code for an unsupported language returns this domain.
     * Its description is localized for the default I18N domain's language.
     */
    @Test
    public void findExistingDomainByCodeWithUnsupportedLanguage() {

        // Get domain fixture with "de-DE" language
        threadLocaleHolder.setOutputLocale(Locale.forLanguageTag("de-DE"));
        Domain domain = getTestFixture().newExistingDocument();

        Domain foundDomain = getRepository().findByCode(domain.getCode());
        assertThat(foundDomain, is(domain));
    }

    // TODO en-US case for many tests
    // TODO Other find* tests
}
