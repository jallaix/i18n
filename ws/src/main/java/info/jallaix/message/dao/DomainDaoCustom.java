package info.jallaix.message.dao;

/**
 * This interface manages custom datasource accesses related to a domain.
 */
public interface DomainDaoCustom {

    /**
     * Indicate if a language is used in any domain
     * @param languageCode The language code
     * @return {@code true} if the language is used, else {@code false}
     */
    public boolean isLanguageUsed(String languageCode);
}
