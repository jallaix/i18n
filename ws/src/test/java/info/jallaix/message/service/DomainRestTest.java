package info.jallaix.message.service;

import com.google.common.collect.ImmutableMap;
import info.jallaix.message.ApplicationMock;
import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.dto.Domain;
import info.jallaix.spring.data.es.test.bean.ValidationError;
import info.jallaix.spring.data.es.test.testcase.BaseRestElasticsearchTestCase;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.TypeReferences;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;
import java.util.*;

import static java.util.Collections.singletonList;

/**
 * Created by Julien on 22/01/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ApplicationMock.class)
@WebIntegrationTest(randomPort = true)
public class DomainRestTest extends BaseRestElasticsearchTestCase<Domain, String, DomainDao> {
    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                      Abstract methods implementation                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Constructor that defines the tests to pass
     */
    public DomainRestTest() {
        super();
        /*super(
                RestTestedMethod.Create.class,
                RestTestedMethod.Update.class,
                RestTestedMethod.Patch.class,
                RestTestedMethod.FindAll.class,
                RestTestedMethod.Exist.class,
                RestTestedMethod.Delete.class);*/
    }

    @Override
    protected Domain newDocumentToInsert() {
        return new Domain("4", "project4", "project4.description", "es-ES", Arrays.asList("en-US", "fr-FR", "es-ES"));
    }

    @Override
    protected Domain newDocumentToUpdate() {
        return new Domain("2", "project4", "project4.description", "es-ES", Arrays.asList("en-US", "fr-FR", "es-ES"));
    }

    @Override
    protected Domain newExistingDocument() {
        return new Domain("2", "project2", "project2.description", "fr-FR", Arrays.asList("en-US", "fr-FR", "es-ES"));
    }

    @Override
    protected Field getSortField() {

        try {
            Field f = Domain.class.getDeclaredField("code");
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object newObjectForPatch() {

        return new Object() {
            @SuppressWarnings("unused")
            public String getCode() {
                return "project4";
            }

            @SuppressWarnings("unused")
            public Collection<String> getAvailableLanguageTags() {
                return Arrays.asList("en-GB", "fr-FR", "es-ES", "fr-CA");
            }
        };
    }

    @Override
    protected int getPageSize() {
        return 2;
    }

    @Override
    protected TypeReferences.ResourceType<Domain> getResourceType() {
        return new TypeReferences.ResourceType<Domain>() {
        };
    }

    @Override
    protected TypeReferences.PagedResourcesType<Resource<Domain>> getPagedResourcesType() {
        return new TypeReferences.PagedResourcesType<Resource<Domain>>() {
        };
    }

    @Override
    protected Map<Domain, List<ValidationError>> getExpectedValidationErrorsOnCreate() {

        final String domainClassName = Domain.class.getSimpleName();

        return ImmutableMap.<Domain, List<ValidationError>>builder()
                // Code required
                .put(
                        new Domain("2", null, "project2.description", "fr-FR", Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.code.required", "null", "code")))
                .put(
                        new Domain("2", "", "project2.description", "fr-FR", Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.code.required", "", "code")))
                .put(
                        new Domain("2", "  ", "project2.description", "fr-FR", Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.code.required", "  ", "code")))
                // Test invalid description
                .put(
                        new Domain("2", "project2", null, "fr-FR", Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.description.required", "null", "description")))
                .put(
                        new Domain("2", "project2", "", "fr-FR", Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.description.required", "", "description")))
                .put(
                        new Domain("2", "project2", "  ", "fr-FR", Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.description.required", "  ", "description")))
                // Default language tag required
                .put(
                        new Domain("2", "project2", "project2.description", null, Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.defaultLanguageTag.required", "null", "defaultLanguageTag")))
                .put(
                        new Domain("2", "project2", "project2.description", "", Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.defaultLanguageTag.required", "", "defaultLanguageTag")))
                .put(
                        new Domain("2", "project2", "project2.description", "  ", Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.defaultLanguageTag.required", "  ", "defaultLanguageTag")))
                // Default language tag must exist in available language tags
                .put(
                        new Domain("2", "project2", "project2.description", "fr-FR", Collections.singletonList("en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.defaultLanguageTag.matchAvailable", "fr-FR", "defaultLanguageTag")))
                // Default language tag must match an existing locale
                .put(
                        new Domain("2", "project2", "project2.description", "unknown-language-tag", Arrays.asList("fr-FR", "en-US", "unknown-language-tag")),
                        Arrays.asList(new ValidationError(domainClassName, "domain.defaultLanguageTag.unavailable", "unknown-language-tag", "defaultLanguageTag"),
                                new ValidationError(domainClassName, "domain.availableLanguageTags.unavailable", "[fr-FR, en-US, unknown-language-tag]", "availableLanguageTags"))
                )
                // Available language tags can't be empty
                .put(
                        new Domain("2", "project2", "project2.description", "fr-FR", null),
                        singletonList(new ValidationError(domainClassName, "domain.availableLanguageTags.required", "null", "availableLanguageTags")))
                .put(
                        new Domain("2", "project2", "project2.description", "fr-FR", Collections.emptyList()),
                        singletonList(new ValidationError(domainClassName, "domain.availableLanguageTags.required", "[]", "availableLanguageTags")))
                // Available language tags must match existing locales
                .put(
                        new Domain("2", "project2", "project2.description", "fr-FR", Arrays.asList("fr-FR", "unknown-language-tag")),
                        singletonList(new ValidationError(domainClassName, "domain.availableLanguageTags.unavailable", "[fr-FR, unknown-language-tag]", "availableLanguageTags")))
                // Test all invalid properties
                .put(
                        new Domain("2", null, null, null, null),
                        Arrays.asList(
                                new ValidationError(domainClassName, "domain.code.required", "null", "code"),
                                new ValidationError(domainClassName, "domain.description.required", "null", "description"),
                                new ValidationError(domainClassName, "domain.defaultLanguageTag.required", "null", "defaultLanguageTag"),
                                new ValidationError(domainClassName, "domain.availableLanguageTags.required", "null", "availableLanguageTags")
                        ))
                .build();
    }

    @Override
    protected Map<Domain, List<ValidationError>> getExpectedValidationErrorsOnUpdate() {

        final String domainClassName = Domain.class.getSimpleName();

        return ImmutableMap.<Domain, List<ValidationError>>builder()
                // Code can't change
                .put(
                        new Domain("2", "project4", "project2.description", "fr-FR", Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.code.immutable", "project4", "code")))
                // Test invalid description
                .put(
                        new Domain("2", "project2", null, "fr-FR", Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.description.required", "null", "description")))
                .put(
                        new Domain("2", "project2", "", "fr-FR", Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.description.required", "", "description")))
                .put(
                        new Domain("2", "project2", "  ", "fr-FR", Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.description.required", "  ", "description")))
                // Default language tag required
                .put(
                        new Domain("2", "project2", "project2.description", null, Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.defaultLanguageTag.required", "null", "defaultLanguageTag")))
                .put(
                        new Domain("2", "project2", "project2.description", "", Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.defaultLanguageTag.required", "", "defaultLanguageTag")))
                .put(
                        new Domain("2", "project2", "project2.description", "  ", Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.defaultLanguageTag.required", "  ", "defaultLanguageTag")))
                // Default language tag must exist in available language tags
                .put(
                        new Domain("2", "project2", "project2.description", "fr-FR", Collections.singletonList("en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.defaultLanguageTag.matchAvailable", "fr-FR", "defaultLanguageTag")))
                // Default language tag must match an existing locale
                .put(
                        new Domain("2", "project2", "project2.description", "unknown-language-tag", Arrays.asList("fr-FR", "en-US", "unknown-language-tag")),
                        Arrays.asList(new ValidationError(domainClassName, "domain.defaultLanguageTag.unavailable", "unknown-language-tag", "defaultLanguageTag"),
                                new ValidationError(domainClassName, "domain.availableLanguageTags.unavailable", "[fr-FR, en-US, unknown-language-tag]", "availableLanguageTags"))
                )
                // Available language tags can't be empty
                .put(
                        new Domain("2", "project2", "project2.description", "fr-FR", null),
                        singletonList(new ValidationError(domainClassName, "domain.availableLanguageTags.required", "null", "availableLanguageTags")))
                .put(
                        new Domain("2", "project2", "project2.description", "fr-FR", Collections.emptyList()),
                        singletonList(new ValidationError(domainClassName, "domain.availableLanguageTags.required", "[]", "availableLanguageTags")))
                // Available language tags must match existing locales
                .put(
                        new Domain("2", "project2", "project2.description", "fr-FR", Arrays.asList("fr-FR", "unknown-language-tag")),
                        singletonList(new ValidationError(domainClassName, "domain.availableLanguageTags.unavailable", "[fr-FR, unknown-language-tag]", "availableLanguageTags")))
                // Test all invalid properties
                .put(
                        new Domain("2", null, null, null, null),
                        Arrays.asList(
                                new ValidationError(domainClassName, "domain.code.immutable", "null", "code"),
                                new ValidationError(domainClassName, "domain.description.required", "null", "description"),
                                new ValidationError(domainClassName, "domain.defaultLanguageTag.required", "null", "defaultLanguageTag"),
                                new ValidationError(domainClassName, "domain.availableLanguageTags.required", "null", "availableLanguageTags")
                        ))
                .build();
    }

    @Override
    protected Map<Domain, List<ValidationError>> getExpectedValidationErrorsOnDelete() {
        return Collections.emptyMap();
    }

    @Override
    protected Map<Object, List<ValidationError>> getExpectedValidationErrorsOnPatch() {
        return Collections.emptyMap();
    }
}
