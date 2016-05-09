package info.jallaix.common.ws.rest;

import info.jallaix.common.dao.LanguageDao;
import info.jallaix.common.dto.Language;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Implementation of the <b>Language</b> REST web service.<br/>
 *     It provides CRUD operations for anything related to the Language entity.
 */
@RestController("/language")
public class LanguageRestService {

    private LanguageDao languageDao;

    /**
     * Create a new language entity in the datasource
     * @param language The language to add in the datasource
     */
	@RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
	public void create(Language language) {

        validateLanguageToCreate(language);

        if (!languageDao.get(language.getCode()).isPresent())
            languageDao.create(language);
        else
            throw new DuplicateLanguageException(language.getCode());
	}

    /**
     * Get an existing language entry from the datasource
     * @param code The code of the language to get
     * @return The language found
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Language get(@RequestParam("code") String code) {

        Optional<Language> language = languageDao.get(code);
        if (language.isPresent())
            return language.get();
        else
            throw new NotFoundLanguageException(code);
    }

    /**
     * Get all existing language entries from the datasource
     * @return The collection of languages found
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Collection<Language> get() {

        return languageDao.get();
    }

    /**
     * Validation tests for a language bean to create
     * @param language The language bean to test
     */
    private void validateLanguageToCreate(Language language) {

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

}
