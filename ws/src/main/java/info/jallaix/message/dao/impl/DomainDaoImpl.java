package info.jallaix.message.dao.impl;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.bean.EntityMessage;
import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.dao.DomainDaoCustom;
import info.jallaix.message.dao.interceptor.MissingSimpleMessageException;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import info.jallaix.message.dao.interceptor.UnsupportedLanguageException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactory;
import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This class implements custom datasource accesses related to a domain.
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
@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
public class DomainDaoImpl implements DomainDaoCustom {

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
     * Default Elasticsearch repository
     */
    private SimpleElasticsearchRepository<Domain> esRepository;


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                           Custom repository operations                                         */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Find a domain by a code with localized description.
     * {@link ThreadLocaleHolder#getOutputLocales()} is used for language selection.
     *
     * @param code The domain code
     * @return The domain found
     */
    @Override
    public Domain findByCode(String code) {

        // Check the code is not null
        if (code == null) {
            ActionRequestValidationException e = new ActionRequestValidationException();
            e.addValidationError("code can't be null");
            throw e;
        }

        // Find domains by code
        List<Domain> domains = esOperations.queryForList(
                new NativeSearchQueryBuilder()
                        .withQuery(
                                QueryBuilders.constantScoreQuery(
                                        QueryBuilders.boolQuery()
                                                .must(QueryBuilders.termQuery(Domain.FIELD_CODE.getName(), code))))
                        .build(), Domain.class);

        // Check results
        if (domains.isEmpty())
            return null;
        else if (domains.size() > 1)
            throw new RuntimeException("At most one domain should be found given the criteria.");
        else
            return localizeDescription(domains.get(0));
    }

    /**
     * Find a domain by identifier with localized description.
     * {@link ThreadLocaleHolder#getOutputLocales()} is used for language selection.
     *
     * @param id The domain identifier
     * @return The domain found
     */
    public Domain findOne(String id) {
        return localizeDescription(getElasticsearchRepository().findOne(id));
    }

    /**
     * Find all domains with localized descriptions.
     * {@link ThreadLocaleHolder#getOutputLocales()} is used for language selection.
     *
     * @return The domains found
     */
    public Iterable<Domain> findAll() {
        return localizeDescriptions(getElasticsearchRepository().findAll());
    }

    /**
     * Find all domains by page with localized descriptions.
     * {@link ThreadLocaleHolder#getOutputLocales()} is used for language selection.
     *
     * @param pageable Page data
     * @return The paged domains found
     */
    public Page<Domain> findAll(Pageable pageable) {

        Page<Domain> page = getElasticsearchRepository().findAll(pageable);
        page.forEach(domain -> localizeDescription(Domain.class.cast(domain)));

        return page;
    }

    /**
     * Find all domains sorted with localized descriptions.
     * {@link ThreadLocaleHolder#getOutputLocales()} is used for language selection.
     *
     * @param sort Sort data
     * @return The domains found
     */
    @SuppressWarnings("unused")
    public Iterable<Domain> findAll(Sort sort) {
        return localizeDescriptions(getElasticsearchRepository().findAll(sort));
    }

    /**
     * Find domains by identifiers sorted with localized descriptions.
     * {@link ThreadLocaleHolder#getOutputLocales()} is used for language selection.
     *
     * @param ids Domain identifiers
     * @return The domains found
     */
    @SuppressWarnings("unused")
    public Iterable<Domain> findAll(Iterable<String> ids) {
        return localizeDescriptions(getElasticsearchRepository().findAll(ids));
    }

    /**
     * Save a domain and save its description in the message's index type.
     *
     * @param entity The domain to save
     * @return The domain saved
     */
    @SuppressWarnings("unused")
    public Domain save(Domain entity) {

        // Execute the default process if there is no entity to save
        if (entity == null)
            return getElasticsearchRepository().save((Domain) null);

        // Replace the domain description's literal value by a message code
        Pair<Domain, String> updatedDomainDescription = updateDescription(entity);

        // Get the existing domain to update if it already exists
        final Domain existingDomain = findExistingDomain(entity);

        // Detect locale errors on a domain update
        checkLocaleForDomainUpdate(existingDomain);

        // Save the domain
        Domain resultDomain = getElasticsearchRepository().save(updatedDomainDescription.getLeft());

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
     * Save a list of domains and save their descriptions in the message's index type.
     *
     * @param entities The domains to save
     * @return The domains saved
     */
    @SuppressWarnings("unused")
    public Iterable<Domain> save(Iterable<Domain> entities) {

        // Execute the default process if there are no entities to save
        if (entities == null)
            return getElasticsearchRepository().save((Iterable<Domain>) null);

        // Check if a domain to save is null
        if (StreamSupport.stream(entities.spliterator(), false)
                .collect(Collectors.toList()).contains(null)) {

            // Throw the default exception for saving null documents
            getElasticsearchRepository().save(Collections.singleton(null));
        }

        // Replace the domain description's literal value for each domain to save
        Collection<Domain> domainsToSave = new ArrayList<>();
        List<Pair<Domain, String>> descriptions = new ArrayList<>();
        for (Domain initialDomain : entities) {

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
        }

        // Save domains
        Iterable<Domain> resultEntities = getElasticsearchRepository().save(domainsToSave);

        // Save localized messages
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
     * Save a domain and save its description in the message's index type.
     *
     * @param entity The domain to save
     * @return The domain saved
     */
    @SuppressWarnings("unused")
    public Domain index(Domain entity) {
        return save(entity);
    }

    /**
     * Delete a domain and its localized description.
     *
     * @param id The domain identifier
     */
    @SuppressWarnings("unused")
    public void delete(String id) {

        getElasticsearchRepository().delete(id);
        deleteMessages(id);
    }

    /**
     * Delete a domain and its localized description.
     *
     * @param entity The domain to delete
     */
    @SuppressWarnings("unused")
    public void delete(Domain entity) {

        getElasticsearchRepository().delete(entity);
        if (entity != null)
            deleteMessages(entity.getId());
    }

    /**
     * Delete a list of domains and their localized descriptions.
     *
     * @param entities The domains to delete
     */
    @SuppressWarnings("unused")
    public void delete(Iterable<? extends Domain> entities) {

        getElasticsearchRepository().delete(entities);

        @SuppressWarnings("unchecked")
        List<Domain> domains = (List<Domain>) entities;
        List<String> domainIds = domains.stream().map(Domain::getId).collect(Collectors.toList());
        deleteMessages(domainIds);
    }

    /**
     * Delete all domains and their localized descriptions.
     */
    @SuppressWarnings("unused")
    public void deleteAll() {

        getElasticsearchRepository().deleteAll();
        deleteMessages();
    }



    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Private methods                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Get the default Elasticsearch repository implementation. See {@link SimpleElasticsearchRepository}.
     *
     * @return The the default Elasticsearch repository
     */
    private ElasticsearchRepository<Domain, String> getElasticsearchRepository() {

        if (esRepository == null)
            esRepository = new SimpleElasticsearchRepository<>(
                    new ElasticsearchRepositoryFactory(esOperations).getEntityInformation(Domain.class),
                    esOperations);

        return esRepository;
    }

    /**
     * Apply a localized message to a domain description.
     *
     * @param domain The un-localized domain
     * @return The localized domain
     */
    private Domain localizeDescription(Domain domain) {

        if (domain == null)
            return null;

        // Find the list of description messages for the found domain
        final List<EntityMessage> messages = findMessages(domain.getId());

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
        domain.setDescription(message.isPresent() ? message.get().getContent() : null);

        return domain;
    }

    /**
     * Apply a localized message to each description of a domain list..
     *
     * @param domains The list of un-localized domains
     * @return The localized domains
     */
    private Iterable<Domain> localizeDescriptions(Iterable<Domain> domains) {

        if (domains == null)
            return null;

        domains.forEach(domain -> localizeDescription(Domain.class.cast(domain)));

        return domains;
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
