package info.jallaix.message.dao;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.dto.Domain;
import info.jallaix.message.dto.Message;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.GetQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Operation checks performed by {@link DomainDao} tests.
 */
public class DomainDaoChecks {

    /**
     * I18n domain
     */
    private DomainHolder i18nDomainHolder;

    /**
     * Elasticsearch operations
     */
    private ElasticsearchOperations esOperations;

    /**
     * Serialization framework
     */
    private Kryo kryo;


    public DomainDaoChecks(DomainHolder i18nDomainHolder, ElasticsearchOperations esOperations, Kryo kryo) {
        this.i18nDomainHolder = i18nDomainHolder;
        this.esOperations = esOperations;
        this.kryo = kryo;
    }

    /**
     * Find messages linked to a domain.
     *
     * @param domainId The identifier of the domain linked to the message
     * @return The set a messages
     */
    public List<Message> getMessages(String domainId) {
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
    public List<Message> getMessages() {
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
    public List<Message> getMessages(Iterable<String> domainIds) {
        return esOperations.queryForList(
                new CriteriaQuery(
                        new Criteria("domainId").is(i18nDomainHolder.getDomain().getId())
                                .and(new Criteria("type").is(Domain.class.getName() + ".description"))
                                .and(new Criteria("entityId").in(domainIds))),
                Message.class);
    }

    /**
     * Check if an existing domain description is updated for the specified locale only.
     * An error occurs if no domain description exists for a basic language whereas a regionalized language is provided.
     */
    public void checkExistingDocumentMessages(Domain updated, Locale locale, List<Message> originalMessages) {

        // The domain description in the Elasticsearch index must contain a message code
        GetQuery getQuery = new GetQuery();
        getQuery.setId(updated.getId());
        Domain savedDomain = esOperations.queryForObject(getQuery, Domain.class);
        assertNotEquals(updated.getDescription(), savedDomain.getDescription());

        // Get all localized messages for message and domain codes
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
    public void checkNewDocumentMessage(Domain inserted) {

        Domain i18nDomain = i18nDomainHolder.getDomain();

        // The domain description in the Elasticsearch index must contain a message code
        GetQuery getQuery = new GetQuery();
        getQuery.setId(inserted.getId());
        Domain savedDomain = esOperations.queryForObject(getQuery, Domain.class);
        assertNotEquals(inserted.getDescription(), savedDomain.getDescription());

        // Get all localized messages for message and domain codes
        List<Message> messages = esOperations.queryForList(
                new CriteriaQuery(
                        new Criteria("domainId").is(i18nDomain.getId())
                                .and(new Criteria("type").is(savedDomain.getDescription()))
                                .and(new Criteria("entityId").is(savedDomain.getId()))),
                Message.class);

        // Only one message for the default language tag should exist
        assertThat(messages, hasSize(1));
        Optional<Message> message = messages.stream().filter(m -> m.getLanguageTag().equals(i18nDomain.getDefaultLanguageTag())).findFirst();
        assertThat(message.isPresent(), is(true));
    }

    /**
     * Check that an existing domain has not been modified in the Elasticsearch index.
     *
     * @param domain The existing domain to check
     */
    public void checkDomainUnmodified(final Domain domain) {

        GetQuery getQuery = new GetQuery();
        getQuery.setId(domain.getId());
        Domain savedDomain = esOperations.queryForObject(getQuery, Domain.class);

        // Domain description is a message code in the index => do not compare them
        Domain originalDomain = kryo.copy(domain);
        originalDomain.setDescription(savedDomain.getDescription());

        assertThat(savedDomain, is(originalDomain));
    }

    /**
     * Check that a domain doesn't exist in the Elasticsearch index.
     *
     * @param domain The domain that should not exist
     */
    public void checkDomainNotExist(final Domain domain) {

        GetQuery getQuery = new GetQuery();
        getQuery.setId(domain.getId());
        Domain savedDomain = esOperations.queryForObject(getQuery, Domain.class);

        assertThat(savedDomain, is(nullValue()));
    }

    /**
     * Initialize all domain descriptions with internationalized messages.
     *
     * @param initialList The initial list of domains
     * @param languageTag The language tag for the internationalized message
     * @return The list of domains
     */
    public List<Domain> internationalizeDomains(List<Domain> initialList, final String languageTag) {

        List<Domain> results = new ArrayList<>();
        initialList.forEach(initial -> results.add(internationalizeDomain(initial, languageTag)));

        return results;
    }

    /**
     * Initialize a domain description with an internationalized message.
     *
     * @param initial     The initial domain
     * @param languageTag The language tag for the internationalized message
     * @return The internationalized domain
     */
    public Domain internationalizeDomain(final Domain initial, final String languageTag) {

        Optional<Message> message = getMessages(initial.getId())
                .stream()
                .filter(m -> m.getLanguageTag().equals(languageTag)).findFirst();
        if (!message.isPresent())
            fail("Invalid fixture: No domain message for " + languageTag);

        Domain result = kryo.copy(initial);
        result.setDescription(message.get().getContent());

        return result;
    }
}
