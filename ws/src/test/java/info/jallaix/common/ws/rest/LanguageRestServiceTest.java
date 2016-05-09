package info.jallaix.common.ws.rest;

import info.jallaix.common.dao.LanguageDao;
import info.jallaix.common.dto.Language;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * The <b>Language</b> REST web service must verify the following tests related to language creation :
 * <ul>
 *     <li>Creating a language entry returns an HTTP 400 status code (BAD REQUEST) if there is no language argument.</li>
 *     <li>Creating a language entry returns an HTTP 400 status code (BAD REQUEST) if there is a language argument with null or empty code.</li>
 *     <li>Creating a language entry returns an HTTP 400 status code (BAD REQUEST) if there is a language argument with null or empty label.</li>
 *     <li>Creating a language entry returns an HTTP 400 status code (BAD REQUEST) if there is a language argument with null or empty english label.</li>
 *     <li>Creating a language entry returns an HTTP 201 status code (CREATED) if the entry doesn't already exist and the language argument is valid.</li>
 *     <li>Creating a language entry returns an HTTP 409 status code (CONFLICT) if it already exists.</li>
 * </ul>
 * <br/>
 * The <b>Language</b> REST web service must verify the following tests related to language update :
 * <ul>
 *     <li>Creating a language entry returns a 400 HTTP error (BAD REQUEST) code if there is no language argument.</li>
 *     <li>Creating a language entry returns a 400 HTTP error (BAD REQUEST) code if there is a language argument with null or empty code.</li>
 *     <li>Creating a language entry returns a 400 HTTP error (BAD REQUEST) code if there is a language argument with null or empty label.</li>
 *     <li>Creating a language entry returns a 400 HTTP error (BAD REQUEST) code if there is a language argument with null or empty english label.</li>
 *     <li>Creating a language entry returns no error if the entry doesn't already exist and the language argument is valid.</li>
 *     <li>Creating a language entry returns a 409 HTTP error (CONFLICT) code if it already exists.</li>
 * </ul>
 */
public class LanguageRestServiceTest extends EasyMockSupport {

    @Rule
    public EasyMockRule rule = new EasyMockRule(this);
    @TestSubject
    private final LanguageRestService service = new LanguageRestService();
    @Mock
    private LanguageDao languageDao;


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                    Tests related to language creation                                          */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Creating a language entry returns an HTTP 400 status code (BAD REQUEST) if there is not language argument.
     */
    @Test
    public void createLanguageNull() {

        validateLanguageToCreate(null, "null language didn't throw an exception");
    }

    /**
     * Creating a language entry returns an HTTP 400 status code (BAD REQUEST) if there is a language argument with null or empty code.
     */
    @Test
    public void createLanguageWithEmptyCode() {

        validateLanguageToCreate(
                new Language(null, "Español", "Spanish"),
                "null language code didn't throw an exception");    // Null code
        validateLanguageToCreate(
                new Language("", "Español", "Spanish"),
                "empty language code didn't throw an exception");   // Empty code
        validateLanguageToCreate(
                new Language("  ", "Español", "Spanish"),
                "language code with spaces didn't throw an exception"); // Code that contains spaces
    }

    /**
     * Creating a language entry returns an HTTP 400 status code (BAD REQUEST) if there is a language argument with null or empty label.
     */
    @Test
    public void createLanguageWithEmptyLabel() {

        validateLanguageToCreate(
                new Language("esp", null, "Spanish"),
                "null language label didn't throw an exception");   // Null label
        validateLanguageToCreate(
                new Language("esp", "", "Spanish"),
                "empty language label didn't throw an exception");  // Empty label
        validateLanguageToCreate(
                new Language("esp", "  ", "Spanish"),
                "language label with spaces didn't throw an exception");    // Label that contains spaces
    }


    /**
     * Creating a language entry returns an HTTP 400 status code (BAD REQUEST) if there is a language argument with null or empty english label.
     */
    @Test
    public void createLanguageWithEmptyEnglishLabel() {

        validateLanguageToCreate(
                new Language("esp", "Español", null),
                "null english language label didn't throw an exception");   // Null english label
        validateLanguageToCreate(
                new Language("esp", "Español", ""),
                "empty english language label didn't throw an exception");  // Empty english label
        validateLanguageToCreate(
                new Language("esp", "Español", "  "),
                "english language label with spaces didn't throw an exception");    // English language that contains spaces
    }

    /**
     * Creating a language entry returns an HTTP 201 status code (CREATED) if the entry doesn't already exist and the language argument is valid.
     */
    @Test
    public void createValidAndNewLanguage() throws NoSuchMethodException {

        Language language = new Language("esp", "Español", "Spanish");

        // Mocking "languageDao"
        expect(languageDao.get("esp")).andReturn(Optional.<Language>empty());
        languageDao.create(language);
        replayAll();

        // Execute test with mock
        service.create(language);
        verifyAll();

        // Validate status code
        validateReturnedStatusCode(service.getClass().getDeclaredMethod("create", Language.class), HttpStatus.CREATED);
    }

    /**
     * Creating a language entry returns an HTTP 409 status code (CONFLICT) if it already exists.
     */
    @Test
    public void createDuplicateLanguage() {

        expect(languageDao.get("esp")).andReturn(Optional.of(new Language("esp", "Español", "Spanish")));
        replayAll();

        try {
            service.create(new Language("esp", "Español", "Spanish"));
            fail("duplicate language didn't throw an exception");
        }
        catch (DuplicateLanguageException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.CONFLICT);
        }
        verifyAll();
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                              Private methods                                                   */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Validate language properties for creation
     * @param language The language to test
     * @param failMessage Message in case of failure
     */
    private void validateLanguageToCreate(Language language, String failMessage) {

        try {
            service.create(language);
            fail(failMessage);
        } catch (LanguageInvalidArgumentException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Validate that an exception returns an HTTP status code
     * @param e Exception to validate
     * @param status The HTTP status code
     */
    private void validateExceptionHttpStatusCode(Exception e, HttpStatus status) {

        ResponseStatus annotation = e.getClass().getAnnotation(ResponseStatus.class);
        if (annotation == null ||
                annotation.value() == null ||
                !annotation.value().equals(status))
            fail("LanguageInvalidArgumentException must send an HTTP " + status + " status code");
    }

    /**
     * Validate that a method returns an HTTP status code
     * @param method Method to validate
     * @param status The HTTP status code
     */
    private void validateReturnedStatusCode(Method method, HttpStatus status) {

        ResponseStatus responseStatus = method.getDeclaredAnnotation(ResponseStatus.class);
        if (responseStatus == null || !responseStatus.value().equals(status))
            fail(method + " method must send an HTTP " +  status + " status code");
    }
}