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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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

    /**
     * Customize a list of typed documents.
     *
     * @param fixture The list of typed documents to customize
     * @return The list of customized typed documents
     */
    @Override
    protected List<Domain> customizeFixture(List<Domain> fixture) {
        return internationalizeDomains(kryo.copy(fixture));
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                     Custom tests                                               */
    /*----------------------------------------------------------------------------------------------------------------*/

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

    /**
     * Saving a new domain inserts the domain in the index.
     * It also inserts the domain description in the message's index type for each Message domain's supported languages.
     */
    @Test
    @Override
    public void saveNewDocument() {

        // Insert a new document in the index
        super.saveNewDocument();
        checkNewDocumentMessages();
    }

    /**
     * Indexing a new domain inserts the domain in the index.
     * It also inserts the domain description in the message's index type for each Message domain's supported languages.
     */
    @Test
    @Override
    public void indexNewDocument() {

        // Insert a new document in the index
        super.indexNewDocument();
        checkNewDocumentMessages();
    }

    /**
     * Saving an existing document replaces the document in the index.
     * It also updates the domain description for the default locale in the message's index type.
     */
    @Test
    @Override
    public void saveExistingDocument() {

        // Find messages linked to the domain to update
        List<Message> originalMessages = getMessages(newDocumentToUpdate());

        // Update an existing document in the index
        super.saveExistingDocument();

        // Check the integrity of domain and messages
        checkExistingDocumentMessages(Locale.forLanguageTag(i18nDomainHolder.getDomain().getDefaultLanguageTag()), originalMessages);
    }

    /**
     * Saving a list of documents inserts and updates the documents in the index.
     * On creation, it also inserts the domains descriptions in the message's index type for each Message domain's supported languages.
     * On update, it also updates the domain description for the default locale in the message's index type.
     */
    @Test
    @Override
    public void saveDocuments() {

        // Find messages linked to the domain to update
        List<Message> originalMessages = getMessages(newDocumentToUpdate());

        // Insert and update documents in the index
        super.saveDocuments();

        // Check the integrity of domain and messages
        checkNewDocumentMessages();
        checkExistingDocumentMessages(Locale.forLanguageTag(i18nDomainHolder.getDomain().getDefaultLanguageTag()), originalMessages);
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                Private methods                                                 */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Check if an existing domain description is updated for the specified locale only.
     */
    private void checkExistingDocumentMessages(Locale locale, List<Message> originalMessages) {

        // The domain description in the Elasticsearch index must contain a message code
        Domain updated = newDocumentToUpdate();
        GetQuery getQuery = new GetQuery();
        getQuery.setId(updated.getId());
        Domain savedDomain = esOperations.queryForObject(getQuery, Domain.class);
        assertNotEquals(updated.getDescription(), savedDomain.getDescription());

        // Get all localized messages for a message and domain codes
        List<Message> messages = getMessages(savedDomain);

        // The domain description for the input locale must match the one expected by the update operation
        Optional<Message> message = messages.stream().filter(m -> m.getLanguageTag().equals(locale.toLanguageTag())).findFirst();
        assertTrue(message.isPresent());
        assertEquals(updated.getDescription(), message.get().getContent());

        // The domain descriptions for locales other than the input locale must match the original descriptions
        messages.stream().filter(m -> !m.getLanguageTag().equals(locale.toLanguageTag()))
                .forEach(m -> assertTrue(originalMessages.contains(m)));
    }

    /**
     * Find messages linked to a domain
     *
     * @param domain The domain linked to the message
     * @return The set a messages
     */
    private List<Message> getMessages(Domain domain) {
        return esOperations.queryForList(
                new CriteriaQuery(
                        new Criteria("domainId").is(i18nDomainHolder.getDomain().getId())
                                .and(new Criteria("type").is(Domain.class.getName() + ".description"))
                                .and(new Criteria("entityId").is(domain.getId()))),
                Message.class);
    }

    /**
     * Check if a new domain description is indexed in the message type.
     */
    private void checkNewDocumentMessages() {

        Domain i18nDomain = i18nDomainHolder.getDomain();

        // The domain description in the Elasticsearch index must contain a message code
        Domain inserted = newDocumentToInsert();
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
     * Get the list of domains from the index. Each domain description is initialized with an internationalized message.
     *
     * @param initialList The initial list of domains
     * @return The list of domains
     */
    private List<Domain> internationalizeDomains(List<Domain> initialList) {

        for (Domain initial : initialList) {

            Optional<Message> message = getMessages(initial).stream().filter(m -> m.getLanguageTag().equals(i18nDomainHolder.getDomain().getDefaultLanguageTag())).findFirst();
            if (!message.isPresent())
                fail("Invalid fixture: No domain message for " + i18nDomainHolder.getDomain().getDefaultLanguageTag());
            initial.setDescription(message.get().getContent());
        }

        return initialList;
    }
}
