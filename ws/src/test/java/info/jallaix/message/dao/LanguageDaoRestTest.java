package info.jallaix.message.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.jallaix.message.ApplicationMock;
import info.jallaix.message.dto.Language;
import info.jallaix.spring.data.es.test.SpringDataEsTestCase;
import info.jallaix.spring.data.es.test.TestClientOperations;
import org.dozer.Mapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.hateoas.mvc.TypeReferences;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
public class LanguageDaoRestTest extends SpringDataEsTestCase<Language, String, LanguageDao> {

    @Value("${local.server.port}")
    private int serverPort;

    /**
     * Test client operations
     */
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private TestClientOperations testClientOperations;

    @javax.annotation.Resource
    private Mapper beanMapper;


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
        createLanguage(null, HttpStatus.BAD_REQUEST, true, null);

        // Test invalid code
        createLanguage(new Language(null, null, "Español", "Spanish"), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.code.required", "null", "code")));
        createLanguage(new Language(null, "", "Español", "Spanish"), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.code.required", "", "code")));
        createLanguage(new Language(null, "  ", "Español", "Spanish"), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.code.required", "  ", "code")));

        // Test invalid label
        createLanguage(new Language(null, "esp", null, "Spanish"), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.label.required", "null", "label")));
        createLanguage(new Language(null, "esp", "", "Spanish"), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.label.required", "", "label")));
        createLanguage(new Language(null, "esp", "  ", "Spanish"), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.label.required", "  ", "label")));

        // Test invalid english label
        createLanguage(new Language(null, "esp", "Español", null), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.englishLabel.required", "null", "englishLabel")));
        createLanguage(new Language(null, "esp", "Español", ""), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.englishLabel.required", "", "englishLabel")));
        createLanguage(new Language(null, "esp", "Español", "  "), HttpStatus.BAD_REQUEST, true,
                Collections.singletonList(new ValidationError(Language.class.getSimpleName(), "language.englishLabel.required", "  ", "englishLabel")));

        // Test all invalid properties
        createLanguage(new Language(null, null, null, null), HttpStatus.BAD_REQUEST, true,
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
        createLanguage(newDocumentToUpdate(), HttpStatus.CONFLICT, true, null);
    }

    /**
     * Creating a language entry returns a {@code 201 Created} HTTP status code if the entry doesn't already exist and the language argument is valid.
     * The code value must be trimmed when tested for existence. All property values must be trimmed when created.
     */
    @Test
    public void createValidLanguage() {
        createLanguage(newDocumentToInsert(), HttpStatus.CREATED, false, null);
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
        getLanguage(initial.getId(), HttpStatus.NOT_FOUND, true);
    }

    /**
     * Getting a language entry returns this entry in HATEOAS format and a {@code 200 Ok} HTTP status code if a language is found.
     */
    @Test
    public void findOneExistingLanguage() {

        Resource<Language> initial = convertToResource(newDocumentToUpdate());
        ResponseEntity<Resource<Language>> response = getLanguage(initial.getId(), HttpStatus.OK, false);

        assertThat(response, is(notNullValue()));
        assert response != null;
        assertThat(response.getBody().getLinks(), is(initial.getLinks()));
    }

    /**
     * Getting all language entities sorted returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     */
    @Test
    public void findAllLanguagesSorted() {
        findAllLanguages(null);
    }

    /**
     * Getting all language entities sorted and paged returns these entities in HATEOAS format and a {@code 200 Ok} HTTP status code.
     */
    @Test
    public void findAllPagedLanguagesSorted() {

        findAllLanguages(0);
        findAllLanguages(1);
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                     Tests related to language update                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    /*
     * Updating a language entry returns a {@code 405 Method Not Allowed} HTTP status code if no language identifier is provided.
     */
    @Test
    public void updateLanguageWithoutId() {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType("application/hal+json"));
        HttpEntity<Language> httpEntityNull = new HttpEntity<>(null, httpHeaders);
        try {
            getHalRestTemplate().exchange(
                    getWebServiceUrl(),         // No code provided
                    HttpMethod.PUT,
                    httpEntityNull,
                    new TypeReferences.ResourceType<Language>() {});
            fail("Should return a 405 METHOD NOT ALLOWED response");
        } catch (HttpStatusCodeException e) {

            assertThat(e.getStatusCode(), is(HttpStatus.METHOD_NOT_ALLOWED));
        }
    }

    /**
     * Updating a language entry returns a {@code 400 Bad Request} HTTP status code if there is no language provided.
     */
    @Test
    public void updateInvalidLanguage() {
        updateLanguage("1", null, HttpStatus.BAD_REQUEST, true, null);
    }

    /**
     * Updating a language entry returns an HTTP 404 status code (NOT FOUND) if there is no existing language to update.
     * The code value must be trimmed when tested for existence.
     */
    @Test
    public void updateNonExistingLanguage() {
        updateLanguage("4", new Language(null, "esp", "Español", "Spanish"), HttpStatus.NOT_FOUND, true, null);
    }

    /**
     * Updating an existing language entry returns a {@code 200 Ok} HTTP status code as well as the updated resource that matches the resource in the request.
     */
    @Test
    public void updateValidLanguage() {

        Language languageToUpdate = newDocumentToUpdate();
        ResponseEntity<Resource<Language>> response = updateLanguage(languageToUpdate.getId(), languageToUpdate, HttpStatus.OK, false, null);

        assertThat(response, is(notNullValue()));
        assert response != null;
        assertThat(response.getBody(), is(convertToResource(languageToUpdate)));
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                    Tests related to language deletion                                          */
    /*----------------------------------------------------------------------------------------------------------------*/

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                              Helper methods                                                    */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Get URL of the web service to test
     *
     * @return URL of the web service to test
     */
    private URI getWebServiceUrl() {
        return getWebServiceUrl(false);
    }

    /**
     * Get URL of the web service to test
     *
     * @param profile Indicate if a profile URL is returned
     * @return URL of the web service to test
     */
    private URI getWebServiceUrl(boolean profile) {
        try {
            return new URI("http", null, "localhost", serverPort, (profile ? "/profile" : "") + "/languages", null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid server URI", e);
        }
    }

    /**
     * Convert a language to a resource containing a language with HATEOAS links
     *
     * @param language The language to convert
     * @return The resource containing a language
     */
    private Resource<Language> convertToResource(Language language) {

        Resource<Language> result = new Resource<>(language);
        result.add(new Link(getWebServiceUrl().toString() + "/" + language.getId()));
        result.add(new Link(getWebServiceUrl().toString() + "/" + language.getId(), "language"));
        result.getContent().setId(null);

        return result;
    }

    /**
     * Get expected HATEOAS links when requesting language resources
     *
     * @return A list of HATEOAS links
     */
    private List<Link> getLanguagesLinks() {

        return Arrays.asList(
                new Link(getWebServiceUrl().toString()),
                new Link(getWebServiceUrl(true).toString(), "profile"));
    }

    /**
     * Get expected HATEOAS links when requesting paged language resources
     *
     * @return A list of HATEOAS links
     */
    private List<Link> getPagedLanguagesLinks(int page) {

        final String fieldToSortBy = getSortField().getName();
        final int pageSize = getPageSize();
        final long documentCount = testClientOperations.countDocuments(getDocumentMetadata());
        final long lastPage = documentCount / pageSize - (documentCount % pageSize == 0 ? 1 : 0);

        List<Link> links = new ArrayList<>();
        links.add(new Link(getWebServiceUrl().toString() + "?page=0&size=" + pageSize + "&sort=" + fieldToSortBy + ",desc", "first"));
        if (page > 0)
            links.add(new Link(getWebServiceUrl().toString() + "?page=" + (page - 1) + "&size=" + pageSize + "&sort=" + fieldToSortBy + ",desc", "prev"));
        links.add(new Link(getWebServiceUrl().toString()));
        if (page < lastPage)
            links.add(new Link(getWebServiceUrl().toString() + "?page=" + (page + 1) + "&size=" + pageSize + "&sort=" + fieldToSortBy + ",desc", "next"));
        links.add(new Link(getWebServiceUrl().toString() + "?page=" + lastPage + "&size=" + pageSize + "&sort=" + fieldToSortBy + ",desc", "last"));
        links.add(new Link(getWebServiceUrl(true).toString(), "profile"));

        return links;
    }

    /**
     * Get a HAL REST template
     *
     * @return A HAL REST template
     */
    private RestTemplate getHalRestTemplate() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/hal+json"));
        converter.setObjectMapper(mapper);

        return new RestTemplate(Collections.<HttpMessageConverter<?>>singletonList(converter));
    }

    /**
     * Call the Language REST service to create an entity
     * @param language Language data to create
     * @param expectedStatus Expected HTTP status to assert
     * @param expectedError {@code true} if an error is expected
     * @param expectedErrors Expected validation errors to assert
     * @return The created language resource
     */
    private ResponseEntity<Resource<Language>> createLanguage(Language language, HttpStatus expectedStatus, boolean expectedError, List<ValidationError> expectedErrors) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType("application/hal+json"));
        HttpEntity<Language> httpEntity = new HttpEntity<>(language, httpHeaders);

        try {
            ResponseEntity<Resource<Language>> responseEntity =
                    getHalRestTemplate().exchange(
                            getWebServiceUrl(),
                            HttpMethod.POST,
                            httpEntity,
                            new TypeReferences.ResourceType<Language>() {});

            if (expectedError)
                fail("Should return a " + expectedStatus.value() + " " + expectedStatus.name() + " response");
            else {
                assertThat(responseEntity.getStatusCode(), is(expectedStatus));
                return responseEntity;
            }
        }
        catch (HttpStatusCodeException e) {

            assertThat(e.getStatusCode(), is(expectedStatus));
            if (expectedErrors != null)
                assertThat(findValidationErrors(e).toArray(), is(expectedErrors.toArray()));
        }

        return null;
    }

    /**
     * Call the Language REST service to get an entity
     * @param linkId HATEOAS link to get the entity
     * @param expectedStatus Expected HTTP status to assert
     * @param expectedError {@code true} if an error is expected
     * @return The get language resource
     */
    private ResponseEntity<Resource<Language>> getLanguage(Link linkId, HttpStatus expectedStatus, boolean expectedError) {

        try {
            ResponseEntity<Resource<Language>> responseEntity =
                    getHalRestTemplate().exchange(
                            linkId.getHref(),
                            HttpMethod.GET,
                            null,
                            new TypeReferences.ResourceType<Language>() {});

            if (expectedError)
                fail("Should return a " + expectedStatus.value() + " " + expectedStatus.name() + " response");
            else {
                assertThat(responseEntity.getStatusCode(), is(expectedStatus));
                return responseEntity;
            }
        }
        catch (HttpStatusCodeException e) {
            assertThat(e.getStatusCode(), is(HttpStatus.NOT_FOUND));
        }

        return null;
    }

    /**
     * Call the Language REST service to get all entities
     * @param page {@code null} if no page is request, else a page number starting from 0
     * @return The get language resources
     */
    private ResponseEntity<PagedResources<Language>> findAllLanguages(Integer page) {

        final Field sortField = getSortField();
        final int pageSize = getPageSize();

        List<Language> documents;
        String urlParams = "?sort=" + sortField.getName() + ",desc";
        if (page != null) {
            documents = testClientOperations.findAllDocumentsByPage(getDocumentMetadata(), documentClass, sortField, page, pageSize);
            urlParams += "&page=" + page + "&size=" + pageSize;
        }
        else
            documents = testClientOperations.findAllDocumentsSorted(getDocumentMetadata(), documentClass, sortField);

        // Find initial data
        List<Resource<Language>> fixture = documents
                .stream()
                .map(this::convertToResource)
                .collect(Collectors.toList());

        // Call REST service
        ResponseEntity<PagedResources<Language>> responseEntity =
                getHalRestTemplate().exchange(
                        getWebServiceUrl() + urlParams,
                        HttpMethod.GET,
                        null,
                        new TypeReferences.PagedResourcesType<Language>() {
                        });

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().getContent().toArray(), is(fixture.toArray()));
        if (page != null)
            assertThat(responseEntity.getBody().getLinks().toArray(), is(getPagedLanguagesLinks(page).toArray()));
        else
            assertThat(responseEntity.getBody().getLinks().toArray(), is(getLanguagesLinks().toArray()));

        return responseEntity;
    }

    /**
     * Call the Language REST service to update
     * @param id Identifier of the language resource to update
     * @param language Language data to update
     * @param expectedStatus Expected HTTP status to assert
     * @param expectedError {@code true} if an error is expected
     * @param expectedErrors Expected validation errors to assert
     * @return The updated language resource
     */
   private ResponseEntity<Resource<Language>> updateLanguage(String id, Language language, HttpStatus expectedStatus, boolean expectedError, List<ValidationError> expectedErrors) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType("application/hal+json"));
        HttpEntity<Language> httpEntity = new HttpEntity<>(language, httpHeaders);

        try {
            ResponseEntity<Resource<Language>> responseEntity =
                    getHalRestTemplate().exchange(
                            getWebServiceUrl() + "/" + id,
                            HttpMethod.PUT,
                            httpEntity,
                            new TypeReferences.ResourceType<Language>() {});

            if (expectedError)
                fail("Should return a " + expectedStatus.value() + " " + expectedStatus.name() + " response");
            else {
                assertThat(responseEntity.getStatusCode(), is(expectedStatus));
                return responseEntity;
            }
        }
        catch (HttpStatusCodeException e) {

            assertThat(e.getStatusCode(), is(expectedStatus));
            if (expectedErrors != null)
                assertThat(findValidationErrors(e).toArray(), is(expectedErrors.toArray()));
        }

        return null;
    }

    /**
     * Find validation errors from the HTTP exception body
     * @param httpException HTTP exception that contains validation errors
     * @return The list of validation errors found
     */
    private List<ValidationError> findValidationErrors(HttpStatusCodeException httpException) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            // The "errors" child node contains all validation errors
            JsonNode errorsNode = mapper.readTree(httpException.getResponseBodyAsString()).get("errors");

            // Map each error to a ValidationError object
            TypeReference<ArrayList<ValidationError>> typeRef = new TypeReference<ArrayList<ValidationError>>() {};
            return mapper.readValue(errorsNode.traverse(), typeRef);

        } catch (IOException ioe) {

            fail("Could not convert response body into JSON");
            return new ArrayList<>();
        }
    }
}