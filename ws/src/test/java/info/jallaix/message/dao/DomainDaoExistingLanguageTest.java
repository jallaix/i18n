package info.jallaix.message.dao;

import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.spring.data.es.test.testcase.BaseDaoElasticsearchTestCase;

import java.util.Locale;

/**
 * <p>The Domain DAO must verify some tests provided by {@link BaseDaoElasticsearchTestCase}.</p>
 * <p>A supported simple language is used for input and output data.</p>
 * <p>Data exist for the simple language.</p>
 */
public class DomainDaoExistingLanguageTest extends DomainDaoTest {

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

        // Set locale to the simple language
        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("fr"));
        threadLocaleHolder.setOutputLocales(Locale.LanguageRange.parse("fr;q=1"));
    }
}
