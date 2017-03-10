package info.jallaix.message.dao;

import info.jallaix.message.bean.Domain;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * This interface manages all datasource accesses related to a domain.
 */
@Repository
@SuppressWarnings("unused")
public interface DomainDao extends ElasticsearchRepository<Domain, String> {

    /**
     * Find a domain by a code.
     *
     * @param code The domain code
     * @return The domain found
     */
    Domain findByCode(String code);
}
