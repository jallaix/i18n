package info.jallaix.message.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception manages duplication error when adding a new language
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateLanguageException extends RuntimeException {

    public DuplicateLanguageException(String languageCode) {
        super("language code = " + languageCode);
    }
}
