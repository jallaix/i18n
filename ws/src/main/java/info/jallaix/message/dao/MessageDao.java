package info.jallaix.message.dao;

import info.jallaix.message.dto.EntityMessage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Collection;

/**
 * This interface manages all datasource accesses related to a message.
 */
public interface MessageDao extends ElasticsearchRepository<EntityMessage, String>, MessageDaoCustom {

    /**
     * Find a message for the given code, language and domain.
     *
     * @param code The code that the message must match
     * @param languageTag The language tag that the message must match
     * @param domainCode The domain code that the message must match
     * @return The message found
     */
    @SuppressWarnings("unused")
    EntityMessage findByDomainIdAndTypeAndEntityIdAndLanguageTag(String domainId, String type, String entityId, String languageTag);

    /**
     * Find all messages that belong to a domain.
     *
     * @param domainCode The domain code that the messages must match
     * @return The collection of messages that belong to the domain
     */
    @SuppressWarnings("unused")
    Collection<EntityMessage> findByDomainId(String domainId);
}
