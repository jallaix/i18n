package info.jallaix.common.ws.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Julien on 09/05/2016.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundLanguageException extends RuntimeException {

    public NotFoundLanguageException(String languageCode) {
        super("code not found = " + languageCode);
    }
}
