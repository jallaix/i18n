package info.jallaix.message.service;

import org.springframework.data.rest.webmvc.RepositoryRestExceptionHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Created by Julien on 03/06/2016.
 */
@ControllerAdvice(basePackageClasses = RepositoryRestExceptionHandler.class)
public class GenericExceptionHandler {
    @ExceptionHandler
    ResponseEntity handle(Exception e) {
        return new ResponseEntity("Some message", new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
}
