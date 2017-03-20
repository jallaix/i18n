package info.jallaix.message.service.hateoas;

import info.jallaix.message.bean.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.Resources;

/**
 * Resource processor that adds a {@code search} HATEOAS link to the {@code domains} resource.
 */
public class DomainsResourceProcessor implements ResourceProcessor<Resources<Resource<Domain>>> {

    /**
     * Accessor to links pointing to controllers backing an entity type
     */
    @Autowired
    private EntityLinks entityLinks;

    /**
     * Add a {@code search} HATEOAS link to the {@code domains} resource.
     *
     * @param domainsResource The domains resource
     * @return The updated domains resource
     */
    @Override
    public Resources<Resource<Domain>> process(Resources<Resource<Domain>> domainsResource) {

        domainsResource.add(entityLinks.linkFor(Domain.class).slash("/search").withRel("search"));
        return domainsResource;
    }
}
