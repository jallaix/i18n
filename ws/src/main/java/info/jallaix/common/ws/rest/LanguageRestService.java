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

		if (language == null)
			throw new LanguageInvalidArgumentException(null, "null language");

		if (language.getCode() == null)
			throw new LanguageInvalidArgumentException(language, "null code");

        if ("".equals(language.getCode()))
            throw new LanguageInvalidArgumentException(language, "empty code");

        if (language.getLabel() == null)
			throw new LanguageInvalidArgumentException(language, "null label");

        if ("".equals(language.getLabel()))
            throw new LanguageInvalidArgumentException(language, "empty label");

        if (language.getEnglishLabel() == null)
			throw new LanguageInvalidArgumentException(language, "null english label");

        if ("".equals(language.getEnglishLabel()))
            throw new LanguageInvalidArgumentException(language, "empty english label");

		return language;
	}
}
