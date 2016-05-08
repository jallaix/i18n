package info.jallaix.common.ws.rest;

import info.jallaix.common.dao.LanguageDao;
import info.jallaix.common.dto.Language;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
	public void create(Language language) {

        validateLanguageToCreate(language);

        Language result = languageDao.get(language.getCode());
        if (result == null)
            languageDao.create(language);
        else
            throw new DuplicateLanguageException(language.getCode());
	}


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
