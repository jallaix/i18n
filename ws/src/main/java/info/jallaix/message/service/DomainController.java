package info.jallaix.message.service;

import info.jallaix.message.bean.Domain;
import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.service.hateoas.DomainResourceAssembler;
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
import org.springframework.web.bind.annotation.*;

/**
 * <p/>
 * Spring Data REST for Elasticsearch permits both POST and PUT requests to create and update entities.
 * The {@link DomainController} overrides the default behaviour by forcing POST for creation and PUT for update.
 * <p/>
 * This controller overrides the POST operation for the {@link Domain} entity so that it throws a {@code 409 Conflict} HTTP error when trying to update an existing {@link Domain}.
 * <p/>
 * It also overrides the PUT operation for the {@link Domain} entity so that it throws a {@code 404 Bad Request} HTTP error when trying to create a new {@link Domain}.
 */
@RepositoryRestController
public class DomainController {

    /**
     * DAO to perform database operations with the Domain entity
     */
    @Autowired
    private DomainDao repository;

    /**
     * Assembler of HATEOAS resources
     */
    @Autowired
    private DomainResourceAssembler resourceAssembler;

    /**
     * Validating event listener
     */
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    ValidatingRepositoryEventListener eventListener;


    /**
     * Create a new domain upon an HTTP POST operation.
     *
     * @param request An HTTP request that contains a {@link Domain} entity and headers
     * @return An HTTP response with a domain resource
     */
    @RequestMapping(method = RequestMethod.POST, value = "/domains")
    public
    @ResponseBody
    ResponseEntity<Resource> createDomain(RequestEntity<Domain> request) {

        // Argument validation
        if (request.getBody() == null)
            throw new HttpMessageNotReadableException("Missing domain data");

        eventListener.onApplicationEvent(new BeforeCreateEvent(request.getBody()));

        // Create the domain
        Domain domain = request.getBody();
        if (repository.exists(domain.getId()))            // The domain already exists => error
            throw new DataIntegrityViolationException("Domain already exists");

        else                                                // The domain doesn't exist => save and return resource
            return new ResponseEntity<>(
                    resourceAssembler.toResource(
                            repository.save(domain)),
                    HttpStatus.CREATED);
    }

    /**
     * Save an existing domain upon an HTTP PUT operation
     *
     * @param request An HTTP request that contains a {@link Domain} entity and headers
     * @param id      Identifier of the entity to update
     * @return An HTTP response with a domain resource
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/domains/{id}")
    public
    @ResponseBody
    ResponseEntity<Resource> updateDomain(RequestEntity<Domain> request, @PathVariable String id) {

        // Argument validation
        if (request.getBody() == null)
            throw new HttpMessageNotReadableException("Missing domain data");

        eventListener.onApplicationEvent(new BeforeSaveEvent(request.getBody()));

        // Update the domain
        Domain domain = request.getBody();
        if (!repository.exists(id))                         // The domain already exists => error
            throw new ResourceNotFoundException("Domain doesn't exists");

        else                                                // The domain doesn't exist => save and return resource
            return new ResponseEntity<>(
                    resourceAssembler.toResource(
                            repository.save(domain)),
                    HttpStatus.OK);
    }

    /**
     * Find a domain by code.
     *
     * @param code The domain code
     * @return An HTTP response with a domain resource
     */
    @RequestMapping(method = RequestMethod.GET, value = "/domains/search/findByCode")
    public
    @ResponseBody
    ResponseEntity<Resource> findByCode(@RequestParam(value = "code", required = true) String code) {

        Domain domain = repository.findByCode(code);

        if (domain == null)
            throw new ResourceNotFoundException("Domain doesn't exists");
        else
            return new ResponseEntity<>(
                    resourceAssembler.toResource(domain),
                    HttpStatus.OK);
    }
}
