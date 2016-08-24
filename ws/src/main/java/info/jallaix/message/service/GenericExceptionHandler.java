package info.jallaix.message.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.rest.webmvc.RepositoryRestExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * This class handles exceptions thrown by controllers in the same package, the same way the {@link RepositoryRestExceptionHandler} handler does.
 */
@ControllerAdvice(basePackageClasses = GenericExceptionHandler.class)
public class GenericExceptionHandler extends RepositoryRestExceptionHandler {

    @Autowired
    public GenericExceptionHandler(MessageSource messageSource) {
        super(messageSource);
    }
}
