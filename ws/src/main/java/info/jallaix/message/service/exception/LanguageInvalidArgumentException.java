package info.jallaix.message.service.exception;

import info.jallaix.message.dto.Language;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception manages invalid arguments error when adding or updating a language
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LanguageInvalidArgumentException extends RuntimeException {

    public LanguageInvalidArgumentException(Language language, String invalidArgument) {
        super(invalidArgument + " : language data = " + language);
    }
}
