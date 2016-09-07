package info.jallaix.message.service;

import info.jallaix.message.dao.LanguageDao;
import info.jallaix.message.dto.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.core.event.BeforeCreateEvent;
import org.springframework.data.rest.core.event.BeforeSaveEvent;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
     * Assembler of HATEOAS resources
     */
    @Autowired
    private LanguageResourceAssembler resourceAssembler;

    /**
     * Validating event listener
     */
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    ValidatingRepositoryEventListener eventListener;

    /**
     * Create a new language upon an HTTP POST operation.
     * @param request An HTTP request that contains a {@link Language} entity and headers
     * @return An HTTP response with a language resource
     */
    @RequestMapping(method = RequestMethod.POST, value="/languages")
    public @ResponseBody ResponseEntity<Resource> createLanguage(RequestEntity<Language> request) {

        // Argument validation
        if (request.getBody() == null)
            throw new HttpMessageNotReadableException("Missing language data");

        eventListener.onApplicationEvent(new BeforeCreateEvent(request.getBody()));

        // Create the language
        Language language = request.getBody();
        if (repository.exists(language.getId()))            // The language already exists => error
            throw new DataIntegrityViolationException("Language already exists");

        else                                                // The language doesn't exist => save and return resource
            return new ResponseEntity<>(
                    resourceAssembler.toResource(
                            repository.save(language)),
                    HttpStatus.CREATED);
    }

    /**
     * Save an existing language upon an HTTP PUT operation
     * @param request An HTTP request that contains a {@link Language} entity and headers
     * @param id Identifier of the entity to update
     * @return An HTTP response with a language resource}
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/languages/{id}")
    public @ResponseBody ResponseEntity<Resource> saveLanguage(RequestEntity<Language> request, @PathVariable String id) {

        // Argument validation
        if (request.getBody() == null)
            throw new HttpMessageNotReadableException("Missing language data");

        eventListener.onApplicationEvent(new BeforeSaveEvent(request.getBody()));

        // Update the language
        Language language = request.getBody();
        if (!repository.exists(id))                         // The language already exists => error
            throw new ResourceNotFoundException("Language doesn't exists");

        else                                                // The language doesn't exist => save and return resource
            return new ResponseEntity<>(
                    resourceAssembler.toResource(
                            repository.save(language)),
                    HttpStatus.OK);
    }
}
