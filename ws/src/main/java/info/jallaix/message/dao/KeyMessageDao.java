package info.jallaix.message.dao;

import info.jallaix.message.bean.EntityMessage;
import info.jallaix.message.bean.KeyMessage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Collection;

/**
 * This interface manages all datasource accesses related to a message.
 */
public interface KeyMessageDao extends ElasticsearchRepository<KeyMessage, String>, KeyMessageDaoCustom {

    /**
     * Find all messages that belong to a domain.
     *
     * @param domainId The domain identifier the messages must match
     * @return The collection of messages that belong to the domain
     */
    @SuppressWarnings("unused")
    Collection<KeyMessage> findByDomainId(String domainId);
}
