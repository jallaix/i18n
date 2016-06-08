package info.jallaix.message.service;

import info.jallaix.message.dto.Language;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * Created by Julien on 29/05/2016.
 */
public class LanguageResourceAssembler extends ResourceAssemblerSupport<Language, LanguageResource> {

    public LanguageResourceAssembler() {
        super(LanguageController.class, LanguageResource.class);
    }

    @Override
    public LanguageResource toResource(Language language) {

        LanguageResource resource = this.createResourceWithId(language.getCode(), language);
        resource.setLabel(language.getLabel());
        resource.setEnglishLabel(language.getEnglishLabel());

        resource.add(linkTo(LanguageController.class).withSelfRel());
       // resource.add(linkTo(LanguageController.class).withRel("language"));

        return resource;
    }
}
