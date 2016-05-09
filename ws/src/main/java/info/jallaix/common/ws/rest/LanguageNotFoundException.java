package info.jallaix.common.ws.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception manages a not found error when updating a language
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class LanguageNotFoundException extends RuntimeException {

    public LanguageNotFoundException(String languageCode) {
        super("code not found = " + languageCode);
    }
}
