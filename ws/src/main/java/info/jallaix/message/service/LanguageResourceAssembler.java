package info.jallaix.message.service;

import info.jallaix.message.dto.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

/**
 * This class assembles languages with links to return HATEOAS resources.
 */
@Component
public class LanguageResourceAssembler extends ResourceAssemblerSupport<Language, Resource> {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private EntityLinks entityLinks;

    /**
     * Constructor storing the Language web controller
     */
    public LanguageResourceAssembler() {
        super(LanguageController.class, Resource.class);
    }

    @Override
    public Resource toResource(Language language) {

        final LinkBuilder linkBuilder = entityLinks.linkForSingleResource(Language.class, language.getId());
        Resource<Language> resource = new Resource<>(language);
        resource.add(linkBuilder.withSelfRel());
        resource.add(linkBuilder.withRel("language"));

        return resource;
    }
}
