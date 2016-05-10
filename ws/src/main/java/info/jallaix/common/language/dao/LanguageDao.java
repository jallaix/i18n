package info.jallaix.common.language.dao;

import info.jallaix.common.language.dto.Language;

import java.util.Collection;
import java.util.Optional;

/**
 * This interface manages all datasource accesses related to a language
 */
public interface LanguageDao {

    /**
     * Create a new language in the datasource
     * @param language The language to create
     */
    void create(Language language);

    /**
     * Define if a language exists in te datasource
     * @param languageCode The language code
     * @return <code>true</code> if the language exists, else <code>false</code>
     */
    boolean exist(String languageCode);

    /**
     * Get a language from the datasource
     * @param languageCode The language code
     * @return The language found
     */
    Optional<Language> get(String languageCode);

    /**
     * Get all languages from the datasource
     * @return A collection of languages
     */
    Collection<Language> get();

    /**
     * Update a language in the datasource
     * @param language The language to update
     */
    void update(Language language);

    /**
     * Delete a language in the datasource
     * @param code The language code
     */
    void delete(String code);
}
