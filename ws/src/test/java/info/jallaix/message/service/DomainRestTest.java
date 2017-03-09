package info.jallaix.message.service;

import com.google.common.collect.ImmutableMap;
import info.jallaix.message.ApplicationMock;
import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.dao.MessageDao;
import info.jallaix.message.dto.Domain;
import info.jallaix.message.dto.EntityMessage;
import info.jallaix.spring.data.es.test.bean.ValidationError;
import info.jallaix.spring.data.es.test.testcase.BaseRestElasticsearchTestCase;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.TypeReferences;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by Julien on 22/01/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ApplicationMock.class)
@WebIntegrationTest(randomPort = true)
public class DomainRestTest extends BaseRestElasticsearchTestCase<Domain, String, DomainDao> {

    @Autowired
    private Domain messageDomain;

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
        return new Domain("2", "project2", "project4.description", "fr-FR", Arrays.asList("en-US", "fr-FR", "es-ES"));
    }

    @Override
    protected Object newObjectForPatch() {

        return new Object() {
            @SuppressWarnings("unused")
            public String getDescription() {
                return "project4.description";
            }

            @SuppressWarnings("unused")
            public Collection<String> getAvailableLanguageTags() {
                return Arrays.asList("en-GB", "fr-FR", "es-ES", "fr-CA");
            }
        };
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
    protected int getPageSize() {
        return 2;
    }

    /**
     * Return a set of domains and messages that are sorted in the Elasticsearch index.
     *
     * @return A set of domains and messages
     */
    @Override
    protected List<?> getStoredDocuments() {

        List<Object> storedDocuments = new ArrayList<>(13);

        storedDocuments.add(new Domain("1", "i18n.message", "info.jallaix.message.dto.Domain.description", "en", Arrays.asList("en", "fr", "es")));
        storedDocuments.add(new EntityMessage(null, "1", "info.jallaix.message.dto.Domain.description", "1", "en", "Internationalized messages"));
        storedDocuments.add(new EntityMessage(null, "1", "info.jallaix.message.dto.Domain.description", "1", "en-US", "Internationalized messages (US)"));
        storedDocuments.add(new EntityMessage(null, "1", "info.jallaix.message.dto.Domain.description", "1", "fr", "Messages internationalisés"));

        storedDocuments.add(new Domain("2", "test.project1", "info.jallaix.message.dto.Domain.description", "en", Arrays.asList("en", "fr", "es")));
        storedDocuments.add(new EntityMessage(null, "1", "info.jallaix.message.dto.Domain.description", "2", "en", "Test project 1's description"));
        storedDocuments.add(new EntityMessage(null, "1", "info.jallaix.message.dto.Domain.description", "2", "fr", "Description du projet de test 1"));

        storedDocuments.add(new Domain("3", "test.project2", "info.jallaix.message.dto.Domain.description", "fr", Arrays.asList("en", "fr", "es")));
        storedDocuments.add(new EntityMessage(null, "1", "info.jallaix.message.dto.Domain.description", "3", "en", "Test project 2's description"));
        storedDocuments.add(new EntityMessage(null, "1", "info.jallaix.message.dto.Domain.description", "3", "fr", "Description du projet de test 2"));

        storedDocuments.add(new Domain("4", "test.project3", "info.jallaix.message.dto.Domain.description", "en", Arrays.asList("en", "fr", "es")));
        storedDocuments.add(new EntityMessage(null, "1", "info.jallaix.message.dto.Domain.description", "4", "en", "Test project 3's description"));
        storedDocuments.add(new EntityMessage(null, "1", "info.jallaix.message.dto.Domain.description", "4", "fr", "Description du projet de test 3"));

        return storedDocuments;
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
                // Default language tag can't change
                .put(
                        new Domain("2", "project2", "project2.description", "en-US", Arrays.asList("fr-FR", "en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.defaultLanguageTag.immutable", "en-US", "defaultLanguageTag")))
                // Default language tag must exist in available language tags
                .put(
                        new Domain("2", "project2", "project2.description", "fr-FR", Collections.singletonList("en-US")),
                        singletonList(new ValidationError(domainClassName, "domain.defaultLanguageTag.matchAvailable", "fr-FR", "defaultLanguageTag")))
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
                                new ValidationError(domainClassName, "domain.defaultLanguageTag.immutable", "null", "defaultLanguageTag"),
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

    @javax.annotation.Resource
    private String domain;

    @Autowired
    private MessageDao messageDao;

    @Override
    public void createValidEntity() {
        ResponseEntity<Resource<Domain>> httpResponse = postEntity(newDocumentToInsert(), HttpStatus.CREATED, false);

        Domain savedDomain = httpResponse.getBody().getContent();
        String languageTag = savedDomain.getDefaultLanguageTag();
        String messageType = savedDomain.getDescription();

        EntityMessage message = messageDao.findByDomainIdAndTypeAndEntityIdAndLanguageTag(messageDomain.getId(), messageType, savedDomain.getId(), languageTag);
        assertThat(message, is(notNullValue()));
    }
}
