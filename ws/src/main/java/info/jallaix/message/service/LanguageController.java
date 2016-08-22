package info.jallaix.message.service;

import info.jallaix.message.dao.LanguageDao;
import info.jallaix.message.dto.Language;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.data.rest.core.event.BeforeCreateEvent;
import org.springframework.data.rest.core.event.RepositoryEvent;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.Resource;

/**
 * <p>
 *     Spring Data REST for Elasticsearch doesn't properly handle POST requests. It should only create new entities but it also updates existing ones.
 * <p>
 *     This controller overrides the POST operation for the {@link Language} entity so that it throws a {@code 409 Conflict} HTTP error when trying to update an existing {@link Language}.
 */
@RepositoryRestController
public class LanguageController {

    /**
     * DAO to perform database operations with the Language entity
     */
    @Autowired
    private LanguageDao repository;

    /**
     * Generator of HATEOAS links
     */
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private RepositoryEntityLinks entityLinks;

    /**
     * Validating event listener
     */
    @Autowired
    ValidatingRepositoryEventListener eventListener;

    /**
     * Bean mapper
     */
    @Resource
    private Mapper beanMapper;

    /**
     * Define a {@code 400 Bad Request} HTTP error
     */
    @ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Invalid language argument")
    public class InvalidLanguageException extends RuntimeException {}

    /**
     * Define a {@code 409 Conflict} HTTP error
     */
    @ResponseStatus(value=HttpStatus.CONFLICT, reason="This language already exists")
    public class DuplicateLanguageException extends RuntimeException {}

    /**
     * Save a new language upon an HTTP POST operation.
     * @param request An HTTP request that contains a {@link Language} entity and headers
     * @return An HTTP response with a {@link LanguageResource}
     * @throws InvalidLanguageException A {@code 400 Bad Request} HTTP error if the HTTP request doesn't contain a valid {@link Language} entity
     * @throws DuplicateLanguageException A {@code 409 Conflict} HTTP error if the HTTP request contains a {@link Language} entity that already exists
     */
    @RequestMapping(method = RequestMethod.POST, value = "/languages")
    public @ResponseBody ResponseEntity<?> saveLanguage(RequestEntity<Language> request) throws InvalidLanguageException, DuplicateLanguageException {

        ResponseEntity<?> response;

        // Argument validation
        if (request.getBody() == null)
            throw new InvalidLanguageException();
        try {
            eventListener.onApplicationEvent(new BeforeCreateEvent(request.getBody()));
        } catch (RepositoryConstraintViolationException e) {
            throw new InvalidLanguageException();
        }

        // Create the language
        Language language = request.getBody();
        if (repository.exists(language.getCode()))          // The language already exists => error
            throw new DuplicateLanguageException();
        else {                                              // The language doesn't exist => save
            language = repository.save(language);

            // Define the response
            LanguageResource languageResource = beanMapper.map(language, LanguageResource.class);
            Link languageLink = entityLinks.linkToSingleResource(Language.class, language.getCode());
            languageResource.add(languageLink.withSelfRel());
            languageResource.add(languageLink);

            response = new ResponseEntity<>(languageResource, HttpStatus.CREATED);
        }

        return response;
    }
}
