package info.jallaix.message.bean;

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
     * Domain 1's english description
     */
    public static final String DOMAIN1_EN_DESCRIPTION = "Internationalized messages";

    /**
     * Domain 1's english US description
     */
    public static final String DOMAIN1_EN_US_DESCRIPTION = "Internationalized messages (US)";

    /**
     * Domain 1's french description
     */
    public static final String DOMAIN1_FR_DESCRIPTION = "Messages internationalisés";

    /**
     * Domain 2's english description
     */
    public static final String DOMAIN2_EN_DESCRIPTION = "Test project 1's description";

    /**
     * Domain 2's french description
     */
    public static final String DOMAIN2_FR_DESCRIPTION = "Description du projet de test 1";

    /**
     * Domain 3's english description
     */
    public static final String DOMAIN3_EN_DESCRIPTION = "Test project 2's description";

    /**
     * Domain 3's english US description
     */
    public static final String DOMAIN3_EN_US_DESCRIPTION = "Test project 2's description (US)";

    /**
     * Domain 3's french description
     */
    public static final String DOMAIN3_FR_DESCRIPTION = "Description du projet de test 2";

    /**
     * Domain 4's english description
     */
    public static final String DOMAIN4_EN_DESCRIPTION = "Test project 3's description";

    /**
     * Domain 4's french description
     */
    public static final String DOMAIN4_FR_DESCRIPTION = "Description du projet de test 3";

    /**
     * Domain 5's english description
     */
    public static final String DOMAIN5_EN_DESCRIPTION = "Test project 4's description";


    /**
     * Return a new document for insertion.
     *
     * @return A document that will be inserted
     */
    @Override
    public Domain newDocumentToInsert() {
        return new Domain("5", "test.project4", DOMAIN5_EN_DESCRIPTION, "es", Arrays.asList("en", "fr", "es"));
    }

    /**
     * Return a new document for update.
     *
     * @return A document that will update an existing one
     */
    @Override
    public Domain newDocumentToUpdate() {
        return new Domain("3", "test.project2", "New project 2's description", "fr", Arrays.asList("fr", "es"));
    }

    /**
     * Return a new existing document.
     *
     * @return A document that exists
     */
    @Override
    public Domain newExistingDocument() {
        return new Domain("3", "test.project2", DOMAIN3_EN_DESCRIPTION, "fr", Arrays.asList("en", "fr", "es"));
    }

    /**
     * Return the sort field
     *
     * @return The sort field
     */
    @Override
    public Field getSortField() {
        return Domain.FIELD_CODE;
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
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "1", "en", DOMAIN1_EN_DESCRIPTION));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "1", "en-US", DOMAIN1_EN_US_DESCRIPTION));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "1", "fr", DOMAIN1_FR_DESCRIPTION));

        storedDocuments.add(new Domain("2", "test.project1", Domain.DOMAIN_DESCRIPTION_TYPE, "en", Arrays.asList("en", "fr", "es")));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "2", "en", DOMAIN2_EN_DESCRIPTION));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "2", "fr", DOMAIN2_FR_DESCRIPTION));

        storedDocuments.add(new Domain("3", "test.project2", Domain.DOMAIN_DESCRIPTION_TYPE, "fr", Arrays.asList("en", "fr", "es")));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "3", "en", DOMAIN3_EN_DESCRIPTION));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "3", "en-US", DOMAIN3_EN_US_DESCRIPTION));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "3", "fr", DOMAIN3_FR_DESCRIPTION));

        storedDocuments.add(new Domain("4", "test.project3", Domain.DOMAIN_DESCRIPTION_TYPE, "en", Arrays.asList("en", "fr", "es")));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "4", "en", DOMAIN4_EN_DESCRIPTION));
        storedDocuments.add(new EntityMessage(null, "1", Domain.DOMAIN_DESCRIPTION_TYPE, "4", "fr", DOMAIN4_FR_DESCRIPTION));

        return storedDocuments;
    }
}
