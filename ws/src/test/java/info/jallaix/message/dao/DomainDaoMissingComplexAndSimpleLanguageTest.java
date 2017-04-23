package info.jallaix.message.dao;

import info.jallaix.message.dao.interceptor.MissingSimpleMessageException;
import info.jallaix.spring.data.es.test.testcase.BaseDaoElasticsearchTestCase;

import java.util.Locale;

import static org.junit.Assert.fail;

/**
 * <p>The Domain DAO must verify some tests provided by {@link BaseDaoElasticsearchTestCase}.</p>
 * <p>A complex language linked to a supported simple language is used for input and output data.</p>
 * <p>Data don't exist for the complex and simple languages.</p>
 */
public class DomainDaoMissingComplexAndSimpleLanguageTest extends DomainDaoTest {

    /**
     * Apply customization before executing a test.
     *
     * @param domainDaoTestsCustomizer Domain customizer for DAO tests
     */
    @Override
    public void customizeTest(DomainDaoTestsCustomizer domainDaoTestsCustomizer) {

        // Descriptions fixture for default language
        super.customizeTest(domainDaoTestsCustomizer);

        // Set locale to the complex language
        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("es-ES"));
        threadLocaleHolder.setOutputLocales(Locale.LanguageRange.parse("es-ES,es"));
    }

    /**
     * Saving an existing domain replaces the document in the index.
     * An error should occur when trying to insert a domain description for a missing complex language tag
     * when a message doesn't already exist for its simple language tag.
     */
    @Override
    public void saveExistingDocument() {

        try {
            super.saveExistingDocument();
            fail("MissingSimpleMessageException should be thrown.");
        }
        catch (MissingSimpleMessageException e) {

            // Check the domain to update has not been updated
            domainDaoChecker.checkDomainUnmodified(getTestFixture().newExistingDocument());
        }
    }

    /**
     * Indexing an existing domain replaces the document in the index.
     * An error should occur when trying to insert a domain description for a missing complex language tag
     * when a message doesn't already exist for its simple language tag.
     */
    @Override
    public void indexExistingDocument() {

        try {
            super.indexExistingDocument();
            fail("MissingSimpleMessageException should be thrown.");
        } catch (MissingSimpleMessageException e) {

            // Check the domain to update has not been updated
            domainDaoChecker.checkDomainUnmodified(getTestFixture().newExistingDocument());
        }
    }

    /**
     * Saving a list of domains inserts and updates the documents in the index.
     * For creation, a domain description should be inserted in the message's index type for the I18N domain's default language,
     * even if the input locale is defined with a different language.
     * For update, an error should occur when trying to insert a domain description for a missing complex language tag
     * when a message doesn't already exist for its simple language tag.
     */
    @Override
    public void saveDocuments() {

        try {
            super.saveDocuments();
            fail("MissingSimpleMessageException should be thrown.");
        }
        catch (MissingSimpleMessageException e) {

            // Check no new domain was inserted
            domainDaoChecker.checkDomainNotExist(getTestFixture().newDocumentToInsert());
            // Check the domain to update has not been updated
            domainDaoChecker.checkDomainUnmodified(getTestFixture().newExistingDocument());
        }
    }
}
