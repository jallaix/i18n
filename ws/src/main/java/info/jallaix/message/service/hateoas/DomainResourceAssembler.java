package info.jallaix.message.service.hateoas;

import info.jallaix.message.bean.Domain;
import info.jallaix.message.service.DomainController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

/**
 * This class assembles domains with links to return HATEOAS resources.
 */
public class DomainResourceAssembler extends ResourceAssemblerSupport<Domain, Resource> {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private EntityLinks entityLinks;

    /**
     * Constructor storing the {@link DomainController} web controller.
     */
    public DomainResourceAssembler() {
        super(DomainController.class, Resource.class);
    }

    /**
     * Convert a {@link Domain} entity into a {@link Resource<Domain>} resource.
     *
     * @param domain The {@link Domain} entity to convert
     * @return The {@link Resource<Domain>} resource
     */
    @Override
    public Resource<Domain> toResource(Domain domain) {

        final LinkBuilder linkBuilder = entityLinks.linkForSingleResource(Domain.class, domain.getId());
        Resource<Domain> resource = new Resource<>(domain);
        resource.add(linkBuilder.withSelfRel());
        resource.add(linkBuilder.withRel("domain"));

        return resource;
    }
}
