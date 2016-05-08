package info.jallaix.common.ws.rest;

import info.jallaix.common.dto.Language;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Julien on 08/05/2016.
 */
@RestController("/language")
public class LanguageRestService {

	@RequestMapping("/")
	public String home() {
		return "Hello Docker World";
	}

	@RequestMapping(method = RequestMethod.POST)
	public Language create(Language language) {

        validateLanguageToCreate(language);

		return language;
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
