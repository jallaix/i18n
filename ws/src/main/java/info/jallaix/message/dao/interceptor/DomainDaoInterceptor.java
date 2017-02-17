package info.jallaix.message.dao.interceptor;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.dto.Domain;
import info.jallaix.message.dto.Message;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * This aspect intercepts Spring Data Elasticsearch operations to manage localized messages.
 * </p>
 * <p>
 * When creating domains, localized property values are replaced by message codes and original values are saved in the message index.
 * </p>
 * <p>
 * When updating domains, their descriptions are updated in the message index for the specified locale.
 * </p>
 * <p>
 * When finding domains, their descriptions are found in the message index for the specified locale.
 * </p>
 */
@Aspect
@Component
public class DomainDaoInterceptor {

    /**
     * Message type for the domain description
     */
    public static final String DOMAIN_DESCRIPTION_TYPE = Domain.class.getName() + ".description";

    /**
     * Application's internationalization data
     */
    @Autowired
    private DomainHolder i18nDomainHolder;

    /**
     * Elasticsearch operations
     */
    @Autowired
    private ElasticsearchOperations esOperations;

    /**
     * Holder for accessing locale data
     */
    @Autowired
    private ThreadLocaleHolder threadLocaleHolder;

    /**
     * Serialization framework
     */
    @Autowired
    private Kryo kryo;

    /**
     * Intercept entities before they are created to save localized messages in the index and replace matching property values by message codes.
     *
     * @param joinPoint      Joint point for the targeted operation
     * @param initialDomains The list of entities to create
     */
    @Around("execution(* org.springframework.data.elasticsearch.repository.ElasticsearchRepository+.save(Iterable,..)) && args(initialDomains,..)")
    public Object aroundSaving(final ProceedingJoinPoint joinPoint, final Iterable<Domain> initialDomains) throws Throwable {

        // Execute the default process if there are no entities to save
        if (initialDomains == null)
            return joinPoint.proceed(new Object[]{null});

        // Replace the domain description's literal value for each domain to save
        List<Pair<Domain, String>> descriptions = new ArrayList<>();
        Collection<Domain> domainsToSave = new ArrayList<>();
        for (Domain initialDomain : initialDomains) {

            if (initialDomain != null) {
                // Replace the domain description's literal value by a message type
                Pair<Domain, String> updatedDomainDescription = updateDescription(initialDomain);
                domainsToSave.add(updatedDomainDescription.getLeft());

                // Get the existing domain to update if it already exists
                final Domain existingDomain = findExistingDomain(initialDomain);
                descriptions.add(
                        new MutablePair<>(
                                existingDomain,
                                updatedDomainDescription.getRight()));
            } else
                domainsToSave.add(null);
        }

        // Call the save method
        Object result = joinPoint.proceed(new Object[]{domainsToSave});

        // Store domain description's literal values in the Message index
        // Replace the domain description's UUIDs by their matching literal values
        Iterable<?> resultEntities = Iterable.class.cast(result);
        int descriptionIndex = 0;
        for (Object resultEntity : resultEntities) {

            Pair<Domain, String> description = descriptions.get(descriptionIndex);
            descriptionIndex++;

            if (resultEntity != null) {
                Domain resultDomain = Domain.class.cast(resultEntity);

                // On creation, build and save the domain description's message for each language supported by the I18n Message domain
                // On update, save the localized description only
                if (description.getLeft() == null)
                    insertInitialMessages(resultDomain.getId(), description.getRight());
                else
                    updateExistingMessage(resultDomain.getId(), description.getRight());

                // Set back the domain description's literal value
                resultDomain.setDescription(description.getRight());
            }
        }

        return resultEntities;
    }

    /**
     * Intercept a domain saving operation before it's indexed to replace its description's value by a message code
     * and store its localized descriptions.
     *
     * @param joinPoint     Joint point for the targeted operation
     * @param initialDomain The domain to index
     */
    @Around("execution(* info.jallaix.message.dao.DomainDao+.save(Object)) && args(initialDomain)"
            + " || execution(* info.jallaix.message.dao.DomainDao+.index(Object)) && args(initialDomain)")
    public Object aroundSaving(final ProceedingJoinPoint joinPoint, final Domain initialDomain) throws Throwable {

        // Execute the default process if there is no entity to save
        if (initialDomain == null)
            return joinPoint.proceed(new Object[]{null});

        // Replace the domain description's literal value by a message type
        Pair<Domain, String> updatedDomainDescription = updateDescription(initialDomain);

        // Get the existing domain to update if it already exists
        final Domain existingDomain = findExistingDomain(initialDomain);

        // Call the save method and get the saved domain
        Object resultEntity = joinPoint.proceed(new Object[]{updatedDomainDescription.getLeft()});
        if (resultEntity == null)
            return null;
        Domain resultDomain = Domain.class.cast(resultEntity);

        // On creation, build and save the domain description's message for each language supported by the I18n Message domain
        // On update, save the localized description only
        if (existingDomain == null)
            insertInitialMessages(resultDomain.getId(), updatedDomainDescription.getRight());
        else
            updateExistingMessage(resultDomain.getId(), updatedDomainDescription.getRight());

        // Set back the localized domain description
        resultDomain.setDescription(updatedDomainDescription.getRight());

        return resultDomain;
    }

    /**
     * Intercept a domain finding operation after it is get to replace its message code by a description's value.
     *
     * @param foundDomain The found domain to update with the localized description
     */
    @AfterReturning(pointcut = "execution(* info.jallaix.message.dao.DomainDao+.findOne(*))", returning = "foundDomain")
    public void afterFindOne(Domain foundDomain) {

        if (foundDomain == null)
            return;

        Message message = searchMessage(
                i18nDomainHolder.getDomain().getId(),
                DOMAIN_DESCRIPTION_TYPE,
                foundDomain.getId(),
                getOutputLanguageTag());

        foundDomain.setDescription(message.getContent());
    }

    /**
     * Intercept domains finding operation after there are get to replace their message codes by their description's values.
     *
     * @param domains The list of found domains to update with the localized descriptions
     */
    @AfterReturning(pointcut = "execution(* info.jallaix.message.dao.DomainDao+.findAll(*))"
            + " || execution(* info.jallaix.message.dao.DomainDao+.findAll())", returning = "domains")
    public void afterFindAll(Iterable<?> domains) {

        if (domains == null)
            return;

        domains.forEach(domain -> afterFindOne(Domain.class.cast(domain)));
    }

    /**
     * Intercept a domain deletion operation after the deletion occurs to remove its linked messages.
     *
     * @param arg The identifier of the deleted domain
     */
    @AfterReturning("execution(* info.jallaix.message.dao.DomainDao+.delete(*)) && args(arg)")
    public void afterDelete(Object arg) {

        if (arg == null)
            return;

        String id = null;
        List<String> domainIds = null;
        if (arg instanceof String)
            id = String.class.cast(arg);
        else if (arg instanceof Domain)
            id = Domain.class.cast(arg).getId();
        else if (arg instanceof List) {
            List<Domain> domains = (List<Domain>) arg;
            domainIds = domains.stream().map(Domain::getId).collect(Collectors.toList());
        }

        if (id != null)
            deleteMessages(i18nDomainHolder.getDomain().getId(), id);
        else if (domainIds != null)
            deleteMessages(i18nDomainHolder.getDomain().getId(), domainIds);
    }

    /**
     * Intercept an all domain deletion operation after the deletion occurs to remove all linked messages.
     */
    @AfterReturning("execution(* info.jallaix.message.dao.DomainDao+.deleteAll())")
    public void afterDeleteAll() {
        deleteMessages(i18nDomainHolder.getDomain().getId());
    }

    /**
     * Replace the domain description's literal value by a message type.
     *
     * @param domain Domain for which the description must be replaced
     * @return The updated domain and the extracted description
     */
    private Pair<Domain, String> updateDescription(final Domain domain) {

        Domain domainToUpdate = kryo.copy(domain);
        final String descriptionContent = domainToUpdate.getDescription();
        domainToUpdate.setDescription(DOMAIN_DESCRIPTION_TYPE);

        return new ImmutablePair<>(domainToUpdate, descriptionContent);
    }

    /**
     * Find the existing domain to update if it exists.
     *
     * @param domainToSave The domain to save
     * @return The existing domain found or {@code null}
     */
    private Domain findExistingDomain(final Domain domainToSave) {

        final Domain existingDomain;
        if (domainToSave.getId() == null)
            existingDomain = null;
        else {
            GetQuery getQuery = new GetQuery();
            getQuery.setId(domainToSave.getId());
            existingDomain = esOperations.queryForObject(getQuery, Domain.class);
        }

        return existingDomain;
    }

    /**
     * Insert new messages in the index for all supported languages of the I18n Messages application.
     *
     * @param domainId           The new domain the messages depend on
     * @param descriptionContent The description content to set on messages
     */
    private void insertInitialMessages(final String domainId, final String descriptionContent) {

        i18nDomainHolder.getDomain().getAvailableLanguageTags().forEach(languageTag ->
                indexMessage(
                        buildMessage(languageTag, domainId, descriptionContent)));
    }

    /**
     * Update an existing message description for the current locale.
     *
     * @param domainId           The existing domain the message depends on
     * @param descriptionContent The description content to set on message
     */
    private void updateExistingMessage(final String domainId, final String descriptionContent) {

        Message messageToUpdate = searchMessage(
                i18nDomainHolder.getDomain().getId(),
                DOMAIN_DESCRIPTION_TYPE,
                domainId,
                getInputLanguageTag());

        messageToUpdate.setContent(descriptionContent);
        indexMessage(messageToUpdate);
    }

    /**
     * Get the language tag for input data.
     *
     * @return The language tag for input data
     */
    private String getInputLanguageTag() {

        return threadLocaleHolder.getInputLocale() == null ?
                i18nDomainHolder.getDomain().getDefaultLanguageTag() :
                threadLocaleHolder.getInputLocale().toLanguageTag();
    }

    /**
     * Get the language tag for output data.
     *
     * @return The language tag for output data
     */
    private String getOutputLanguageTag() {

        return threadLocaleHolder.getInputLocale() == null ?
                i18nDomainHolder.getDomain().getDefaultLanguageTag() :
                threadLocaleHolder.getOutputLocale().toLanguageTag();
    }

    /**
     * Build a message for the domain's description.
     *
     * @param languageTag        The language tag of the message
     * @param domainId           The domain identifier
     * @param descriptionContent The description content
     * @return The built message
     */
    private Message buildMessage(final String languageTag, final String domainId, final String descriptionContent) {

        Message message = new Message();

        // I18n message domain identifier
        message.setDomainId(i18nDomainHolder.getDomain().getId());
        // Message type
        message.setType(DOMAIN_DESCRIPTION_TYPE);
        // Domain identifier
        message.setEntityId(domainId);
        // Input language tag
        message.setLanguageTag(languageTag);
        // Localized content
        message.setContent(descriptionContent);

        return message;
    }

    /**
     * Look for the message that matches the arguments.
     *
     * @param i18nDomainId    Identifier of this application domain
     * @param descriptionType Type of the domain description
     * @param domainId        Identifier of the domain
     * @param languageTag     Language tag
     * @return The found message or {@code null}
     */
    private Message searchMessage(final String i18nDomainId, final String descriptionType, final String domainId, final String languageTag) {

        return esOperations.queryForObject(
                new CriteriaQuery(
                        new Criteria("domainId").is(i18nDomainId)
                                .and(new Criteria("type").is(descriptionType))
                                .and(new Criteria("entityId").is(domainId))
                                .and(new Criteria("languageTag").is(languageTag))),
                Message.class);
    }

    /**
     * Create or update a message.
     *
     * @param message the message to save
     */
    private void indexMessage(final Message message) {

        // Index the message
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setObject(message);
        indexQuery.setId(message.getId());
        esOperations.index(indexQuery);

        // Refresh the message (make it available for search)
        esOperations.refresh(Message.class.getDeclaredAnnotation(Document.class).indexName(), true);
    }

    /**
     * Delete all messages belonging to a domain.
     *
     * @param i18nDomainId Identifier of this application domain
     * @param domainId     Identifier of the domain
     */
    private void deleteMessages(final String i18nDomainId, final String domainId) {

        esOperations.delete(
                new CriteriaQuery(
                        new Criteria("domainId").is(i18nDomainId)
                                .and(new Criteria("entityId").is(domainId))),
                Message.class);
    }

    /**
     * Delete all messages.
     *
     * @param i18nDomainId Identifier of this application domain
     * @param domainIds    List of domain identifiers
     */
    private void deleteMessages(final String i18nDomainId, final Iterable<String> domainIds) {

        esOperations.delete(
                new CriteriaQuery(
                        new Criteria("domainId").is(i18nDomainId)
                                .and(new Criteria("entityId").in(domainIds))),
                Message.class);
    }

    /**
     * Delete all messages belonging to a list of domains.
     *
     * @param i18nDomainId Identifier of this application domain
     */
    private void deleteMessages(final String i18nDomainId) {
        esOperations.delete(new CriteriaQuery(new Criteria("domainId").is(i18nDomainId)), Message.class);
    }
}
