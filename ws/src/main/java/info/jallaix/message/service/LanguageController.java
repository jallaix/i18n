package info.jallaix.message.service;

import info.jallaix.message.dao.LanguageDao;
import info.jallaix.message.dto.Language;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Created by Julien on 30/05/2016.
 */
@RepositoryRestController
public class LanguageController {

    @Autowired
    private LanguageDao repository;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private RepositoryEntityLinks entityLinks;

    @Resource
    Validator beforeCreateLanguageValidator;

/*    @Autowired
    public LanguageController(LanguageDao repository) {
        this.repository = repository;
    }*/

    @RequestMapping(method = RequestMethod.POST, value = "/languages")
    public @ResponseBody ResponseEntity<?> saveLanguage(RequestEntity<Language> request) {

        ResponseEntity<?> response;

        // Argument validation
        Errors errors = new BeanPropertyBindingResult(request.getBody(), "saveLanguage");
        beforeCreateLanguageValidator.validate(request, errors);
        if (errors.hasErrors())
            throw new InvalidLanguageException();

        Language language = request.getBody();
        if (repository.exists(language.getCode()))          // The language already exists => error
            throw new DuplicateLanguageException();
        else {                                              // The language doesn't exist => save
            language = repository.save(language);

            // TODO Refactor
            LanguageResource languageResource = new LanguageResource(); //LanguageResourceAssembler().toResource(language);
            languageResource.setLabel(language.getLabel());
            languageResource.setEnglishLabel(language.getEnglishLabel());
            Link languageLink = entityLinks.linkToSingleResource(Language.class, language.getCode());
            languageResource.add(languageLink.withSelfRel());
            languageResource.add(languageLink);

            response = new ResponseEntity<>(languageResource, HttpStatus.CREATED);
        }

        return response;
    }

    @ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Invalid language argument")
    public class InvalidLanguageException extends RuntimeException {}

    @ResponseStatus(value=HttpStatus.CONFLICT, reason="This language already exists")
    public class DuplicateLanguageException extends RuntimeException {}
}
