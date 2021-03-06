package info.jallaix.message.dao;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.bean.EntityMessage;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import info.jallaix.spring.data.es.test.customizer.BaseDaoTestsCustomizer;
import info.jallaix.spring.data.es.test.fixture.ElasticsearchTestFixture;
import org.elasticsearch.common.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Julien on 11/03/2017.
 */
public class DomainDaoTestsCustomizer extends BaseDaoTestsCustomizer<Domain> {

    /**
     * Domain testing checks
     */
    private DomainDaoChecker domainDaoChecker;

    /**
     * Locale data holder
     */
    private ThreadLocaleHolder threadLocaleHolder;

    /**
     * Test fixture
     */
    private ElasticsearchTestFixture<Domain> testFixture;

    /**
     * Serialization framework
     */
    private Kryo kryo;

    /**
     * Descriptions fixture used when finding many domains
     */
    private Map<String, String> descriptionsFixture;

    /**
     * Description fixture used when finding one domain
     */
    private String descriptionFixture;

    /**
     * Language tag used for input data
     */
    private String inputLanguageTag;

    /**
     * Language ranges used for output data
     */
    private String outputLanguageRanges;

    /**
     * Language tag expected in the HTTP response
     */
    private String responseLanguageTag;


    /**
     * Constructor with domain DAO checker.
     *
     * @param domainDaoChecker The domain DAO checker
     */
    public DomainDaoTestsCustomizer(
            DomainDaoChecker domainDaoChecker,
            ThreadLocaleHolder threadLocaleHolder,
            ElasticsearchTestFixture<Domain> testFixture,
            Kryo kryo) {

        this.domainDaoChecker = domainDaoChecker;
        this.threadLocaleHolder = threadLocaleHolder;
        this.testFixture = testFixture;
        this.kryo = kryo;
    }

    /**
     * Check the integrity of domain and messages.
     * A domain description should be inserted in the message's index type for the I18N domain's default language.
     *
     * @param toInsert Domain to insert
     * @param inserted Inserted domain
     */
    @Override
    public void customizeSaveNewDocument(Domain toInsert, Domain inserted) {
        domainDaoChecker.checkNewDocumentMessage(inserted);
    }

    /**
     * Get the initial list of messages before saving a domain.
     *
     * @param toUpdate Domain to update
     * @return The initial list of messages
     */
    public Object getCustomDataOnSaveExistingDocument(Domain toUpdate) {
        return domainDaoChecker.getMessages(toUpdate.getId());
    }

    /**
     * Check the integrity of domain and messages.
     * Domain description for the default locale should be updated in the message's index type.
     *
     * @param toUpdate   Domain to update
     * @param updated    Updated domain
     * @param customData Custom data
     */
    public void customizeSaveExistingDocument(Domain toUpdate, Domain updated, Object customData) {

        @SuppressWarnings("unchecked")
        final List<EntityMessage> originalMessages = (List<EntityMessage>) customData;

        domainDaoChecker.checkExistingDocumentMessages(updated, threadLocaleHolder.getInputLocale(), originalMessages);
    }

    /**
     * Get the initial list of messages before saving some domains.
     *
     * @param toSave Documents to save
     * @return The custom data
     */
    public Object getCustomDataOnSaveDocuments(List<Domain> toSave) {
        return domainDaoChecker.getMessages(testFixture.newDocumentToUpdate().getId());
    }

    /**
     * Check the integrity of domains and messages.
     * On creation, it also inserts the domains descriptions in the message's index type for each I18N domain's supported languages.
     * On update, it also updates the domain description for the default locale in the message's index type.
     *
     * @param toSave Domains to save
     * @param saved  Saved domains
     */
    public void customizeSaveDocuments(List<Domain> toSave, List<Domain> saved, Object customData) {

        @SuppressWarnings("unchecked")
        final List<EntityMessage> originalMessages = (List<EntityMessage>) customData;

        domainDaoChecker.checkNewDocumentMessage(testFixture.newDocumentToInsert());
        domainDaoChecker.checkExistingDocumentMessages(testFixture.newDocumentToUpdate(), threadLocaleHolder.getInputLocale(), originalMessages);
    }

    /**
     * Internationalize domain descriptions for the {@link info.jallaix.spring.data.es.test.testcase.BaseDaoElasticsearchTestCase#findAllDocuments()} and so on tests.
     *
     * @param fixture The list of domains to internationalize
     * @return The list of internationalized domains
     */
    @Override
    public List<Domain> customizeFindAllFixture(final List<Domain> fixture) {
        if (descriptionsFixture == null)
            return fixture;
        else
            return domainDaoChecker.internationalizeDomains(fixture, descriptionsFixture);
    }

    /**
     * Internationalize domain description for the {@link info.jallaix.spring.data.es.test.testcase.BaseDaoElasticsearchTestCase#findOneExistingDocument()} test.
     *
     * @param fixture The domain to internationalize
     * @return The internationalized domain
     */
    @Override
    public Domain customizeFindOneFixture(final Domain fixture) {
        if (descriptionFixture == null)
            return fixture;
        else
            return domainDaoChecker.internationalizeDomain(fixture, descriptionFixture);
    }

    /**
     * Check the integrity of domain and messages.
     * No message should reference any domain.
     */
    @Override
    public void customizeDeleteAll() {
        assertThat(domainDaoChecker.getMessages(), hasSize(0));
    }

    /**
     * Check the integrity of domain and messages.
     * No message should reference the deleted domains.
     *
     * @param toDelete List of domains to delete
     */
    @Override
    public void customizeDeleteSet(List<Domain> toDelete) {

        List<String> domainIds = toDelete.stream().map(Domain::getId).collect(Collectors.toList());
        assertThat(domainDaoChecker.getMessages(domainIds), hasSize(0));
    }

    /**
     * Check the integrity of domain and messages.
     * No message should reference the deleted domain.
     *
     * @param id Identifier of the deleted domain
     */
    @Override
    public void customizeDeleteOne(String id) {
        assertThat(domainDaoChecker.getMessages(id), hasSize(0));
    }

    /**
     * Update an HTTP entity before it's sent to the server.
     *
     * @param httpEntity The original HTTP entity
     * @return The updated HTTP entity
     */
    @Override
    public HttpEntity<?> customizeHttpEntity(final HttpEntity<?> httpEntity) {

        // Copy original headers
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(httpEntity.getHeaders());

        // Define language headers
        if (StringUtils.isNotBlank(inputLanguageTag))
            headers.add(HttpHeaders.CONTENT_LANGUAGE, inputLanguageTag);
        if (StringUtils.isNotBlank(outputLanguageRanges))
            headers.add(HttpHeaders.ACCEPT_LANGUAGE, outputLanguageRanges);

        return new HttpEntity<>(httpEntity.getBody(), headers);
    }

    /**
     * Assert that the HTTP response contains the expected language tag.
     *
     * @param response The HTTP response
     */
    @Override
    public void assertResponse(ResponseEntity<?> response) {

        if (StringUtils.isNotBlank(responseLanguageTag))
            assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_LANGUAGE), is(responseLanguageTag));
    }

    /**
     * Set descriptions fixture used when finding many domains.
     *
     * @param descriptionsFixture Descriptions fixture used when finding many domains
     */
    public void setDescriptionsFixture(Map<String, String> descriptionsFixture) {
        this.descriptionsFixture = descriptionsFixture;
    }

    /**
     * Set description fixture used when finding one domain.
     *
     * @param descriptionFixture Description fixture used when finding many domains
     */
    public void setDescriptionFixture(String descriptionFixture) {
        this.descriptionFixture = descriptionFixture;
    }

    /**
     * Set the language tag used for input data
     *
     * @param inputLanguageTag The language tag used for input data
     */
    public void setInputLanguageTag(String inputLanguageTag) {
        this.inputLanguageTag = inputLanguageTag;
    }

    /**
     * Set the language ranges used for output data
     *
     * @param outputLanguageRanges The language ranges used for output data
     */
    public void setOutputLanguageRanges(String outputLanguageRanges) {
        this.outputLanguageRanges = outputLanguageRanges;
    }

    /**
     * Set the language tag expected in the HTTP response.
     *
     * @param responseLanguageTag Language tag expected in the HTTP response
     */
    public void setResponseLanguageTag(String responseLanguageTag) {
        this.responseLanguageTag = responseLanguageTag;
    }
}
