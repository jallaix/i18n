package info.jallaix.common.ws.rest;

import info.jallaix.common.dto.Language;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.Assert.*;

/**
 * Created by Julien on 08/05/2016.
 *
 * The language REST service must verify the following tests :
 * <ul>
 *     <li>Creating a language entry returns the inserted language if it doesn't already exist</li>
 *     <li>Creating a language entry returns a 400 HTTP error (BAD REQUEST) code if there is not language argument</li>
 *     <li>Creating a language entry returns a 400 HTTP error (BAD REQUEST) code if there is a language argument with null or empty code</li>
 *     <li>Creating a language entry returns a 400 HTTP error (BAD REQUEST) code if there is a language argument with null or empty label</li>
 *     <li>Creating a language entry returns a 400 HTTP error (BAD REQUEST) code if there is a language argument with null or empty english label</li>
 *     <li>Creating a language entry returns a 409 HTTP error (CONFLICT) code if it already exists</li>
 * </ul>
 */
public class LanguageRestServiceTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * Creating a language entry returns the inserted language if it doesn't already exist
     */
    @Test
    public void createNotExistingLanguage() {

        LanguageRestService service = new LanguageRestService();
        Language language = new Language("esp", "Español", "Spanish");
        Language createdLanguage = service.create(language);

        assertEquals(createdLanguage, language);
    }

    /**
     * Creating a language entry returns a 400 HTTP error (BAD REQUEST) code if there is not language argument
     */
    @Test
    public void createLanguageNull() {

        LanguageRestService service = new LanguageRestService();

        try {
            service.create(null);
            fail("null language didn't throw an exception");
        }
        catch (LanguageInvalidArgumentException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Creating a language entry returns a 400 HTTP error (BAD REQUEST) code if there is a language argument with null or empty code
     */
    @Test
    public void createLanguageWithEmptyCode() {

        LanguageRestService service = new LanguageRestService();

        // Test language with null code
        try {
            Language language = new Language(null, "Español", "Spanish");
            service.create(language);
            fail("null language code didn't throw an exception");
        } catch (LanguageInvalidArgumentException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.BAD_REQUEST);
        }

        // Test language with empty code
        try {
            Language language = new Language("", "Español", "Spanish");
            service.create(language);
            fail("empty language code didn't throw an exception");
        } catch (LanguageInvalidArgumentException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.BAD_REQUEST);
        }

        // Test language with code that contains spaces
        try {
            Language language = new Language("  ", "Español", "Spanish");
            service.create(language);
            fail("language code with spaces didn't throw an exception");
        } catch (LanguageInvalidArgumentException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Creating a language entry returns a 400 HTTP error (BAD REQUEST) code if there is a language argument with null or empty label
     */
    @Test
    public void createLanguageWithEmptyLabel() {

        LanguageRestService service = new LanguageRestService();

        // Test language with null label
        try {
            Language language = new Language("esp", null, "Spanish");
            service.create(language);
            fail("null language label didn't throw an exception");
        } catch (LanguageInvalidArgumentException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.BAD_REQUEST);
        }

        // Test language with empty label
        try {
            Language language = new Language("esp", "", "Spanish");
            service.create(language);
            fail("empty language label didn't throw an exception");
        } catch (LanguageInvalidArgumentException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.BAD_REQUEST);
        }

        // Test language with label that contains spaces
        try {
            Language language = new Language("esp", "  ", "Spanish");
            service.create(language);
            fail("language label with spaces didn't throw an exception");
        } catch (LanguageInvalidArgumentException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Creating a language entry returns a 400 HTTP error (BAD REQUEST) code if there is a language argument with null or empty english label
     */
    @Test
    public void createLanguageWithEmptyEnglishLabel() {

        LanguageRestService service = new LanguageRestService();

        // Test language with null english label
        try {
            Language language = new Language("esp", "Español", null);
            service.create(language);
            fail("null english language label didn't throw an exception");
        }
        catch (LanguageInvalidArgumentException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.BAD_REQUEST);
        }

        // Test language with empty english label
        try {
            Language language = new Language("esp", "Español", "");
            service.create(language);
            fail("empty english language label didn't throw an exception");
        }
        catch (LanguageInvalidArgumentException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.BAD_REQUEST);
        }

        // Test language with english language that contains spaces
        try {
            Language language = new Language("esp", "Español", "  ");
            service.create(language);
            fail("empty english language label didn't throw an exception");
        }
        catch (LanguageInvalidArgumentException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Validate that an exception is associated with an HTTP error code
     * @param e Exception to validate
     * @param status The HTTP status code
     */
    private void validateExceptionHttpStatusCode(Exception e, HttpStatus status) {

        ResponseStatus annotation = e.getClass().getAnnotation(ResponseStatus.class);
        if (annotation == null ||
                annotation.value() == null ||
                !annotation.value().equals(HttpStatus.BAD_REQUEST))
            fail("LanguageInvalidArgumentException must send an HTTP 400 BAD_REQUEST status code");
    }
}