package info.jallaix.common.language.service;

import info.jallaix.common.language.dao.LanguageDao;
import info.jallaix.common.language.dto.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the <b>Language</b> REST web service.<br/>
 *     It provides CRUD operations for anything related to the Language entity.
 */
@RestController("/language")
public class LanguageRestService {

    @Autowired
    private LanguageDao languageDao;

    /**
     * Create a new language entity in the datasource
     * @param language The language to add in the datasource
     */
	@RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
	public void create(Language language) {

        language = trimLanguageValues(language);                        // Trim strings
        validateLanguageToCreate(language);                             // Validate mandatory properties

        if (!languageDao.exists(language.getCode()))
            languageDao.index(language);                               // Create language if it doesn't exist
        else
            throw new DuplicateLanguageException(language.getCode());   // Else throw an exception
	}

    /**
     * Get an existing language entry from the datasource
     * @param code The code of the language to get
     * @return The language found
     */
    @RequestMapping(value = "/{code}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Language get(@PathVariable("code") String code) {

        code = code.trim();                                             // Trim string

        Language language = languageDao.findOne(code);
        if (language != null)
            return language;                                            // Return language if found
        else
            throw new LanguageNotFoundException(code);                  // Else throw an exception
    }

    /**
     * Get all existing language entries from the datasource
     * @return The collection of languages found
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Language> get() {

        final List<Language> languages = new ArrayList<>();
        languageDao.findAll().forEach(languages::add);                  // Convert iterable to list

        return languages;                                               // Get all languages
    }

    /**
     * Update an existing language
     * @param language The language to update
     */
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void update(Language language) {

        language = trimLanguageValues(language);                        // Trim strings
        validateLanguageToUpdate(language);                             // Validate mandatory properties

        if (languageDao.exists(language.getCode()))
            languageDao.save(language);                               // Update language if it exists
        else
            throw new LanguageNotFoundException(language.getCode());    // Else throw an exception
    }

    /**
     * Delete an existing language
     * @param code The code of the language to delete
     */
    @RequestMapping(value = "{code}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("code") String code) {

        code = code.trim();                                             // Trim string

        if (languageDao.exists(code))
            languageDao.delete(code);                                   // Delete language if it exists
        else
            throw new LanguageNotFoundException(code);                  // Else throw an exception
    }


    /**
     * Validation tests for a language bean to create
     * @param language The language bean to test
     */
    private static void validateLanguageToCreate(Language language) {

        if (language == null)
            throw new LanguageInvalidArgumentException(null, "null language");
        else if (language.getCode() == null)
            throw new LanguageInvalidArgumentException(language, "null code");
        else if ("".equals(language.getCode().trim()))
            throw new LanguageInvalidArgumentException(language, "empty code");
        else if (language.getLabel() == null)
            throw new LanguageInvalidArgumentException(language, "null label");
        else if ("".equals(language.getLabel().trim()))
            throw new LanguageInvalidArgumentException(language, "empty label");
        else if (language.getEnglishLabel() == null)
            throw new LanguageInvalidArgumentException(language, "null english label");
        else if ("".equals(language.getEnglishLabel().trim()))
            throw new LanguageInvalidArgumentException(language, "empty english label");
    }

    /**
     * Validation tests for a language bean to update
     * @param language The language bean to upate
     */
    private static void validateLanguageToUpdate(Language language) {

        if (language == null)
            throw new LanguageInvalidArgumentException(null, "null language");
        else if (language.getCode() == null)
            throw new LanguageInvalidArgumentException(language, "null code");
        else if ("".equals(language.getCode().trim()))
            throw new LanguageInvalidArgumentException(language, "empty code");
        else if (language.getLabel() != null && "".equals(language.getLabel().trim()))
            throw new LanguageInvalidArgumentException(language, "empty label");
        else if (language.getEnglishLabel() != null && "".equals(language.getEnglishLabel().trim()))
            throw new LanguageInvalidArgumentException(language, "empty english label");
    }

    /**
     * Trim all property strings on a language
     * @param language The language on which strings must be trimmed
     * @return The updated language
     */
    private static Language trimLanguageValues(Language language) {

        if (language == null) return null;
        if (language.getCode() != null) language.setCode(language.getCode().trim());
        if (language.getLabel() != null) language.setLabel(language.getLabel().trim());
        if (language.getEnglishLabel() != null) language.setEnglishLabel(language.getEnglishLabel().trim());

        return language;
    }
}
