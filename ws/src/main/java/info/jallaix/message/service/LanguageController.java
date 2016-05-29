package info.jallaix.message.service;

import info.jallaix.message.dao.LanguageDao;
import info.jallaix.message.dto.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Julien on 30/05/2016.
 */
@RepositoryRestController
public class LanguageController {

    private final LanguageDao repository;

    @Autowired
    public LanguageController(LanguageDao repository) {
        this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/languages")
    public @ResponseBody ResponseEntity<?> saveLanguage(RequestEntity<Language> request) {

        ResponseEntity<?> response;

        if (request.hasBody()) {
            Language language = request.getBody();
            if (repository.exists(language.getCode()))
                response = new ResponseEntity<LanguageResource>(HttpStatus.CONFLICT);
            else {
                language = repository.save(language);
                LanguageResource languageResource = new LanguageResourceAssembler().toResource(language);
                response = new ResponseEntity<LanguageResource>(languageResource, HttpStatus.CREATED);
            }
        }
        else {
            response = new ResponseEntity<LanguageResource>(HttpStatus.BAD_REQUEST);
        }

        return response;
    }
}
