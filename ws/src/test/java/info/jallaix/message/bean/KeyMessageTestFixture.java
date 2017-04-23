package info.jallaix.message.bean;

import info.jallaix.spring.data.es.test.fixture.ElasticsearchTestFixture;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Fixture for key message-related tests.
 */
public class KeyMessageTestFixture implements ElasticsearchTestFixture<KeyMessage> {

    /**
     * Return a new document for insertion.
     *
     * @return A document that will be inserted
     */
    @Override
    public KeyMessage newDocumentToInsert() {
        return new KeyMessage("4", "1", "key2", "en", DomainTestFixture.DOMAIN2_EN_DESCRIPTION);
    }

    /**
     * Return a new document for update.
     *
     * @return A document that will update an existing one
     */
    @Override
    public KeyMessage newDocumentToUpdate() {
        return new KeyMessage("1", "1", "key1", "en", DomainTestFixture.DOMAIN1_EN_US_DESCRIPTION);
    }

    /**
     * Return a new existing document.
     *
     * @return A document that exists
     */
    @Override
    public KeyMessage newExistingDocument() {
        return new KeyMessage("2", "1", "key1", "fr", DomainTestFixture.DOMAIN1_FR_DESCRIPTION);
    }

    /**
     * Return the sort field
     *
     * @return The sort field
     */
    @Override
    public Field getSortField() {
        return EntityMessage.FIELD_LANGUAGE_TAG;
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
     * Return the list of document to store in the index before each test
     *
     * @return The list of document to store in the index
     */
    @Override
    public List<?> getStoredDocuments() {

        List<Object> storedDocuments = new ArrayList<>(3);

        storedDocuments.add(new KeyMessage("1", "1", "key1", "en", DomainTestFixture.DOMAIN1_EN_DESCRIPTION));
        storedDocuments.add(new KeyMessage("2", "1", "key1", "fr", DomainTestFixture.DOMAIN1_FR_DESCRIPTION));
        storedDocuments.add(new KeyMessage("3", "1", "key1", "en-US", DomainTestFixture.DOMAIN1_EN_US_DESCRIPTION));

        return storedDocuments;
    }
}
