package info.jallaix.common.ws.rest;

import info.jallaix.common.dto.Language;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Julien on 08/05/2016.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LanguageInvalidArgumentException extends RuntimeException {

    public LanguageInvalidArgumentException(Language language, String invalidArgument) {
        super(invalidArgument + " : language data = " + language);
    }
}
