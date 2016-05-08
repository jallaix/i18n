package info.jallaix.common.ws.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Julien on 09/05/2016.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateLanguageException extends RuntimeException {

    public DuplicateLanguageException(String languageCode) {
        super(" language code = " + languageCode);
    }
}
