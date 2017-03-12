package info.jallaix.message.bean;

import info.jallaix.message.dao.DomainDaoTest;
import info.jallaix.spring.data.es.test.fixture.ElasticsearchTestFixture;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fixture for domain-related tests.
 */
public class DomainTestFixture implements ElasticsearchTestFixture<Domain> {

    /**
     * Return a new document for insertion.
     *
     * @return A document that will be inserted
     */
    @Override
    public Domain newDocumentToInsert() {
        return new Domain("5", "test.project4", "Test project 4's description", "es", Arrays.asList("en", "fr", "es"));
    }

    /**
     * Return a new document for update.
     *
     * @return A document that will update an existing one
     */
    @Override
    public Domain newDocumentToUpdate() {
        return new Domain("3", "test.project2", "New project 2's description", "es", Arrays.asList("en", "fr", "es"));
    }

    /**
     * Return a new existing document.
     *
     * @return A document that exists
     */
    @Override
    public Domain newExistingDocument() {
        return new Domain("3", "test.project2", DomainDaoTest.DOMAIN3_EN_DESCRIPTION, "fr", Arrays.asList("en", "fr", "es"));
    }

    /**
     * Return the sort field
     *
     * @return The sort field
     */
    @Override
    public Field getSortField() {

        try {
            return Domain.class.getDeclaredField("code");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the size of a page to get
     *
     * @return The size of a page to get
     */
    @Override
    public int getPageSize() {
        return 2;
    }

    /**
     * Return a set of domains and messages that are sorted in the Elasticsearch index.
     *
     * @return A set of domains and messages
     */
    @Override
    public List<?> getStoredDocuments() {

        List<Object> storedDocuments = new ArrayList<>(13);

        storedDocuments.add(new Domain("1", "i18n.message", Domain.DOMAIN_DESCRIPTION_TYPE, "en", Arrays.asList("en", "fr", "es")));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "1", "en", "Internationalized messages"));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "1", "en-US", "Internationalized messages (US)"));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "1", "fr", "Messages internationalisés"));

        storedDocuments.add(new Domain("2", "test.project1", Domain.DOMAIN_DESCRIPTION_TYPE, "en", Arrays.asList("en", "fr", "es")));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "2", "en", "Test project 1's description"));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "2", "fr", "Description du projet de test 1"));

        storedDocuments.add(new Domain("3", "test.project2", Domain.DOMAIN_DESCRIPTION_TYPE, "fr", Arrays.asList("en", "fr", "es")));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "3", "en", DomainDaoTest.DOMAIN3_EN_DESCRIPTION));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "3", "fr", DomainDaoTest.DOMAIN3_FR_DESCRIPTION));

        storedDocuments.add(new Domain("4", "test.project3", Domain.DOMAIN_DESCRIPTION_TYPE, "en", Arrays.asList("en", "fr", "es")));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "4", "en", "Test project 3's description"));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "4", "fr", "Description du projet de test 3"));

        return storedDocuments;
    }
}
