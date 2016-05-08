package info.jallaix.common.dao;

import info.jallaix.common.dto.Language;

/**
 * Created by Julien on 08/05/2016.
 */
public interface LanguageDao {

    /**
     * Create a new language in the datasource
     * @param language The language to create
     */
    public void create(Language language);

    /**
     * Get a language with its code
     * @param languageCode The language code
     * @return The language found
     */
    public Language get(String languageCode);
}
