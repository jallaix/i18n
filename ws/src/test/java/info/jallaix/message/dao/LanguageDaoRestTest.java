package info.jallaix.message.dao;

import info.jallaix.message.ApplicationMock;
import info.jallaix.message.dto.Language;
import info.jallaix.spring.data.es.test.SpringDataRestEsTestCase;
import info.jallaix.spring.data.es.test.ValidationError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.TypeReferences;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * <p>
 * The Language REST web service must verify the following tests related to <b>language creation</b> :
 * <ul>
 *     <li>
 *         Creating a language entry returns a {@code 400 Bad Request} HTTP status code if there is an invalid argument:
 *         <ol>
 *             <li>no language</li>
 *             <li>language with null or empty code</li>
 *             <li>language with null or empty label</li>
 *             <li>language with null or empty english label</li>
 *         </ol>
 *     </li>
 *     <li>
 *         Creating a language entry returns a {@code 409 Conflict} HTTP status code if it already exists.
 *         The code value must be trimmed when tested for existence.
 *     </li>
 *     <li>
 *         Creating a language entry returns a {@code 201 Created} HTTP status code if the entry doesn't already exist and the language argument is valid.
 *         The code value must be trimmed when tested for existence. All property values must be trimmed when created.
 *     </li>
 * </ul>
 *
 * <p>
 * The Language REST web service must verify the following tests related to <b>language search</b> :
 * <ul>
 *     <li>Getting a language entry returns a {@code 404 Not Found} HTTP status code if there is no language found.</li>
 *     <li>Getting a language entry returns this entry in HATEOAS format and a {@code 200 Ok} HTTP status code if a language is found.</li>
 *     <li>Getting all language entries returns these entries in HATEOAS format and a {@code 200 Ok} HTTP status code.</li>
 * </ul>
 *
 * <p>
 * The Language REST web service must verify the following tests related to <b>language update</b> :
 * <ul>
 *     <li>Updating a language entry returns a {@code 405 Method Not Allowed} HTTP status code if no language identifier is provided.</li>
 *     <li>Updating a language entry returns a {@code 400 Bad Request} HTTP status code if there is no language provided.</li>
 *     <li>
 *         Updating a language entry returns a {@code 404 Not Found} HTTP status code if there is no existing language to update.
 *     </li>
 *     <li>Updating an existing language entry returns a {@code 200 Ok} HTTP status code as well as the updated resource that matches the resource in the request.</li>
 * </ul>
 *
 * <p>
 * The Language REST web service must verify the following tests related to <b>language deletion</b> :
 * <ul>
 *     <li>Deleting a language entry returns an HTTP 404 status code (NOT FOUND) if there is no language found.</li>
 *     <li>Deleting a language entry returns an HTTP 200 status code (OK) if a language is found.</li>
 * </ul>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ApplicationMock.class)
@WebIntegrationTest(randomPort = true)
public class LanguageDaoRestTest extends SpringDataRestEsTestCase<Language, String, LanguageDao> {

    @Value("${local.server.port}")
    private int serverPort;


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                      Abstract methods implementation                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    @Override
    protected Language newDocumentToInsert() {
        return new Language("4", "esp", "Español", "Spanish");
    }

    @Override
    protected Language newDocumentToUpdate() {
        return new Language("2", "esp", "Español", "Spanish");
    }

    @Override
    protected Field getSortField() {

        try {
            return Language.class.getDeclaredField("code");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected int getPageSize() {
        return 2;
    }

    @Override
    protected TypeReferences.ResourceType<Language> getResourceType() {
        return new TypeReferences.ResourceType<Language>() {};
    }

    @Override
    protected TypeReferences.PagedResourcesType<Resource<Language>> getPagedResourcesType() {
        return new TypeReferences.PagedResourcesType<Resource<Language>>() {};
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                    Tests related to language creation                                          */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Creating a language entry returns a {@code 400 Bad Request} HTTP status code if there is an invalid argument:
     * <ol>
     * <li>no language</li>
     * <li>language with null or empty code</li>
     * <li>language with null or empty label</li>
     * <li>language with null or empty english label</li>
     * </ol>
     */
    @Test
    public void createInvalidLanguage() {

        // Test null language
        postEntity(null, HttpStatus.BAD_REQUEST, true);

        // Test invalid code
        postEntity(new Language(null, null, "Español", "Spanish"), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.code.required", "null", "code")));
        postEntity(new Language(null, "", "Español", "Spanish"), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.code.required", "", "code")));
        postEntity(new Language(null, "  ", "Español", "Spanish"), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.code.required", "  ", "code")));

        // Test invalid label
        postEntity(new Language(null, "esp", null, "Spanish"), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.label.required", "null", "label")));
        postEntity(new Language(null, "esp", "", "Spanish"), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.label.required", "", "label")));
        postEntity(new Language(null, "esp", "  ", "Spanish"), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.label.required", "  ", "label")));

        // Test invalid english label
        postEntity(new Language(null, "esp", "Español", null), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.englishLabel.required", "null", "englishLabel")));
        postEntity(new Language(null, "esp", "Español", ""), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.englishLabel.required", "", "englishLabel")));
        postEntity(new Language(null, "esp", "Español", "  "), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.englishLabel.required", "  ", "englishLabel")));

        // Test all invalid properties
        postEntity(new Language(null, null, null, null), HttpStatus.BAD_REQUEST, true,
                Arrays.asList(
                        new ValidationError(Language.class.getSimpleName(), "language.code.required", "null", "code"),
                        new ValidationError(Language.class.getSimpleName(), "language.label.required", "null", "label"),
                        new ValidationError(Language.class.getSimpleName(), "language.englishLabel.required", "null", "englishLabel")
                ));
    }

    /**
     * Creating a language entry returns a {@code 409 Conflict} HTTP status code if it already exists.
     * The code value must be trimmed when tested for existence.
     */
    @Test
    public void createDuplicateLanguage() {
        postEntity(newDocumentToUpdate(), HttpStatus.CONFLICT, true);
    }

    /**
     * Creating a language entry returns a {@code 201 Created} HTTP status code if the entry doesn't already exist and the language argument is valid.
     * The code value must be trimmed when tested for existence. All property values must be trimmed when created.
     */
    @Test
    public void createValidLanguage() {
        postEntity(newDocumentToInsert(), HttpStatus.CREATED, false);
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                     Tests related to language search                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Getting a language entry returns a {@code 404 Not Found} HTTP status code if there is no language found.
     */
    @Test
    public void findOneMissingLanguage() {

        Resource<Language> initial = convertToResource(newDocumentToInsert());
        getEntity(initial.getId(), HttpStatus.NOT_FOUND, true);
    }

    /**
     * Getting a language entry returns this entry in HATEOAS format and a {@code 200 Ok} HTTP status code if a language is found.
     */
    @Test
    public void findOneExistingLanguage() {

        Resource<Language> initial = convertToResource(newDocumentToUpdate());
        ResponseEntity<Resource<Language>> response = getEntity(initial.getId(), HttpStatus.OK, false);

        assertThat(response, is(notNullValue()));
        assert response != null;
        assertThat(response.getBody().getLinks(), is(initial.getLinks()));
    }

    /**
     * Getting all language entities sorted returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     */
    @Test
    public void findAllLanguagesSorted() {
        getEntities();
    }

    /**
     * Getting all language entities sorted and paged returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     */
    @Test
    public void findAllPagedLanguagesSorted() {

        getEntities(0);
        getEntities(1);
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                     Tests related to language update                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    /*
     * Updating a language entry returns a {@code 405 Method Not Allowed} HTTP status code if no language identifier is provided.
     */
    @Test
    public void updateLanguageWithoutId() { putEntity(null, null, HttpStatus.METHOD_NOT_ALLOWED, true); }

    /**
     * Updating a language entry returns a {@code 400 Bad Request} HTTP status code if there is no language provided.
     */
    @Test
    public void updateInvalidLanguage() {
        putEntity("1", null, HttpStatus.BAD_REQUEST, true);
    }

    /**
     * Updating a language entry returns an HTTP 404 status code (NOT FOUND) if there is no existing language to update.
     * The code value must be trimmed when tested for existence.
     */
    @Test
    public void updateNonExistingLanguage() {
        putEntity("4", new Language(null, "esp", "Español", "Spanish"), HttpStatus.NOT_FOUND, true);
    }

    /**
     * Updating an existing language entry returns a {@code 200 Ok} HTTP status code as well as the updated resource that matches the resource in the request.
     */
    @Test
    public void updateValidLanguage() {

        Language languageToUpdate = newDocumentToUpdate();
        ResponseEntity<Resource<Language>> response = putEntity(languageToUpdate.getId(), languageToUpdate, HttpStatus.OK, false);

        assertThat(response, is(notNullValue()));
        assert response != null;
        assertThat(response.getBody(), is(convertToResource(languageToUpdate)));
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                    Tests related to language deletion                                          */
    /*----------------------------------------------------------------------------------------------------------------*/
}