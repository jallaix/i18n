package info.jallaix.message.service;

import info.jallaix.message.dao.LanguageDao;
import info.jallaix.message.dto.Language;
import info.jallaix.message.service.exception.DuplicateLanguageException;
import info.jallaix.message.service.exception.LanguageInvalidArgumentException;
import info.jallaix.message.service.exception.LanguageNotFoundException;
import org.easymock.*;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * The Language REST web service must verify the following tests related to <b>language creation</b> :
 * <ul>
 *     <li>
 *         Creating a language entry returns an HTTP 400 status code (BAD REQUEST) if there is an invalid argument:
 *         <ol>
 *             <li>no language</li>
 *             <li>language with null or empty code</li>
 *             <li>language with null or empty label</li>
 *             <li>language with null or empty english label</li>
 *         </ol>
 *     </li>
 *     <li>
 *         Creating a language entry returns an HTTP 409 status code (CONFLICT) if it already exists.
 *         The code value must be trimmed when tested for existence.
 *     </li>
 *     <li>
 *         Creating a language entry returns an HTTP 201 status code (CREATED) if the entry doesn't already exist and the language argument is valid.
 *         The code value must be trimmed when tested for existence. All property values must be trimmed when created.
 *     </li>
 * </ul>
 * <br/>
 * The Language REST web service must verify the following tests related to <b>language search</b> :
 * <ul>
 *     <li>Getting a language entry returns an HTTP 404 status code (NOT FOUND) if there is no language found.</li>
 *     <li>Getting a language entry returns this entry and an HTTP 200 status code (OK) if a language is found.</li>
 *     <li>Getting all language entries returns these entries and an HTTP 200 status code (OK).</li>
 * </ul>
 * <br/>
 * The Language REST web service must verify the following tests related to <b>language update</b> :
 * <ul>
 *     <li>
 *         Updating a language entry returns an HTTP 400 status code (BAD REQUEST) if there is an invalid argument:
 *         <ol>
 *             <li>no language</li>
 *             <li>language with null or empty code</li>
 *             <li>language with empty label (null is accepted)</li>
 *             <li>language with empty english label (null is accepted)</li>
 *         </ol>
 *     </li>
 *     <li>
 *         Updating a language entry returns an HTTP 404 status code (NOT FOUND) if there is no existing language to update.
 *         The code value must be trimmed when tested for existence.
 *     </li>
 *     <li>
 *         Updating a language entry returns an HTTP 204 status code (NO CONTENT) if the entry already exists and the language argument is valid.
 *         The code value must be trimmed when tested for existence. All property values must be trimmed when updated.
 *     </li>
 *     <li>Updating a language entry with null properties (except code) only updates the properties set.</li>
 * </ul>
 * <br/>
 * The Language REST web service must verify the following tests related to <b>language deletion</b> :
 * <ul>
 *     <li>Deleting a language entry returns an HTTP 404 status code (NOT FOUND) if there is no language found.</li>
 *     <li>Deleting a language entry returns an HTTP 200 status code (OK) if a language is found.</li>
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
     * Creating a language entry returns an HTTP 400 status code (BAD REQUEST) if there is an invalid argument:
     * <ol>
     *     <li>no language</li>
     *     <li>language with null or empty code</li>
     *     <li>language with null or empty label</li>
     *     <li>language with null or empty english label</li>
     * </ol>
     */
    @Test
    public void createInvalidLanguage() {

        validateLanguageToCreate(null, "null language didn't throw an exception");  // Null language

        validateLanguageToCreate(
                new Language(null, "Español", "Spanish"),
                "null language code didn't throw an exception");                    // Null code
        validateLanguageToCreate(
                new Language("", "Español", "Spanish"),
                "empty language code didn't throw an exception");                   // Empty code
        validateLanguageToCreate(
                new Language("  ", "Español", "Spanish"),
                "language code with spaces didn't throw an exception");             // Code that contains spaces

        validateLanguageToCreate(
                new Language("esp", null, "Spanish"),
                "null language label didn't throw an exception");                   // Null label
        validateLanguageToCreate(
                new Language("esp", "", "Spanish"),
                "empty language label didn't throw an exception");                  // Empty label
        validateLanguageToCreate(
                new Language("esp", "  ", "Spanish"),
                "language label with spaces didn't throw an exception");            // Label that contains spaces

        validateLanguageToCreate(
                new Language("esp", "Español", null),
                "null english language label didn't throw an exception");           // Null english label
        validateLanguageToCreate(
                new Language("esp", "Español", ""),
                "empty english language label didn't throw an exception");          // Empty english label
        validateLanguageToCreate(
                new Language("esp", "Español", "  "),
                "english language label with spaces didn't throw an exception");    // English language that contains spaces
    }

    /**
     * Creating a language entry returns an HTTP 409 status code (CONFLICT) if it already exists.
     * The code value must be trimmed when tested for existence.
     */
    @Test
    public void createDuplicateLanguage() {

        // Mocking "languageDao"
        expect(languageDao.exists("esp")).andReturn(true);
        replayAll();

        // Execute test with mock
        try {
            service.create(new Language(" esp ", "Español", "Spanish"));
            fail("duplicate language didn't throw an exception");
        }
        catch (DuplicateLanguageException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.CONFLICT);    // Validate status code
        }
        verifyAll();
    }

    /**
     * Creating a language entry returns an HTTP 201 status code (CREATED) if the entry doesn't already exist and the language argument is valid.
     * The code value must be trimmed when tested for existence. All property values must be trimmed when created.
     * @throws NoSuchMethodException
     */
    @Test
    public void createValidAndNewLanguage() throws NoSuchMethodException {

        // Mocking "languageDao"
        expect(languageDao.exists("esp")).andReturn(false);
        expect(languageDao.index(new Language("esp", "Español", "Spanish"))).andReturn(new Language("esp", "Español", "Spanish"));
        replayAll();

        // Execute test with mock
        service.create(new Language(" esp ", " Español ", " Spanish "));
        verifyAll();

        // Validate status code
        validateReturnedStatusCode(service.getClass().getDeclaredMethod("create", Language.class), HttpStatus.CREATED);
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                     Tests related to language search                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Getting a language entry returns an HTTP 404 status code (NOT FOUND) if there is no language found.
     * @throws NoSuchMethodException
     */
    @Test
    public void searchLanguageNotFound() throws NoSuchMethodException {

        // Mocking "languageDao"
        expect(languageDao.findOne("esp")).andReturn(null);
        replayAll();

        // Execute test with mock
        try {
            service.get(" esp ");
            fail("empty search with key didn't throw an exception");
        }
        catch (LanguageNotFoundException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.NOT_FOUND);
        }
        verifyAll();
    }

    /**
     * Getting a language entry returns an HTTP 200 status code (OK) if a language is found.
     * @throws NoSuchMethodException
     */
    @Test
    public void searchLanguageFound() throws NoSuchMethodException {

        // Mocking "languageDao"
        expect(languageDao.findOne("esp")).andReturn(new Language("esp", "Español", "Spanish"));
        replayAll();

        // Execute test with mock
        assertNotNull(service.get(" esp "));
        verifyAll();

        // Validate status code
        validateReturnedStatusCode(service.getClass().getDeclaredMethod("get", String.class), HttpStatus.OK);
    }

    /**
     * Getting all language entries returns an HTTP 200 status code (OK).
     * @throws NoSuchMethodException
     */
    @Test
    public void searchLanguages() throws NoSuchMethodException {

        // Mocking "languageDao"
        expect(languageDao.findAll()).andReturn(Collections.<Language>emptyList());
        replayAll();

        // Execute test with mock
        assertArrayEquals(Collections.<Language>emptyList().toArray(), service.get().toArray());
        verifyAll();

        // Mocking "languageDao"
        resetAll();
        expect(languageDao.findAll()).andReturn(Arrays.<Language>asList(
                new Language("esp", "Español", "Spanish"),
                new Language("fra", "Français", "French")
        ));
        replayAll();

        // Execute test with mock
        assertArrayEquals(Arrays.asList(
                new Language("esp", "Español", "Spanish"),
                new Language("fra", "Français", "French")
        ).toArray(), service.get().toArray());
        verifyAll();

        // Validate status code
        validateReturnedStatusCode(service.getClass().getDeclaredMethod("get"), HttpStatus.OK);
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                     Tests related to language update                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Updating a language entry returns an HTTP 400 status code (BAD REQUEST) if there is an invalid argument:
     * <ol>
     *     <li>no language</li>
     *     <li>language with null or empty code</li>
     *     <li>language with empty label (null is accepted)</li>
     *     <li>language with empty english label (null is accepted)</li>
     * </ol>
     */
    @Test
    public void updateInvalidLanguage() {

        validateLanguageToUpdate(null, "null language didn't throw an exception");

        validateLanguageToUpdate(
                new Language("", "Español", "Spanish"),
                "empty language code didn't throw an exception");   // Empty code
        validateLanguageToUpdate(
                new Language("  ", "Español", "Spanish"),
                "language code with spaces didn't throw an exception"); // Code that contains spaces

        validateLanguageToUpdate(
                new Language("esp", "", "Spanish"),
                "empty language code didn't throw an exception");   // Empty label
        validateLanguageToUpdate(
                new Language("esp", "  ", "Spanish"),
                "language code with spaces didn't throw an exception"); // Label that contains spaces

        validateLanguageToUpdate(
                new Language("esp", "Español", ""),
                "empty language code didn't throw an exception");   // Empty english label
        validateLanguageToUpdate(
                new Language("esp", "Español", "  "),
                "language code with spaces didn't throw an exception"); // English label that contains spaces
    }

    /**
     * Updating a language entry returns an HTTP 404 status code (NOT FOUND) if there is no existing language to update.
     * The code value must be trimmed when tested for existence.
     */
    @Test
    public void updateLanguageNotFound() {

        // Mocking "languageDao"
        expect(languageDao.exists("esp")).andReturn(false);
        replayAll();

        // Execute test with mock
        try {
            service.update(new Language(" esp ", "Español", "Spanish"));
            fail("update on non existing language didn't throw an exception");
        }
        catch (LanguageNotFoundException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.NOT_FOUND);
        }
        verifyAll();
    }

    /**
     * Updating a language entry returns an HTTP 204 status code (NO CONTENT) if the entry already exists and the language argument is valid.
     * The code value must be trimmed when tested for existence. All property values must be trimmed when updated.
     * @throws NoSuchMethodException
     */
    @Test
    public void updateLanguageFound() throws NoSuchMethodException {

        // Mocking "languageDao"
        expect(languageDao.exists("esp")).andReturn(true);
        expect(languageDao.save(new Language("esp", "Español", "Spanish"))).andReturn(new Language("esp", "Español", "Spanish"));
        replayAll();

        // Execute test with mock
        service.update(new Language(" esp ", " Español ", " Spanish "));
        verifyAll();

        // Validate status code
        validateReturnedStatusCode(service.getClass().getDeclaredMethod("update", Language.class), HttpStatus.OK);
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                    Tests related to language deletion                                          */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Deleting a language entry returns an HTTP 404 status code (NOT FOUND) if there is no language found.
     */
    @Test
    public void deleteLanguageNotFound() {

        // Mocking "languageDao"
        expect(languageDao.exists("esp")).andReturn(false);
        replayAll();

        // Execute test with mock
        try {
            service.delete(" esp ");
            fail("delete on non existing language didn't throw an exception");
        }
        catch (LanguageNotFoundException e) {
            validateExceptionHttpStatusCode(e, HttpStatus.NOT_FOUND);
        }
        verifyAll();
    }

    /**
     * Deleting a language entry returns an HTTP 200 status code (OK) if a language is found.
     * @throws NoSuchMethodException
     */
    @Test
    public void deleteLanguageFound() throws NoSuchMethodException {

        // Mocking "languageDao"
        expect(languageDao.exists("esp")).andReturn(true);
        languageDao.delete("esp");
        replayAll();

        // Execute test with mock
        service.delete(" esp ");
        verifyAll();

        // Validate status code
        validateReturnedStatusCode(service.getClass().getDeclaredMethod("delete", String.class), HttpStatus.OK);
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
     * Validate language properties for update
     * @param language The language to test
     * @param failMessage Message in case of failure
     */
    private void validateLanguageToUpdate(Language language, String failMessage) {

        try {
            service.update(language);
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