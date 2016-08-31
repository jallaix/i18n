package info.jallaix.message.service;

import info.jallaix.message.dto.Language;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * This class assembles languages with links to return HATEOAS resources.
 */
@Component
public class LanguageResourceAssembler extends ResourceAssemblerSupport<Language, Resource> {

    /**
     * Constructor storing the Language web controller
     */
    public LanguageResourceAssembler() {
        super(LanguageController.class, Resource.class);
    }

    @Override
    public Resource toResource(Language language) {

        Resource<Language> resource = new Resource<>(language);
        resource.add(linkTo(LanguageController.class).slash(language.getId()).withSelfRel());
        resource.add(linkTo(LanguageController.class).slash(language.getId()).withRel("language"));

        return resource;
    }
}
