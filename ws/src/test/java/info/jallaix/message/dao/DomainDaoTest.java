package info.jallaix.message.dao;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.config.DomainDaoTestConfiguration;
import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
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
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

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

    @Autowired
    private DomainHolder i18nDomainHolder;

    @Autowired
    private ElasticsearchOperations esOperations;

    @Autowired
    private ThreadLocaleHolder threadLocaleHolder;

    @Autowired
    private Kryo kryo;

    @Before
    public void initMessageDomain() {

    }

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

        return new Domain("5", "test.project4", "Test project 4's description", "es-ES", Arrays.asList("en-US", "fr-FR", "es-ES"));
    }

    /**
     * Return a new document for update.
     *
     * @return A document that will update an existing one
     */
    @Override
    protected Domain newDocumentToUpdate() {

        return new Domain("3", "test.project2", "New project 2's description", "es-ES", Arrays.asList("en-US", "fr-FR", "es-ES"));
    }

    /**
     * Return a new existing document.
     *
     * @return A document that exists
     */
    @Override
    protected Domain newExistingDocument() {

        return new Domain("3", "test.project2", "Test project 2's description", "fr-FR", Arrays.asList("en-US", "fr-FR", "es-ES"));
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

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                     Custom tests                                               */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Check the integrity of domain and messages.
     * Domains descriptions should be inserted in the message's index type for each Message domain's supported languages.
     *
     * @param toInsert Domain to insert
     * @param inserted Inserted domain
     */
    @Override
    protected void customizeSaveNewDocument(Domain toInsert, Domain inserted) {
        checkNewDocumentMessages(inserted);
    }

    /**
     * Get the initial list of messages before saving a domain.
     *
     * @param toUpdate Domain to update
     * @return The initial list of messages
     */
    protected Object getCustomDataOnSaveExistingDocument(Domain toUpdate) {
        return getMessages(toUpdate.getId());
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

        checkExistingDocumentMessages(updated, Locale.forLanguageTag(i18nDomainHolder.getDomain().getDefaultLanguageTag()), originalMessages);
    }

    /**
     * Get the initial list of messages before saving some domains.
     *
     * @param toSave Documents to save
     * @return The custom data
     */
    protected Object getCustomDataOnSaveDocuments(List<Domain> toSave) {
        return getMessages(newDocumentToUpdate().getId());
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

        checkNewDocumentMessages(newDocumentToInsert());
        checkExistingDocumentMessages(newDocumentToUpdate(), Locale.forLanguageTag(i18nDomainHolder.getDomain().getDefaultLanguageTag()), originalMessages);
    }

    /**
     * Internationalize domain descriptions for the {@link #findAllDocuments()} and so on tests.
     *
     * @param fixture The list of domains to internationalize
     * @return The list of internationalized domains
     */
    @Override
    protected List<Domain> customizeFindAllFixture(final List<Domain> fixture) {
        return internationalizeDomains(kryo.copy(fixture));
    }

    /**
     * Internationalize domain description for the {@link #findOneExistingDocument()} test.
     *
     * @param fixture The domain to internationalize
     * @return The internationalized domain
     */
    protected Domain customizeFindOneFixture(final Domain fixture) {
        return internationalizeDomain(kryo.copy(fixture));
    }

    /**
     * Check the integrity of domain and messages.
     * No message should reference any domain.
     */
    @Override
    protected void customizeDeleteAll() {
        assertThat(getMessages(), hasSize(0));
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
        assertThat(getMessages(domainIds), hasSize(0));
    }

    /**
     * Check the integrity of domain and messages.
     * No message should reference the deleted domain.
     *
     * @param id Identifier of the deleted domain
     */
    @Override
    protected void customizeDeleteOne(String id) {
        assertThat(getMessages(id), hasSize(0));
    }

    /**
     * Looking for a language in any domain is successful if the language code is used.
     */
    @Test
    public void languageIsUsed() {
        assertThat(getRepository().isLanguageUsed("en-US"), is(true));
    }

    /**
     * Looking for a language in any domain fails if the language code doesn't exist.
     */
    @Test
    public void languageIsNotUsed() {
        assertThat(getRepository().isLanguageUsed("es-ES"), is(false));
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                Private methods                                                 */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Find messages linked to a domain.
     *
     * @param domainId The identifier of the domain linked to the message
     * @return The set a messages
     */
    private List<Message> getMessages(String domainId) {
        return esOperations.queryForList(
                new CriteriaQuery(
                        new Criteria("domainId").is(i18nDomainHolder.getDomain().getId())
                                .and(new Criteria("type").is(Domain.class.getName() + ".description"))
                                .and(new Criteria("entityId").is(domainId))),
                Message.class);
    }

    /**
     * Find all messages linked to any domain.
     *
     * @return The set of messages
     */
    private List<Message> getMessages() {
        return esOperations.queryForList(
                new CriteriaQuery(
                        new Criteria("domainId").is(i18nDomainHolder.getDomain().getId())
                                .and(new Criteria("type").is(Domain.class.getName() + ".description"))),
                Message.class);
    }

    /**
     * Find messages linked to a list of domains.
     *
     * @param domainIds The list of domain identifiers linked to the messages
     * @return The set a messages
     */
    private List<Message> getMessages(Iterable<String> domainIds) {
        return esOperations.queryForList(
                new CriteriaQuery(
                        new Criteria("domainId").is(i18nDomainHolder.getDomain().getId())
                                .and(new Criteria("type").is(Domain.class.getName() + ".description"))
                                .and(new Criteria("entityId").in(domainIds))),
                Message.class);
    }

    /**
     * Check if an existing domain description is updated for the specified locale only.
     */
    private void checkExistingDocumentMessages(Domain updated, Locale locale, List<Message> originalMessages) {

        // The domain description in the Elasticsearch index must contain a message code
        GetQuery getQuery = new GetQuery();
        getQuery.setId(updated.getId());
        Domain savedDomain = esOperations.queryForObject(getQuery, Domain.class);
        assertNotEquals(updated.getDescription(), savedDomain.getDescription());

        // Get all localized messages for a message and domain codes
        List<Message> messages = getMessages(savedDomain.getId());

        // The domain description for the input locale must match the one expected by the update operation
        Optional<Message> message = messages.stream().filter(m -> m.getLanguageTag().equals(locale.toLanguageTag())).findFirst();
        assertTrue(message.isPresent());
        assertEquals(updated.getDescription(), message.get().getContent());

        // The domain descriptions for locales other than the input locale must match the original descriptions
        messages.stream().filter(m -> !m.getLanguageTag().equals(locale.toLanguageTag()))
                .forEach(m -> assertTrue(originalMessages.contains(m)));
    }

    /**
     * Check if a new domain description is indexed in the message type.
     *
     * @param inserted Inserted domain
     */
    private void checkNewDocumentMessages(Domain inserted) {

        Domain i18nDomain = i18nDomainHolder.getDomain();

        // The domain description in the Elasticsearch index must contain a message code
        GetQuery getQuery = new GetQuery();
        getQuery.setId(inserted.getId());
        Domain savedDomain = esOperations.queryForObject(getQuery, Domain.class);
        assertNotEquals(inserted.getDescription(), savedDomain.getDescription());

        // Get all localized messages for a message and domain codes
        List<Message> messages = esOperations.queryForList(
                new CriteriaQuery(
                        new Criteria("domainId").is(i18nDomain.getId())
                                .and(new Criteria("type").is(savedDomain.getDescription()))
                                .and(new Criteria("entityId").is(savedDomain.getId()))),
                Message.class);

        // Get the message domain to verify that all supported languages have a matching description
        i18nDomain.getAvailableLanguageTags().forEach(languageTag -> {

            // A message with the localized description must be linked to the message code
            Optional<Message> message = messages.stream().filter(m -> m.getLanguageTag().equals(languageTag)).findFirst();

            assertTrue(message.isPresent());
            assertEquals(inserted.getDescription(), message.get().getContent());
        });
    }

    /**
     * Initialize all domain descriptions with internationalized messages.
     *
     * @param initialList The initial list of domains
     * @return The list of domains
     */
    private List<Domain> internationalizeDomains(List<Domain> initialList) {

        List<Domain> results = new ArrayList<>();
        initialList.forEach(initial -> results.add(internationalizeDomain(initial)));

        return results;
    }

    /**
     * Initialize a domain description with an internationalized message.
     *
     * @param initial The initial domain
     * @return The internationalized domain
     */
    private Domain internationalizeDomain(final Domain initial) {

        Optional<Message> message = getMessages(initial.getId())
                .stream()
                .filter(m -> m.getLanguageTag().equals(i18nDomainHolder.getDomain().getDefaultLanguageTag())).findFirst();
        if (!message.isPresent())
            fail("Invalid fixture: No domain message for " + i18nDomainHolder.getDomain().getDefaultLanguageTag());

        Domain result = kryo.copy(initial);
        result.setDescription(message.get().getContent());

        return result;
    }
}
