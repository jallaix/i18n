package info.jallaix.message.service;

import info.jallaix.message.dao.DomainDaoTestsCustomizer;
import info.jallaix.spring.data.es.test.testcase.BaseRestElasticsearchTestCase;

/**
 * <p>The Domain REST controller must verify some tests provided by {@link BaseRestElasticsearchTestCase}.</p>
 * <p>An unsupported complex language is used for input and output data.</p>
 */
public class DomainRestUnsupportedLanguageTest extends DomainRestTest {

    /**
     * Apply customization before executing a test.
     *
     * @param domainDaoTestsCustomizer Domain customizer for DAO tests
     */
    @Override
    public void customizeTest(DomainDaoTestsCustomizer domainDaoTestsCustomizer) {

        // Descriptions fixture for default language
        super.customizeTest(domainDaoTestsCustomizer);

        // Set input and output language to the complex language
        domainDaoTestsCustomizer.setInputLanguageTag("de-DE");
        domainDaoTestsCustomizer.setOutputLanguageRanges("de-DE,de");
        domainDaoTestsCustomizer.setResponseLanguageTag("en");
    }
}
