package info.jallaix.message.dao.interceptor;

import info.jallaix.message.dao.MessageDao;
import info.jallaix.message.dto.Domain;
import info.jallaix.message.dto.Message;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * <p>
 * This bean intercepts Spring Data Elasticsearch operations to manage localized messages.
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

    private ThreadLocal<UUID> descriptionUuid = new ThreadLocal<>();
    private ThreadLocal<String> descriptionContent = new ThreadLocal<>();

    /**
     * Intercept entities before they are created to save localized messages in the index and replace matching property values by message codes.
     *
     * @param entities The list of entities to create
     */
    @Before("execution(* org.springframework.data.elasticsearch.repository.ElasticsearchRepository+.save(Iterable,..)) && args(entities,..)"
            + " || execution(* org.springframework.data.elasticsearch.repository.ElasticsearchRepository+.index(Iterable,..)) && args(entities,..)")
    public void beforeCreation(Iterable entities) {
        System.out.println(entities);
    }

    /**
     * Intercept a domain before it's indexed to replace its description's value by a message code.
     *
     * @param arg The domain to index
     */
    @Before("execution(* info.jallaix.message.dao.DomainDao+.save(Object)) && args(arg)"
            + " || execution(* info.jallaix.message.dao.DomainDao+.index(Object)) && args(arg)")
    public void beforeCreation(Object arg) {

        if (arg == null)
            return;
        Domain domain = Domain.class.cast(arg);

        // Store the domain description
        descriptionContent.set(domain.getDescription());

        // Replace the domain description's value by a random UUID that will be used to store the description in the message index
        descriptionUuid.set(UUID.randomUUID());
        domain.setDescription(descriptionUuid.get().toString());
    }

    /**
     * Intercept a domain after been indexed to store its original description's value in the message index.
     * The returned domain's description is replaced by the matching value from the message index.
     *
     * @param result The indexed domain
     */
    @AfterReturning(pointcut = "execution(* info.jallaix.message.dao.DomainDao+.save(Object))"
            + " || execution(* info.jallaix.message.dao.DomainDao+.index(Object))", returning = "result")
    public void afterCreation(Object result) throws NoSuchFieldException {

        if (result == null)
            return;
        Domain domain = Domain.class.cast(result);

        // Get the message domain
        messageDomain.getAvailableLanguageTags().forEach(languageTag -> {

            // Create and save the domain description's message
            Message message = buildMessage(languageTag);
            message = messageDao.index(message);
        });

        // Set the localized domain description
        domain.setDescription(descriptionContent.get());

        // Clear thread local values
        descriptionUuid.remove();
        descriptionContent.remove();
    }

    /**
     * Build a message for the domain's description.
     *
     * @return The built message
     */
    private Message buildMessage(String languageTag) {

        Message message = new Message();

        // Message code
        message.setCode(descriptionUuid.get().toString());
        // Domain code
        message.setDomainCode(messageDomain.getCode());
        // Message type
        //TODO message.setType(Domain.class.getName() + ".description");
        // Input language tag
        message.setLanguageTag(languageTag);
        // Localized content
        message.setContent(descriptionContent.get());

        return message;
    }
}
