package info.jallaix.message.service;

import com.google.common.collect.ImmutableMap;
import info.jallaix.message.ApplicationMock;
import info.jallaix.message.dao.LanguageDao;
import info.jallaix.message.dto.Language;
import info.jallaix.spring.data.es.test.bean.ValidationError;
import info.jallaix.spring.data.es.test.testcase.BaseRestElasticsearchTestCase;
import info.jallaix.spring.data.es.test.testcase.RestTestedMethod;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.TypeReferences;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * The Language REST web service must verify the following tests related to <b>language update</b> :
 * <ul>
 * <li>Updating a language entry returns a {@code 405 Method Not Allowed} HTTP status code if no language identifier is provided.</li>
 * <li>Updating a language entry returns a {@code 400 Bad Request} HTTP status code if there is no language provided.</li>
 * <li>
 * Updating a language entry returns a {@code 404 Not Found} HTTP status code if there is no existing language to update.
 * </li>
 * <li>Updating an existing language entry returns a {@code 200 Ok} HTTP status code as well as the updated resource that matches the resource in the request.</li>
 * </ul>
 * <p/>
 * <p/>
 * The Language REST web service must verify the following tests related to <b>language deletion</b> :
 * <ul>
 * <li>Deleting a language entry returns an HTTP 404 status code (NOT FOUND) if there is no language found.</li>
 * <li>Deleting a language entry returns an HTTP 200 status code (OK) if a language is found.</li>
 * </ul>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ApplicationMock.class)
@WebIntegrationTest(randomPort = true)
public class LanguageRestTest extends BaseRestElasticsearchTestCase<Language, String, LanguageDao> {

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                      Abstract methods implementation                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Constructor that defines the tests to pass
     */
    public LanguageRestTest() {
        super(
                RestTestedMethod.Create.class,
                RestTestedMethod.Update.class,
                RestTestedMethod.Patch.class,
                RestTestedMethod.FindAll.class,
                RestTestedMethod.Exist.class,
                RestTestedMethod.Delete.class);
    }

    @Override
    protected Language newDocumentToInsert() {
        return new Language("4", "esp", "Español", "Spanish");
    }

    @Override
    protected Language newDocumentToUpdate() {
        return new Language("2", "esp", "Español", "Spanish");
    }

    @Override
    protected Language newExistingDocument() {
        return new Language("2", "fra", "Français", "French");
    }

    @Override
    protected Field getSortField() {

        try {
            Field f = Language.class.getDeclaredField("code");
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
            public String getCode() { return "esp"; }
            @SuppressWarnings("unused")
            public String getLabel() { return "Español"; }
        };
    }

    @Override
    protected int getPageSize() {
        return 2;
    }

    @Override
    protected TypeReferences.ResourceType<Language> getResourceType() {
        return new TypeReferences.ResourceType<Language>() {
        };
    }

    @Override
    protected TypeReferences.PagedResourcesType<Resource<Language>> getPagedResourcesType() {
        return new TypeReferences.PagedResourcesType<Resource<Language>>() {
        };
    }

    /**
     * Expected validation errors:
     * <ol>
     * <li>language with null or empty code</li>
     * <li>language with null or empty label</li>
     * <li>language with null or empty english label</li>
     * </ol>
     *
     * @return A map of languages linked to a list of expected validation errors
     */
    @Override
    protected Map<Language, List<ValidationError>> getExpectedValidationErrorsOnCreate() {

        final String languageClassName = Language.class.getSimpleName();

        return ImmutableMap.<Language, List<ValidationError>>builder()
                // Test invalid code
                .put(
                        new Language("2", null, "Español", "Spanish"),
                        Collections.singletonList(new ValidationError(languageClassName, "language.code.required", "null", "code")))
                .put(
                        new Language("2", "", "Español", "Spanish"),
                        Collections.singletonList(new ValidationError(languageClassName, "language.code.required", "", "code")))
                .put(
                        new Language("2", "  ", "Español", "Spanish"),
                        Collections.singletonList(new ValidationError(languageClassName, "language.code.required", "  ", "code")))
                // Test invalid label
                .put(
                        new Language("2", "esp", null, "Spanish"),
                        Collections.singletonList(new ValidationError(languageClassName, "language.label.required", "null", "label")))
                .put(
                        new Language("2", "esp", "", "Spanish"),
                        Collections.singletonList(new ValidationError(languageClassName, "language.label.required", "", "label")))
                .put(
                        new Language("2", "esp", "  ", "Spanish"),
                        Collections.singletonList(new ValidationError(languageClassName, "language.label.required", "  ", "label")))
                // Test invalid english label
                .put(
                        new Language("2", "esp", "Español", null),
                        Collections.singletonList(new ValidationError(languageClassName, "language.englishLabel.required", "null", "englishLabel")))
                .put(
                        new Language("2", "esp", "Español", ""),
                        Collections.singletonList(new ValidationError(languageClassName, "language.englishLabel.required", "", "englishLabel")))
                .put(
                        new Language("2", "esp", "Español", "  "),
                        Collections.singletonList(new ValidationError(languageClassName, "language.englishLabel.required", "  ", "englishLabel")))
                // Test all invalid properties
                .put(
                        new Language("2", null, null, null),
                        Arrays.asList(
                                new ValidationError(languageClassName, "language.code.required", "null", "code"),
                                new ValidationError(languageClassName, "language.label.required", "null", "label"),
                                new ValidationError(languageClassName, "language.englishLabel.required", "null", "englishLabel")
                        ))
                .build();
    }
    @Override
    protected Map<Language, List<ValidationError>> getExpectedValidationErrorsOnUpdate() {
        return getExpectedValidationErrorsOnCreate();
    }

    /**
     * Expected validation errors: language with linked messages
     */
    @Override
    protected Map<Language, List<ValidationError>> getExpectedValidationErrorsOnDelete() {

        Language language = new Language("1", "eng", "English", "English");

        return ImmutableMap.<Language, List<ValidationError>>builder()
                // Test invalid code
                .put(
                        language,
                        Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.message.existing", language.getCode(), "code")))
                .build();
    }

    /**
     * Expected validation errors:
     * <ol>
     * <li>language with null or empty code</li>
     * <li>language with null or empty label</li>
     * <li>language with null or empty english label</li>
     * </ol>
     *
     * @return A map of languages linked to a list of expected validation errors
     */
    @Override
    protected Map<Object, List<ValidationError>> getExpectedValidationErrorsOnPatch() {

        final String languageClassName = Language.class.getSimpleName();

        return ImmutableMap.<Object, List<ValidationError>>builder()
                // Test invalid code
                .put(
                        new Object() { @SuppressWarnings("unused") public String getCode() { return null; }},
                        Collections.singletonList(new ValidationError(languageClassName, "language.code.required", "null", "code")))
                .put(
                        new Object() { @SuppressWarnings("unused") public String getCode() { return ""; }},
                        Collections.singletonList(new ValidationError(languageClassName, "language.code.required", "", "code")))
                .put(
                        new Object() { @SuppressWarnings("unused") public String getCode() { return "  "; }},
                        Collections.singletonList(new ValidationError(languageClassName, "language.code.required", "  ", "code")))
                // Test invalid label
                .put(
                        new Object() { @SuppressWarnings("unused") public String getLabel() { return null; }},
                        Collections.singletonList(new ValidationError(languageClassName, "language.label.required", "null", "label")))
                .put(
                        new Object() { @SuppressWarnings("unused") public String getLabel() { return ""; }},
                        Collections.singletonList(new ValidationError(languageClassName, "language.label.required", "", "label")))
                .put(
                        new Object() { @SuppressWarnings("unused") public String getLabel() { return "  "; }},
                        Collections.singletonList(new ValidationError(languageClassName, "language.label.required", "  ", "label")))
                // Test invalid english label
                .put(
                        new Object() { @SuppressWarnings("unused") public String getEnglishLabel() { return null; }},
                        Collections.singletonList(new ValidationError(languageClassName, "language.englishLabel.required", "null", "englishLabel")))
                .put(
                        new Object() { @SuppressWarnings("unused") public String getEnglishLabel() { return ""; }},
                        Collections.singletonList(new ValidationError(languageClassName, "language.englishLabel.required", "", "englishLabel")))
                .put(
                        new Object() { @SuppressWarnings("unused") public String getEnglishLabel() { return "  "; }},
                        Collections.singletonList(new ValidationError(languageClassName, "language.englishLabel.required", "  ", "englishLabel")))
                // Test all invalid properties
                .put(
                        new Object() {
                            @SuppressWarnings("unused") public String getCode() { return null; }
                            @SuppressWarnings("unused") public String getLabel() { return null; }
                            @SuppressWarnings("unused") public String getEnglishLabel() { return null; }
                        },
                        Arrays.asList(
                                new ValidationError(languageClassName, "language.code.required", "null", "code"),
                                new ValidationError(languageClassName, "language.label.required", "null", "label"),
                                new ValidationError(languageClassName, "language.englishLabel.required", "null", "englishLabel")
                        ))
                .build();
    }
}