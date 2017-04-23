package info.jallaix.message.service;

import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.message.dao.DomainDaoTestUtils;
import info.jallaix.message.dao.DomainDaoTestsCustomizer;
import info.jallaix.spring.data.es.test.testcase.BaseRestElasticsearchTestCase;

/**
 * <p>The Domain REST controller must verify some tests provided by {@link BaseRestElasticsearchTestCase}.</p>
 * <p>A complex language linked to a supported simple language is used for input and output data.</p>
 * <p>Data exist for the complex language.</p>
 */
public class DomainRestExistingComplexLanguageTest extends DomainRestTest {

    /**
     * Apply customization before executing a test.
     *
     * @param domainDaoTestsCustomizer Domain customizer for DAO tests
     */
    @Override
    public void customizeTest(DomainDaoTestsCustomizer domainDaoTestsCustomizer) {

        // Descriptions fixture for the complex language
        domainDaoTestsCustomizer.setDescriptionFixture(DomainTestFixture.DOMAIN3_EN_US_DESCRIPTION);
        domainDaoTestsCustomizer.setDescriptionsFixture(DomainDaoTestUtils.getEnglishUsDescriptions());

        // Set input and output language to the complex language
        domainDaoTestsCustomizer.setInputLanguageTag("en-US");
        domainDaoTestsCustomizer.setOutputLanguageRanges("en;q=0.5,en-US;q=1");
        domainDaoTestsCustomizer.setResponseLanguageTag("en-US");
    }
}
