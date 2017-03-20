package info.jallaix.message.dao;

import info.jallaix.message.bean.Domain;

/**
 * This interface manages all custom datasource accesses related to a domain.
 */
public interface DomainDaoCustom {

    /**
     * Find a domain by a code.
     *
     * @param code The domain code
     * @return The domain found
     */
    Domain findByCode(String code);
}
