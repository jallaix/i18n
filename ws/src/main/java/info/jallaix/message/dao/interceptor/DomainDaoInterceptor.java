package info.jallaix.message.dao.interceptor;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.bean.EntityMessage;
import info.jallaix.message.dao.impl.EntityMessageDaoImpl;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * This aspect intercepts Spring Data Elasticsearch operations to manage localized messages.
 * </p>
 * <ul>
 * <li>
 * When creating domains, localized property values are replaced by message codes and original values are saved in the message index.
 * </li>
 * <li>
 * When updating domains, their descriptions are updated in the message index for the specified locale.
 * </li>
 * <li>
 * When finding domains, their descriptions are found in the message index for the specified locale.
 * </li>
 * <li>
 * When deleting domains, their descriptions found in the message index are also deleted.
 * </li>
 * </ul>
 */
@Aspect
@Component
public class DomainDaoInterceptor {

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


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                     Interceptors                                               */
    /*----------------------------------------------------------------------------------------------------------------*/

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

                // Detect locale errors on a domain update
                checkLocaleForDomainUpdate(existingDomain);

                descriptions.add(
                        new MutablePair<>(
                                existingDomain,
                                updatedDomainDescription.getRight()));
            } else
                domainsToSave.add(null);
        }

        // Call the save method
        Object result = joinPoint.proceed(new Object[]{domainsToSave});

        // Store domain description's literal values in the I18N index
        // Replace the domain description's UUIDs by their matching literal values
        Iterable<?> resultEntities = Iterable.class.cast(result);
        int descriptionIndex = 0;
        for (Object resultEntity : resultEntities) {

            Pair<Domain, String> description = descriptions.get(descriptionIndex);
            descriptionIndex++;

            if (resultEntity != null) {
                Domain resultDomain = Domain.class.cast(resultEntity);

                // On creation, build and save the domain description's message for each language supported by the I18N domain
                // On update, save the localized description only
                if (description.getLeft() == null)
                    insertInitialMessage(resultDomain.getId(), description.getRight());
                else
                    insertOrUpdateMessage(resultDomain.getId(), description.getRight());

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

        // Detect locale errors on a domain update
        checkLocaleForDomainUpdate(existingDomain);

        // Call the save method and get the saved domain
        Object resultEntity = joinPoint.proceed(new Object[]{updatedDomainDescription.getLeft()});
        if (resultEntity == null)
            return null;
        Domain resultDomain = Domain.class.cast(resultEntity);

        // On creation, build and save the domain description's message for each language supported by the I18n domain
        // On update, save the localized description only
        if (existingDomain == null)
            insertInitialMessage(resultDomain.getId(), updatedDomainDescription.getRight());
        else
            insertOrUpdateMessage(resultDomain.getId(), updatedDomainDescription.getRight());

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

        // Find the list of description messages for the found domain
        final List<EntityMessage> messages = findMessages(foundDomain.getId());

        // Get the list of available language tags in the message list
        Collection<Locale> existingLocales = messages.stream()
                .map(EntityMessage::getLanguageTag)
                .map(Locale::forLanguageTag)
                .collect(Collectors.toList());

        // Get the best matching language tag
        final Locale lookupLocale = Locale.lookup(threadLocaleHolder.getOutputLocales(), existingLocales);
        final String lookupTag =
                (lookupLocale == null) ?
                        i18nDomainHolder.getDomain().getDefaultLanguageTag() :
                        lookupLocale.toLanguageTag();

        // Set the domain description for the lookup language tag
        final Optional<EntityMessage> message = messages.stream()
                .filter(m -> lookupTag.equals(m.getLanguageTag()))
                .findFirst();
        foundDomain.setDescription(message.isPresent() ? message.get().getContent() : null);
    }

    /**
     * Intercept a domain finding operation by code after it is get to replace its message code by a description's value.
     *
     * @param joinPoint Joint point for the targeted operation
     * @param code      Code of the domain to find
     */
    @Around("execution(* info.jallaix.message.dao.DomainDao+.findByCode(String)) && args(code)")
    public Object aroundFindByCode(final ProceedingJoinPoint joinPoint, final String code) throws Throwable {

        // Check the code is not null
        if (code == null) {
            ActionRequestValidationException e = new ActionRequestValidationException();
            e.addValidationError("code can't be null");
            throw e;
        }

        // Call the find operation
        Domain foundDomain = Domain.class.cast(joinPoint.proceed());

        // Replace the domain's message code by a description's value.
        afterFindOne(foundDomain);

        return foundDomain;
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

        // Case of a String argument => map to an identifier
        if (arg instanceof String) {
            id = String.class.cast(arg);
        }
        // Case of a Domain argument => get identifier from the domain
        else if (arg instanceof Domain) {
            id = Domain.class.cast(arg).getId();
        }
        // Case of a List argument => get identifiers from the set of domains
        else if (arg instanceof List) {
            @SuppressWarnings("unchecked")
            List<Domain> domains = (List<Domain>) arg;
            domainIds = domains.stream().map(Domain::getId).collect(Collectors.toList());
        }

        // Delete messages by a domain identifier
        if (id != null) {
            deleteMessages(id);
        }
        // Delete messages by a set of domain identifiers
        else if (domainIds != null) {
            deleteMessages(domainIds);
        }
    }

    /**
     * Intercept a deletion operation for all domains after the deletion occurs to remove all linked messages.
     */
    @AfterReturning("execution(* info.jallaix.message.dao.DomainDao+.deleteAll())")
    public void afterDeleteAll() {
        deleteMessages();
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Private methods                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Replace the domain description's literal value by a message type.
     *
     * @param domain Domain for which the description must be replaced
     * @return The updated domain and the extracted description
     */
    private Pair<Domain, String> updateDescription(final Domain domain) {

        Domain domainToUpdate = kryo.copy(domain);
        final String descriptionContent = domainToUpdate.getDescription();
        domainToUpdate.setDescription(Domain.DOMAIN_DESCRIPTION_TYPE);

        return new ImmutablePair<>(domainToUpdate, descriptionContent);
    }

    /**
     * Find an existing domain.
     *
     * @param domain The domain with an identifier
     * @return The existing domain found or {@code null}
     */
    private Domain findExistingDomain(final Domain domain) {

        final Domain existingDomain;
        if (domain.getId() == null)
            existingDomain = null;
        else {
            GetQuery getQuery = new GetQuery();
            getQuery.setId(domain.getId());
            existingDomain = esOperations.queryForObject(getQuery, Domain.class);
        }

        return existingDomain;
    }

    /**
     * Insert a new message in the index for the default language of the I18N domain.
     *
     * @param domainId           The domain identifier the message depend on
     * @param descriptionContent The description content to set on the message
     */
    private void insertInitialMessage(final String domainId, final String descriptionContent) {

        indexMessage(
                buildMessage(
                        i18nDomainHolder.getDomain().getDefaultLanguageTag(),
                        domainId,
                        descriptionContent));
    }

    /**
     * <p>Insert a message if it doesn't exist for the input locale, else update the existing message.</p>
     * <p>A complex input locale (with data other than language) may be inserted only if a message already exists
     * for the simple language.</p>
     *
     * @param domainId           The existing domain identifier the message depends on
     * @param descriptionContent The description content to set on the message
     */
    private void insertOrUpdateMessage(final String domainId, final String descriptionContent) {

        // Get the message for the input locale
        EntityMessage messageForInputLocale = getDomainDescriptionForInputLocale(domainId);

        // Insert a message for the input locale
        if (messageForInputLocale == null) {
            indexMessage(
                    buildMessage(
                            threadLocaleHolder.getInputLocale().toLanguageTag(),
                            domainId,
                            descriptionContent));
        }
        // Update message for the input locale
        else {
            messageForInputLocale.setContent(descriptionContent);
            indexMessage(messageForInputLocale);
        }
    }

    /**
     * <p>Check an input locale is supported by the I18N domain.</p>
     * <p>Check a message already exists for a simple language if an input locale has a complex language tag.</p>
     *
     * @param domain The domain to update
     */
    private void checkLocaleForDomainUpdate(Domain domain) {

        // Nothing to check on a null domain
        if (domain == null)
            return;

        // Check the locale is supported by the I18N domain
        Locale inputLocale = threadLocaleHolder.getInputLocale();
        checkSupportedLocale(inputLocale);

        // No message for the input locale
        if (getDomainDescriptionForInputLocale(domain.getId()) == null) {

            // Error when the input locale has a complex language tag and no message already exists for the simple language
            if (!hasInputLocaleSimpleLanguage() && !existDomainDescriptionForSimpleLanguage(domain.getId()))
                throw new MissingSimpleMessageException(inputLocale, domain.getId());
        }
    }

    /**
     * Check that a local is supported by the I18N domain.
     *
     * @param locale The locale to test
     * @throws UnsupportedLanguageException In case the locale is not supported by the domain
     */
    private void checkSupportedLocale(Locale locale) throws UnsupportedLanguageException {

        if (!i18nDomainHolder.getDomain().getAvailableLanguageTags().contains(locale.getLanguage()))
            throw new UnsupportedLanguageException(locale, i18nDomainHolder.getDomain().getId());
    }

    /**
     * Get the domain description's message for the input locale.
     *
     * @param domainId The domain identifier
     * @return The found message for the domain description or {@code null}
     */
    private EntityMessage getDomainDescriptionForInputLocale(final String domainId) {

        return findMessage(
                domainId,
                threadLocaleHolder.getInputLocale().toLanguageTag());
    }

    /**
     * Indicate if the input locale just contains a language or also other data (country, script, ...).
     *
     * @return {@code true} if the input locale just contains a language else {@code false}
     */
    private boolean hasInputLocaleSimpleLanguage() {

        final Locale inputLocale = threadLocaleHolder.getInputLocale();

        return inputLocale.toLanguageTag().equals(inputLocale.getLanguage());
    }

    /**
     * Check if a domain description exists for the language of the input locale.
     *
     * @param domainId The domain identifier
     * @return {@code true} if the domain description exists else {@code false}
     */
    private boolean existDomainDescriptionForSimpleLanguage(final String domainId) {

        return findMessage(
                domainId,
                threadLocaleHolder.getInputLocale().getLanguage()) != null;
    }

    /**
     * Build a message for the domain's description.
     *
     * @param languageTag        The language tag of the message
     * @param domainId           The domain identifier
     * @param descriptionContent The description content
     * @return The built message
     */
    private EntityMessage buildMessage(final String languageTag, final String domainId, final String descriptionContent) {

        EntityMessage message = new EntityMessage();

        // I18n message domain identifier
        message.setDomainId(i18nDomainHolder.getDomain().getId());
        // Message type
        message.setType(Domain.DOMAIN_DESCRIPTION_TYPE);
        // Domain identifier
        message.setEntityId(domainId);
        // Input language tag
        message.setLanguageTag(languageTag);
        // Localized content
        message.setContent(descriptionContent);

        return message;
    }

    /**
     * Find the message for a domain description that matches a domain identifier and a language tag.
     *
     * @param domainId    Identifier of the domain
     * @param languageTag Language tag
     * @return The found message or {@code null}
     */
    private EntityMessage findMessage(final String domainId, final String languageTag) {

        return new EntityMessageDaoImpl(esOperations).findOne(
                i18nDomainHolder.getDomain().getId(),
                Domain.DOMAIN_DESCRIPTION_TYPE,
                domainId,
                languageTag
        );
    }

    /**
     * Find the list of messages for a domain description that matches a domain identifier.
     *
     * @param domainId Identifier of the domain
     * @return The found list of messages
     */
    private List<EntityMessage> findMessages(final String domainId) {

        return esOperations.queryForList(
                new NativeSearchQueryBuilder()
                        .withQuery(
                                QueryBuilders.constantScoreQuery(
                                        QueryBuilders.boolQuery()
                                                .must(QueryBuilders.termQuery(EntityMessage.FIELD_DOMAIN_ID.getName(), i18nDomainHolder.getDomain().getId()))
                                                .must(QueryBuilders.termQuery(EntityMessage.FIELD_TYPE.getName(), Domain.DOMAIN_DESCRIPTION_TYPE))
                                                .must(QueryBuilders.termQuery(EntityMessage.FIELD_ENTITY_ID.getName(), domainId))))
                        .build(), EntityMessage.class);
    }

    /**
     * Create or update a message.
     *
     * @param message the message to save
     */
    private void indexMessage(final EntityMessage message) {

        // Index the message
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setObject(message);
        indexQuery.setId(message.getId());
        esOperations.index(indexQuery);

        // Refresh the message (make it available for search). Write down that the refresh(Class<?>) version doesn't work.
        esOperations.refresh(EntityMessage.class.getDeclaredAnnotation(Document.class).indexName(), true);
    }

    /**
     * Delete all messages belonging to a domain.
     *
     * @param domainId Identifier of the domain
     */
    private void deleteMessages(final String domainId) {

        deleteMessages(
                QueryBuilders.constantScoreQuery(
                        QueryBuilders.boolQuery()
                                .must(QueryBuilders.termQuery(EntityMessage.FIELD_DOMAIN_ID.getName(), i18nDomainHolder.getDomain().getId()))
                                .must(QueryBuilders.termQuery(EntityMessage.FIELD_ENTITY_ID.getName(), domainId))));
    }

    /**
     * Delete all messages belonging to a set of domains.
     *
     * @param domainIds Collection of domain identifiers
     */
    private void deleteMessages(final Collection<String> domainIds) {

        deleteMessages(
                QueryBuilders.constantScoreQuery(
                        QueryBuilders.boolQuery()
                                .must(QueryBuilders.termQuery(EntityMessage.FIELD_DOMAIN_ID.getName(), i18nDomainHolder.getDomain().getId()))
                                .must(QueryBuilders.termsQuery(EntityMessage.FIELD_ENTITY_ID.getName(), domainIds))));
    }

    /**
     * Delete all domain messages.
     */
    private void deleteMessages() {

        deleteMessages(
                QueryBuilders.constantScoreQuery(
                        QueryBuilders.boolQuery()
                                .must(QueryBuilders.termQuery(EntityMessage.FIELD_DOMAIN_ID.getName(), i18nDomainHolder.getDomain().getId()))));
    }

    /**
     * Delete messages depending on a query builder.
     *
     * @param queryBuilder The query builder
     */
    private void deleteMessages(QueryBuilder queryBuilder) {

        DeleteQuery deleteQuery = new DeleteQuery();
        deleteQuery.setQuery(queryBuilder);
        esOperations.delete(deleteQuery, EntityMessage.class);
    }
}
