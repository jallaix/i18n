package info.jallaix.message.dao;

import info.jallaix.message.dto.Domain;

/**
 * This interface manages custom datasource accesses related to a domain.
 */
public interface DomainDaoCustom {

    /**
     * Indicate if a language is used in any domain
     * @param languageId The language identifier
     * @return {@code true} if the language is used, else {@code false}
     */
    boolean isLanguageUsed(String languageId);
}
