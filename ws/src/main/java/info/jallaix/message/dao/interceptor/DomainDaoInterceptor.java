package info.jallaix.message.dao.interceptor;

import info.jallaix.message.config.DomainHolder;
import info.jallaix.message.dao.MessageDao;
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
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This aspect intercepts Spring Data Elasticsearch operations to manage localized messages.
 * </p>
 * <p>
 * When creating entities, localized property values are replaced by message codes and original values are saved in the message index.
 * </p>
 */
@Aspect
@Component
public class DomainDaoInterceptor {

    public static final String DOMAIN_DESCRIPTION_TYPE = Domain.class.getName() + ".description";

    @Autowired
    private DomainHolder i18nDomainHolder;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private ElasticsearchOperations esOperations;

    @Autowired
    private ThreadLocaleHolder threadLocaleHolder;


    /**
     * Intercept entities before they are created to save localized messages in the index and replace matching property values by message codes.
     *
     * @param joinPoint      Joint point for the targeted operation
     * @param initialDomains The list of entities to create
     */
    @Around("execution(* org.springframework.data.elasticsearch.repository.ElasticsearchRepository+.save(Iterable,..)) && args(initialDomains,..)")
    public Object aroundSaving(ProceedingJoinPoint joinPoint, Iterable<Domain> initialDomains) throws Throwable {

        // Execute the default process if there are no entities to save
        if (initialDomains == null)
            return joinPoint.proceed(new Object[]{null});

        // Replace the domain description's literal value for each domain to save
        List<Pair<Domain, String>> descriptions = new ArrayList<>();
        for (Domain initialDomain : initialDomains) {

            if (initialDomain != null) {
                // Replace the domain description's literal value by a message type
                Pair<Domain, String> updatedDomainDescription = updateDescription(initialDomain);

                // Get the existing domain to update if it already exists
                final Domain existingDomain = findExistingDomain(initialDomain);
                descriptions.add(
                        new MutablePair<>(
                                existingDomain,
                                updatedDomainDescription.getRight()));
            }
        }

        // Call the save method
        Object result = joinPoint.proceed(new Object[]{initialDomains});

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
    public Object aroundSaving(ProceedingJoinPoint joinPoint, Domain initialDomain) throws Throwable {

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

    @AfterReturning(pointcut = "execution(* info.jallaix.message.dao.DomainDao+.findOne(*))", returning = "foundDomain")
    public void afterFindOne(Domain foundDomain) {

        if (foundDomain == null)
            return;

        Domain i18nDomain = i18nDomainHolder.getDomain();
        final String languageTag = threadLocaleHolder.getInputLocale() == null ?
                i18nDomain.getDefaultLanguageTag() :
                threadLocaleHolder.getOutputLocale().getLanguage();

        Message message = messageDao.findByDomainIdAndTypeAndEntityIdAndLanguageTag(
                i18nDomainHolder.getDomain().getId(),
                DOMAIN_DESCRIPTION_TYPE,
                foundDomain.getId(),
                languageTag);

        foundDomain.setDescription(message.getContent());
    }

    /**
     * Replace the domain description's literal value by a message type
     *
     * @param domain Domain on which the description must be replaced
     * @return The updated domain and the extracted description
     */
    private Pair<Domain, String> updateDescription(Domain domain) {

        final String descriptionContent = domain.getDescription();
        domain.setDescription(DOMAIN_DESCRIPTION_TYPE);

        return new ImmutablePair<>(domain, descriptionContent);
    }

    /**
     * Find the existing domain to update if it exists
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
     * Insert new messages in the index for all supported languages of the I18n Messages application
     *
     * @param domainId           The new domain the messages depend on
     * @param descriptionContent The description content to set on messages
     */
    private void insertInitialMessages(String domainId, String descriptionContent) {

        i18nDomainHolder.getDomain().getAvailableLanguageTags().forEach(languageTag ->
                messageDao.index(
                        buildMessage(languageTag, domainId, descriptionContent)));
    }

    /**
     * Update an existing message description for the current locale
     *
     * @param domainId           The existing domain the message depends on
     * @param descriptionContent The description content to set on message
     */
    private void updateExistingMessage(String domainId, String descriptionContent) {

        Domain i18nDomain = i18nDomainHolder.getDomain();
        final String languageTag = threadLocaleHolder.getInputLocale() == null ?
                i18nDomain.getDefaultLanguageTag() :
                threadLocaleHolder.getInputLocale().getLanguage();
        Message messageToUpdate = messageDao.findByDomainIdAndTypeAndEntityIdAndLanguageTag(
                i18nDomain.getId(),
                DOMAIN_DESCRIPTION_TYPE,
                domainId,
                languageTag);
        messageToUpdate.setContent(descriptionContent);
        messageDao.index(messageToUpdate);
    }

    /**
     * Build a message for the domain's description.
     *
     * @return The built message
     */
    private Message buildMessage(String languageTag, String descriptionId, String descriptionContent) {

        Message message = new Message();

        // I18n message domain identifier
        message.setDomainId(i18nDomainHolder.getDomain().getId());
        // Message type
        message.setType(DOMAIN_DESCRIPTION_TYPE);
        // Entity identifier
        message.setEntityId(descriptionId);
        // Input language tag
        message.setLanguageTag(languageTag);
        // Localized content
        message.setContent(descriptionContent);

        return message;
    }

    /*public class DomainIterable implements Iterable<Domain> {

        private Iterable<Domain> domains;
        private Map<UUID, String> descriptions;

        public DomainIterable(Iterable<Domain> domains, Map<UUID, String> descriptions) {
            this.domains = domains;
            this.descriptions = descriptions;
        }

        @Override
        public Iterator<Domain> iterator() {
            return new DomainIterator(domains.iterator(), descriptions);
        }
    }*/

    /*public class DomainIterator implements Iterator<Domain> {

        private Iterator<Domain> domainIterator;
        private Map<UUID, String> descriptions;

        public DomainIterator(Iterator<Domain> domainIterator, Map<UUID, String> descriptions) {
            this.domainIterator = domainIterator;
            this.descriptions = descriptions;
        }

        @Override
        public boolean hasNext() {
            return domainIterator.hasNext();
        }

        @Override
        public Domain next() {

            Domain resultDomain = domainIterator.next();

            UUID descriptionUuid = UUID.fromString(resultDomain.getDescription());
            String descriptionContent = descriptions.get(descriptionUuid);

            // Create and save the domain description's message for each language supported by the Message application
            messageDomain.getAvailableLanguageTags().forEach(languageTag ->
                    messageDao.index(buildMessage(resultDomain.getCode(), languageTag, descriptionUuid, descriptionContent)));

            // Set back the localized domain description
            resultDomain.setDescription(descriptionContent);

            return resultDomain;
        }
    }*/
}
