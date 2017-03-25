package info.jallaix.message.bean;

import com.google.common.collect.ImmutableMap;
import info.jallaix.spring.data.es.test.fixture.RestElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.bean.ValidationError;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.TypeReferences;

import java.util.*;

import static java.util.Collections.singletonList;

/**
 * Fixture for domain-related tests with REST.
 */
public class DomainRestTestFixture implements RestElasticsearchTestFixture<Domain> {

    @Override
    public Object newObjectForPatch() {

        return new Object() {
            @SuppressWarnings("unused")
            public String getDescription() {
                return "New project 2's description";
            }

            @SuppressWarnings("unused")
            public Collection<String> getAvailableLanguageTags() {
                return Arrays.asList("en", "fr", "es", "de");
            }
        };
    }

    @Override
    public TypeReferences.ResourceType<Domain> getResourceType() {
        return new TypeReferences.ResourceType<Domain>() {
        };
    }

    @Override
    public TypeReferences.PagedResourcesType<Resource<Domain>> getPagedResourcesType() {
        return new TypeReferences.PagedResourcesType<Resource<Domain>>() {
        };
    }

    /**
     * Expected validation errors on create:
     * <ol>
     * <li>domain with null or empty code</li>
     * <li>domain with null or empty description</li>
     * <li>domain with null or empty default language tag</li>
     * <li>default language tag not present in available language tags</li>
     * <li>default language tag not matching an existing locale</li>
     * <li>domain with null or empty list of available language tags</li>
     * <li>available language tags not matching existing locales</li>
     * </ol>
     *
     * @return A map of languages linked to a list of expected validation errors
     */
    @Override
    public Map<Domain, List<ValidationError>> getExpectedValidationErrorsOnCreate() {

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
                // Description required
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
                        new Domain("2", "project2", "project2.description", "fr-FR", singletonList("en-US")),
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

    /**
     * Expected validation errors on update:
     * <ol>
     * <li>domain with new code</li>
     * <li>domain with null or empty description</li>
     * <li>domain with new default language tag</li>
     * <li>default language tag not present in available language tags</li>
     * <li>default language tag not matching an existing locale</li>
     * <li>domain with null or empty list of available language tags</li>
     * <li>available language tags not matching existing locales</li>
     * </ol>
     *
     * @return A map of languages linked to a list of expected validation errors
     */
    @Override
    public Map<Domain, List<ValidationError>> getExpectedValidationErrorsOnUpdate() {

        final String domainClassName = Domain.class.getSimpleName();

        return ImmutableMap.<Domain, List<ValidationError>>builder()
                // Code can't change
                .put(
                        new Domain("2", "invalid.code", DomainTestFixture.DOMAIN2_EN_DESCRIPTION, "en", Arrays.asList("en", "fr", "es")),
                        singletonList(new ValidationError(domainClassName, "domain.code.immutable", "invalid.code", "code")))
                // Test invalid description
                .put(
                        new Domain("2", "test.project1", null, "en", Arrays.asList("en", "fr", "es")),
                        singletonList(new ValidationError(domainClassName, "domain.description.required", "null", "description")))
                .put(
                        new Domain("2", "test.project1", "", "en", Arrays.asList("en", "fr", "es")),
                        singletonList(new ValidationError(domainClassName, "domain.description.required", "", "description")))
                .put(
                        new Domain("2", "test.project1", "  ", "en", Arrays.asList("en", "fr", "es")),
                        singletonList(new ValidationError(domainClassName, "domain.description.required", "  ", "description")))
                // Default language tag can't change
                .put(
                        new Domain("2", "test.project1", DomainTestFixture.DOMAIN2_EN_DESCRIPTION, "fr", Arrays.asList("en", "fr", "es")),
                        singletonList(new ValidationError(domainClassName, "domain.defaultLanguageTag.immutable", "fr", "defaultLanguageTag")))
                // Default language tag must exist in available language tags
                .put(
                        new Domain("2", "test.project1", DomainTestFixture.DOMAIN2_EN_DESCRIPTION, "en", singletonList("fr")),
                        singletonList(new ValidationError(domainClassName, "domain.defaultLanguageTag.matchAvailable", "en", "defaultLanguageTag")))
                // Available language tags can't be empty
                .put(
                        new Domain("2", "test.project1", DomainTestFixture.DOMAIN2_EN_DESCRIPTION, "en", null),
                        singletonList(new ValidationError(domainClassName, "domain.availableLanguageTags.required", "null", "availableLanguageTags")))
                .put(
                        new Domain("2", "test.project1", DomainTestFixture.DOMAIN2_EN_DESCRIPTION, "en", Collections.emptyList()),
                        singletonList(new ValidationError(domainClassName, "domain.availableLanguageTags.required", "[]", "availableLanguageTags")))
                // Available language tags must match existing locales
                .put(
                        new Domain("2", "test.project1", DomainTestFixture.DOMAIN2_EN_DESCRIPTION, "en", Arrays.asList("en", "unknown-language-tag")),
                        singletonList(new ValidationError(domainClassName, "domain.availableLanguageTags.unavailable", "[en, unknown-language-tag]", "availableLanguageTags")))
                // Test all invalid properties
                .put(
                        new Domain("2", null, null, null, null),
                        Arrays.asList(
                                new ValidationError(domainClassName, "domain.code.immutable", "null", "code"),
                                new ValidationError(domainClassName, "domain.description.required", "null", "description"),
                                new ValidationError(domainClassName, "domain.defaultLanguageTag.immutable", "null", "defaultLanguageTag"),
                                new ValidationError(domainClassName, "domain.availableLanguageTags.required", "null", "availableLanguageTags")
                        ))
                .build();
    }

    @Override
    public Map<Domain, List<ValidationError>> getExpectedValidationErrorsOnDelete() {
        return Collections.emptyMap();
    }

    @Override
    public Map<Object, List<ValidationError>> getExpectedValidationErrorsOnPatch() {
        return Collections.emptyMap();
    }
}
