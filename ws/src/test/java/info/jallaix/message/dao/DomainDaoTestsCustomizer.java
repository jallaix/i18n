package info.jallaix.message.dao;

import com.esotericsoftware.kryo.Kryo;
import info.jallaix.message.bean.Domain;
import info.jallaix.message.bean.EntityMessage;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import info.jallaix.spring.data.es.test.fixture.ElasticsearchTestFixture;
import info.jallaix.spring.data.es.test.customizer.BaseDaoTestsCustomizer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
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

    private Map<String, String> descriptionsFixture;
    public void setDescriptionsFixture(Map<String, String> descriptionsFixture) {
        this.descriptionsFixture = descriptionsFixture;
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

    private String descriptionFixture;
    public void setDescriptionFixture(String descriptionFixture) {
        this.descriptionFixture = descriptionFixture;
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
}
