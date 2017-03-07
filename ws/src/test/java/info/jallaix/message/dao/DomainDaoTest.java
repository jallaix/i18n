package info.jallaix.message.dao;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.config.DomainDaoTestConfiguration;
import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.dao.interceptor.MissingSimpleMessageException;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import info.jallaix.message.dao.interceptor.UnsupportedLanguageException;
import info.jallaix.message.dto.Domain;
import info.jallaix.message.dto.Language;
import info.jallaix.message.dto.Message;
import info.jallaix.spring.data.es.test.SpringDataEsTestConfiguration;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
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
    /*                                                   Overriden methods                                            */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Constructor that defines the tests to pass
     */
    public DomainDaoTest() {
        super(/*DaoTestedMethod.Exist.class*/);
    }

    /**
     * Return a new document for insertion.
     *
     * @return A document that will be inserted
     */
    @Override
    protected Domain newDocumentToInsert() {
        return new Domain("5", "test.project4", "Test project 4's description", "es", Arrays.asList("en", "fr", "es"));
    }

    /**
     * Return a new document for update.
     *
     * @return A document that will update an existing one
     */
    @Override
    protected Domain newDocumentToUpdate() {

        return new Domain("3", "test.project2", "New project 2's description", "es", Arrays.asList("en", "fr", "es"));
    }

    /**
     * Return a new existing document.
     *
     * @return A document that exists
     */
    @Override
    protected Domain newExistingDocument() {

        return new Domain("3", "test.project2", "Test project 2's description", "fr", Arrays.asList("en", "fr", "es"));
    }

    /**
     * Return the sort field
     *
     * @return The sort field
     */
    @Override
    protected Field getSortField() {

        try {
            return Language.class.getDeclaredField("code");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the size of a page to get
     *
     * @return The size of a page to get
     */
    @Override
    protected int getPageSize() {
        return 2;
    }

    /**
     * Return a set of domains and messages that are sorted in the Elasticsearch index.
     *
     * @return A set of domains and messages
     */
    @Override
    protected List<?> getStoredDocuments() {

        List<Object> storedDocuments = new ArrayList<>(13);

        storedDocuments.add(new Domain("1", "i18n.message", "info.jallaix.message.dto.Domain.description", "en", Arrays.asList("en", "fr", "es")));
        storedDocuments.add(new Message(null, "1", "info.jallaix.message.dto.Domain.description", "1", "en", "Internationalized messages"));
        storedDocuments.add(new Message(null, "1", "info.jallaix.message.dto.Domain.description", "1", "en-US", "Internationalized messages (US)"));
        storedDocuments.add(new Message(null, "1", "info.jallaix.message.dto.Domain.description", "1", "fr", "Messages internationalisés"));

        storedDocuments.add(new Domain("2", "test.project1", "info.jallaix.message.dto.Domain.description", "en", Arrays.asList("en", "fr", "es")));
        storedDocuments.add(new Message(null, "1", "info.jallaix.message.dto.Domain.description", "2", "en", "Test project 1's description"));
        storedDocuments.add(new Message(null, "1", "info.jallaix.message.dto.Domain.description", "2", "fr", "Description du projet de test 1"));

        storedDocuments.add(new Domain("3", "test.project2", "info.jallaix.message.dto.Domain.description", "fr", Arrays.asList("en", "fr", "es")));
        storedDocuments.add(new Message(null, "1", "info.jallaix.message.dto.Domain.description", "3", "en", "Test project 2's description"));
        storedDocuments.add(new Message(null, "1", "info.jallaix.message.dto.Domain.description", "3", "fr", "Description du projet de test 2"));

        storedDocuments.add(new Domain("4", "test.project3", "info.jallaix.message.dto.Domain.description", "en", Arrays.asList("en", "fr", "es")));
        storedDocuments.add(new Message(null, "1", "info.jallaix.message.dto.Domain.description", "4", "en", "Test project 3's description"));
        storedDocuments.add(new Message(null, "1", "info.jallaix.message.dto.Domain.description", "4", "fr", "Description du projet de test 3"));

        return storedDocuments;
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                     Custom tests                                               */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Check the integrity of domain and messages.
     * A domain description should be inserted in the message's index type for the Message domain's default language.
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
        final List<Message> originalMessages = (List<Message>) customData;

        domainDaoChecks.checkExistingDocumentMessages(updated, threadLocaleHolder.getInputLocale(), originalMessages);
    }

    /**
     * Get the initial list of messages before saving some domains.
     *
     * @param toSave Documents to save
     * @return The custom data
     */
    protected Object getCustomDataOnSaveDocuments(List<Domain> toSave) {
        return domainDaoChecks.getMessages(newDocumentToUpdate().getId());
    }

    /**
     * Check the integrity of domains and messages.
     * On creation, it also inserts the domains descriptions in the message's index type for each Message domain's supported languages.
     * On update, it also updates the domain description for the default locale in the message's index type.
     *
     * @param toSave Domains to save
     * @param saved  Saved domains
     */
    protected void customizeSaveDocuments(List<Domain> toSave, List<Domain> saved, Object customData) {

        @SuppressWarnings("unchecked")
        final List<Message> originalMessages = (List<Message>) customData;

        domainDaoChecks.checkNewDocumentMessage(newDocumentToInsert());
        domainDaoChecks.checkExistingDocumentMessages(newDocumentToUpdate(), threadLocaleHolder.getInputLocale(), originalMessages);
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
     * Looking for a language in any domain is successful if the language code is used.
     */
    @Test
    public void languageIsUsed() {
        assertThat(getRepository().isLanguageUsed("en"), is(true));
    }

    /**
     * Looking for a language in any domain fails if the language code doesn't exist.
     */
    @Test
    public void languageIsNotUsed() {
        assertThat(getRepository().isLanguageUsed("es"), is(false));
    }

    /**
     * Indexing a new domain inserts the document in the index.
     * A domain description should be inserted in the message's index type for the I18N domain's default language,
     * even if the input locale is defined with a different language.
     */
    @Test
    public void indexNewDomainWithLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("fr"));
        indexNewDocument();
    }

    /**
     * Saving a new domain inserts the document in the index.
     * A domain description should be inserted in the message's index type for the I18N domain's default language,
     * even if the input locale is defined with a different language.
     */
    @Test
    public void saveNewDomainWithLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("fr"));
        saveNewDocument();
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
    @Test(expected = MissingSimpleMessageException.class)
    public void indexExistingDomainWithComplexLanguageWithoutSimpleLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("es-ES"));
        indexExistingDocument();
    }

    /**
     * Indexing an existing domain replaces the document in the index.
     * An error should occur when trying to insert a domain description for a missing language tag
     * when this language tag is not supported by the I18N domain.
     */
    @Test(expected = UnsupportedLanguageException.class)
    public void indexExistingDomainWithUnsupportedLanguage() {

        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("de-DE"));
        indexExistingDocument();
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
            domainDaoChecks.checkDomainUnmodified(newExistingDocument());
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
            domainDaoChecks.checkDomainUnmodified(newExistingDocument());
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
            domainDaoChecks.checkDomainNotExist(newDocumentToInsert());
            // Check the domain to update has not been updated
            domainDaoChecks.checkDomainUnmodified(newExistingDocument());
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
            domainDaoChecks.checkDomainNotExist(newDocumentToInsert());
            // Check the domain to update has not been updated
            domainDaoChecks.checkDomainUnmodified(newExistingDocument());
        }
    }
}
