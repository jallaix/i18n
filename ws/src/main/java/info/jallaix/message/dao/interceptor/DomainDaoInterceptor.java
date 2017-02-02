package info.jallaix.message.dao.interceptor;

import info.jallaix.message.dao.MessageDao;
import info.jallaix.message.dto.Domain;
import info.jallaix.message.dto.Message;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

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

    @Resource
    private Domain messageDomain;

    @Autowired
    private MessageDao messageDao;

    /**
     * Intercept entities before they are created to save localized messages in the index and replace matching property values by message codes.
     *
     * @param entities The list of entities to create
     */
    @Around("execution(* org.springframework.data.elasticsearch.repository.ElasticsearchRepository+.save(Iterable,..)) && args(entities,..)")
    public void aroundSaving(ProceedingJoinPoint joinPoint, Iterable entities) {

    }

    /**
     * Intercept a domain saving operation before it's indexed to replace its description's value by a message code
     * and store its localized descriptions.
     *
     * @param joinPoint Joint point for the targeted operation
     * @param entity    The domain to index
     */
    @Around("execution(* info.jallaix.message.dao.DomainDao+.save(Object)) && args(entity)"
            + " || execution(* info.jallaix.message.dao.DomainDao+.index(Object)) && args(entity)")
    public Object aroundSaving(ProceedingJoinPoint joinPoint, Object entity) throws Throwable {

        // Get the domain to save
        if (entity == null)
            return joinPoint.proceed(new Object[]{null});
        Domain initialDomain = Domain.class.cast(entity);

        // Store the domain description
        final String descriptionContent = initialDomain.getDescription();

        // Replace the domain description's value by a random UUID that will be used to store the description in the message index
        final UUID descriptionUuid = UUID.randomUUID();
        initialDomain.setDescription(descriptionUuid.toString());

        // Call the save method
        Object resultEntity = joinPoint.proceed(new Object[]{initialDomain});

        // Get the saved domain
        if (resultEntity == null)
            return null;
        Domain resultDomain = Domain.class.cast(resultEntity);

        // Create and save the domain description's message for each language supported by the Message application
        messageDomain.getAvailableLanguageTags().forEach(languageTag ->
                messageDao.index(buildMessage(resultDomain.getCode(), languageTag, descriptionUuid, descriptionContent)));

        // Set back the localized domain description
        resultDomain.setDescription(descriptionContent);

        return resultDomain;
    }

    /**
     * Build a message for the domain's description.
     *
     * @return The built message
     */
    private Message buildMessage(String domainCode, String languageTag, UUID descriptionUuid, String descriptionContent) {

        Message message = new Message();

        // Message code
        message.setCode(descriptionUuid.toString());
        // Domain code
        message.setDomainCode(domainCode);
        // Message type
        //TODO message.setType(Domain.class.getName() + ".description");
        // Input language tag
        message.setLanguageTag(languageTag);
        // Localized content
        message.setContent(descriptionContent);

        return message;
    }
}
