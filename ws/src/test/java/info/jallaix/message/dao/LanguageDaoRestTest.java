package info.jallaix.message.dao;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.jallaix.message.ApplicationMock;
import info.jallaix.message.dto.Language;
import info.jallaix.message.service.LanguageResource;
import info.jallaix.spring.data.es.test.SpringDataEsTestCase;
import info.jallaix.spring.data.es.test.TestClientOperations;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                      Abstract methods implementation                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    @Override
    protected Language newDocumentToInsert() {
        return new Language("esp", "Español", "Spanish");
    }

    @Override
    protected Language newDocumentToUpdate() {
        return new Language("fra", "Español", "Spanish");
    }

    @Override
    protected int getPageSize() { return 2; }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                    Tests related to language creation                                          */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Creating a language entry returns an HTTP 400 status code (BAD REQUEST) if it has invalid arguments.
     */
    @Test
    public void createInvalidLanguage() {

        // Test null language
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType("application/hal+json"));
        HttpEntity<Language> httpEntityNull = new HttpEntity<>(null, httpHeaders);
        try {
            getHalRestTemplate().exchange(
                    getWebServiceUrl(),
                    HttpMethod.POST,
                    httpEntityNull,
                    new TypeReferences.ResourceType<LanguageResource>() {});
            fail("Should return a 400 BAD REQUEST response");
        }
        catch (HttpStatusCodeException e) {
            assertThat(e.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        }

        // Test invalid code
        callCreateLanguage(null, "Español", "Spanish");
        callCreateLanguage("", "Español", "Spanish");
        callCreateLanguage("  ", "Español", "Spanish");

        // Test invalid label
        callCreateLanguage("esp", null, "Spanish");
        callCreateLanguage("esp", "", "Spanish");
        callCreateLanguage("esp", "  ", "Spanish");

        // Test invalid english label
        callCreateLanguage("esp", "Español", null);
        callCreateLanguage("esp", "Español", "");
        callCreateLanguage("esp", "Español", "  ");
    }

    /**
     * Creating a language entry returns an HTTP 409 status code (CONFLICT) if it already exists.
     */
    @Test
    public void createDuplicateLanguage() {

        // Construct POST content
        Language toInsert = newDocumentToUpdate();                                  // Existing document
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType("application/hal+json"));
        HttpEntity<Language> httpEntity = new HttpEntity<>(toInsert, httpHeaders);

        // Call REST service
        ResponseEntity<Resource<LanguageResource>> responseEntity;
        try {
            responseEntity =
                    getHalRestTemplate().exchange(
                            getWebServiceUrl(),
                            HttpMethod.POST,
                            httpEntity,
                            new TypeReferences.ResourceType<LanguageResource>() {});
        }
        catch (HttpStatusCodeException e) {

            assertThat(e.getStatusCode(), is(HttpStatus.CONFLICT));
            return;
        }

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.CONFLICT));
        assertThat(responseEntity.getBody().getContent(), is(nullValue()));
    }

    /**
     * Creating a language entry returns an HTTP 201 status code (CREATED) if it doesn't already exist.
     * @throws HttpStatusCodeException If the language creation fails
     */
    @Test
    public void createValidLanguage() throws HttpStatusCodeException {

        // Construct POST content
        Language toInsert = newDocumentToInsert();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType("application/hal+json"));
        HttpEntity<Language> httpEntity = new HttpEntity<>(toInsert, httpHeaders);

        // Call REST service
        ResponseEntity<LanguageResource> responseEntity =
                    getHalRestTemplate().exchange(
                            getWebServiceUrl(),
                            HttpMethod.POST,
                            httpEntity,
                            LanguageResource.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(responseEntity.getBody().getLinks(), is(convertToResource(toInsert).getLinks()));
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                     Tests related to language search                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Getting all language entries returns these entries in HAL format and an HTTP 200 status code (OK).
     */
    @Test
    public void findAllLanguages() {

        // Find initial data
        List<LanguageResource> fixture = testClientOperations.findAllDocuments(getDocumentMetadata(), Language.class)
                .stream()
                .map(this::convertToResource)
                .collect(Collectors.toList());

        // Call REST service
        ResponseEntity<PagedResources<LanguageResource>> responseEntity =
                getHalRestTemplate().exchange(
                        getWebServiceUrl(),
                        HttpMethod.GET,
                        null,
                        new TypeReferences.PagedResourcesType<LanguageResource>() {});

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().getContent().toArray(), is(fixture.toArray()));
    }

    /**
     * Getting an existing language entry returns this entry in HAL format and an HTTP 200 status code (OK).
     */
    @Test
    public void findOneExistingLanguage() {

        LanguageResource initial = convertToResource(newDocumentToUpdate());

        // Call REST service
        ResponseEntity<LanguageResource> responseEntity =
                getHalRestTemplate().exchange(
                        initial.getId().getHref(),
                        HttpMethod.GET,
                        null,
                        LanguageResource.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().getLinks(), is(initial.getLinks()));
    }

    /**
     * Getting a missing language entry returns an HTTP 404 status code (NOT FOUND).
     */
    @Test
    public void findOneMissingLanguage() {

        LanguageResource initial = convertToResource(newDocumentToInsert());

        // Call REST service
        ResponseEntity<Resource<LanguageResource>> responseEntity;
        try {
            responseEntity =
                    getHalRestTemplate().exchange(
                            initial.getId().getHref(),
                            HttpMethod.GET,
                            null,
                            new TypeReferences.ResourceType<LanguageResource>() {});
        }
        catch (HttpStatusCodeException e) {

            assertThat(e.getStatusCode(), is(HttpStatus.NOT_FOUND));
            return;
        }

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(responseEntity.getBody().getContent(), is(nullValue()));
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                     Tests related to language update                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                    Tests related to language deletion                                          */
    /*----------------------------------------------------------------------------------------------------------------*/

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                    Tests related to language deletion                                          */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Get URL of the web service to test
     * @return URL of the web service to test
     */
    private URI getWebServiceUrl() {
        try {
            return new URI("http", null, "localhost", serverPort, "/languages/", null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid server URI", e);
        }
    }

    /**
     * Get a HAL REST template
     * @return A HAL REST template
     */
    private RestTemplate getHalRestTemplate() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/hal+json"));
        converter.setObjectMapper(mapper);

        return new RestTemplate(Collections.<HttpMessageConverter<?>> singletonList(converter));
    }

    /**
     * Convert a language to a resource containing a language
     * @param language The language to convert
     * @return The resource containing a language
     */
    private LanguageResource convertToResource(Language language) {

        LanguageResource result = new LanguageResource();
        result.setLabel(language.getLabel());
        result.setEnglishLabel(language.getEnglishLabel());

        result.add(new Link(getWebServiceUrl().toString() + language.getCode()));
        result.add(new Link(getWebServiceUrl().toString() + language.getCode(), "language"));

        return result;
    }

    /**
     * Call the REST service for language creation with given properties
     * @param code The language code
     * @param label The language label
     * @param englishLabel The language english label
     */
    private void callCreateLanguage(String code, String label, String englishLabel) {

        Language language = new Language();
        language.setCode(code);
        language.setLabel(label);
        language.setEnglishLabel(englishLabel);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType("application/hal+json"));
        HttpEntity<Language> httpEntityEmptyCode = new HttpEntity<>(language, httpHeaders);
        try {
            getHalRestTemplate().exchange(
                    getWebServiceUrl(),
                    HttpMethod.POST,
                    httpEntityEmptyCode,
                    new TypeReferences.ResourceType<LanguageResource>() {});
            fail("Should return a 400 BAD REQUEST response");
        }
        catch (HttpStatusCodeException e) {
            assertThat(e.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        }
    }
}