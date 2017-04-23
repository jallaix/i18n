package info.jallaix.message.service;

import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.message.dao.DomainDaoTestUtils;
import info.jallaix.message.dao.DomainDaoTestsCustomizer;
import info.jallaix.spring.data.es.test.testcase.BaseRestElasticsearchTestCase;

/**
 * <p>The Domain REST controller must verify some tests provided by {@link BaseRestElasticsearchTestCase}.</p>
 * <p>A complex language linked to a supported simple language is used for input and output data.</p>
 * <p>Data don't exist for the complex language but exist for the simple language.</p>
 */
public class DomainRestMissingComplexLanguageButExistingSimpleLanguageTest extends DomainRestTest {

    /**
     * Apply customization before executing a test.
     *
     * @param domainDaoTestsCustomizer Domain customizer for DAO tests
     */
    @Override
    public void customizeTest(DomainDaoTestsCustomizer domainDaoTestsCustomizer) {

        // Descriptions fixture for the simple language
        domainDaoTestsCustomizer.setDescriptionFixture(DomainTestFixture.DOMAIN3_FR_DESCRIPTION);
        domainDaoTestsCustomizer.setDescriptionsFixture(DomainDaoTestUtils.getFrenchDescriptions());

        // Set input and output language to the complex language
        domainDaoTestsCustomizer.setInputLanguageTag("fr-BE");
        domainDaoTestsCustomizer.setOutputLanguageRanges("fr;q=0.5,fr-BE;q=1,en;q=0.1");
        domainDaoTestsCustomizer.setResponseLanguageTag("fr-BE");
    }
}
