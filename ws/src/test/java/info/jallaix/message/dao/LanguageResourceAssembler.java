package info.jallaix.message.dao;

import info.jallaix.message.dto.Language;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

/**
 * Created by Julien on 29/05/2016.
 */
public class LanguageResourceAssembler extends ResourceAssemblerSupport<Language, LanguageResource> {

    public LanguageResourceAssembler() {
        super(LanguageDao.class, LanguageResource.class);
    }

    @Override
    public LanguageResource toResource(Language language) {

        LanguageResource resource = this.instantiateResource(language);
        resource.setLabel(language.getLabel());
        resource.setEnglishLabel(language.getEnglishLabel());

        resource.add(new Link("http://localhost:8080/languages/" + language.getCode()));
        resource.add(new Link("http://localhost:8080/languages/" + language.getCode(), "language"));

        return resource;
    }
}
