package info.jallaix.message.dao;

import info.jallaix.message.dto.Domain;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * This interface manages all datasource accesses related to a domain.
 */
@Repository
public interface DomainDao extends ElasticsearchRepository<Domain, String>, DomainDaoCustom {

    /**
     * Find a domain by a code.
     *
     * @param code The domain code
     * @return The found domain
     */
    Domain findByCode(String code);
}
