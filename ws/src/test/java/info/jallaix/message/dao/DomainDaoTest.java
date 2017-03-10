package info.jallaix.message.dao;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.config.DomainDaoTestConfiguration;
import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.dao.interceptor.MissingSimpleMessageException;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import info.jallaix.message.dao.interceptor.UnsupportedLanguageException;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.message.bean.EntityMessage;
import info.jallaix.spring.data.es.test.SpringDataEsTestConfiguration;
import info.jallaix.spring.data.es.test.bean.BaseElasticsearchTestFixture;
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

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
    private DomainDaoChecks domainDaoChecks;


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Tests lifecycle                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

    @Before
    public void initMessageDomain() {
        domainDaoChecks = new DomainDaoChecks(i18nDomainHolder, esOperations, kryo);
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
    protected BaseElasticsearchTestFixture<Domain> getTestFixture() {
        return new DomainTestFixture();
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                     Custom tests                                               */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Check the integrity of domain and messages.
     * A domain description should be inserted in the message's index type for the I18N domain's default language.
     *
     * @param toInsert Domain to insert
     * @param inserted Inserted domain
     */
    @Override
    protected void customizeSaveNewDocument(Domain toInsert, Domain inserted) {
        domainDaoChecks.checkNewDocumentMessage(inserted);
    }

    /**
     * Get the initial list of messages before saving a domain.
     *
     * @param toUpdate Domain to update
     * @return The initial list of messages
     */
    protected Object getCustomDataOnSaveExistingDocument(Domain toUpdate) {
        return domainDaoChecks.getMessages(toUpdate.getId());
    }

    /**
     * Check the integrity of domain and messages.
     * Domain description for the default locale should be updated in the message's index type.
     *
     * @param toUpdate   Domain to update
     * @param updated    Updated domain
     * @param customData Custom data
     */
    protected void customizeSaveExistingDocument(Domain toUpdate, Domain updated, Object customData) {

        @SuppressWarnings("unchecked")
        final List<EntityMessage> originalMessages = (List<EntityMessage>) customData;

        domainDaoChecks.checkExistingDocumentMessages(updated, threadLocaleHolder.getInputLocale(), originalMessages);
    }

    /**
     * Get the initial list of messages before saving some domains.
     *
     * @param toSave Documents to save
     * @return The custom data
     */
    protected Object getCustomDataOnSaveDocuments(List<Domain> toSave) {
        return domainDaoChecks.getMessages(getTestFixture().newDocumentToUpdate().getId());
    }

    /**
     * Check the integrity of domains and messages.
     * On creation, it also inserts the domains descriptions in the message's index type for each I18N domain's supported languages.
     * On update, it also updates the domain description for the default locale in the message's index type.
     *
     * @param toSave Domains to save
     * @param saved  Saved domains
     */
    protected void customizeSaveDocuments(List<Domain> toSave, List<Domain> saved, Object customData) {

        @SuppressWarnings("unchecked")
        final List<EntityMessage> originalMessages = (List<EntityMessage>) customData;

        domainDaoChecks.checkNewDocumentMessage(getTestFixture().newDocumentToInsert());
        domainDaoChecks.checkExistingDocumentMessages(getTestFixture().newDocumentToUpdate(), threadLocaleHolder.getInputLocale(), originalMessages);
    }

    /**
     * Internationalize domain descriptions for the {@link #findAllDocuments()} and so on tests.
     *
     * @param fixture The list of domains to internationalize
     * @return The list of internationalized domains
     */
    @Override
    protected List<Domain> customizeFindAllFixture(final List<Domain> fixture) {
        return domainDaoChecks.internationalizeDomains(kryo.copy(fixture), "en");
    }

    /**
     * Internationalize domain description for the {@link #findOneExistingDocument()} test.
     *
     * @param fixture The domain to internationalize
     * @return The internationalized domain
     */
    protected Domain customizeFindOneFixture(final Domain fixture) {
        return domainDaoChecks.internationalizeDomain(kryo.copy(fixture), "en");
    }

    /**
     * Check the integrity of domain and messages.
     * No message should reference any domain.
     */
    @Override
    protected void customizeDeleteAll() {
        assertThat(domainDaoChecks.getMessages(), hasSize(0));
    }

    /**
     * Check the integrity of domain and messages.
     * No message should reference the deleted domains.
     *
     * @param toDelete List of domains to delete
     */
    @Override
    protected void customizeDeleteSet(List<Domain> toDelete) {

        List<String> domainIds = toDelete.stream().map(Domain::getId).collect(Collectors.toList());
        assertThat(domainDaoChecks.getMessages(domainIds), hasSize(0));
    }

    /**
     * Check the integrity of domain and messages.
     * No message should reference the deleted domain.
     *
     * @param id Identifier of the deleted domain
     */
    @Override
    protected void customizeDeleteOne(String id) {
        assertThat(domainDaoChecks.getMessages(id), hasSize(0));
    }

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
            domainDaoChecks.checkDomainUnmodified(getTestFixture().newExistingDocument());
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
            domainDaoChecks.checkDomainUnmodified(getTestFixture().newExistingDocument());
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
            domainDaoChecks.checkDomainUnmodified(getTestFixture().newExistingDocument());
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
            domainDaoChecks.checkDomainUnmodified(getTestFixture().newExistingDocument());
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
            domainDaoChecks.checkDomainNotExist(getTestFixture().newDocumentToInsert());
            // Check the domain to update has not been updated
            domainDaoChecks.checkDomainUnmodified(getTestFixture().newExistingDocument());
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
            domainDaoChecks.checkDomainNotExist(getTestFixture().newDocumentToInsert());
            // Check the domain to update has not been updated
            domainDaoChecks.checkDomainUnmodified(getTestFixture().newExistingDocument());
        }
    }
}
