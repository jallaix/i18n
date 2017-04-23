package info.jallaix.message.dao;

import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.spring.data.es.test.testcase.BaseDaoElasticsearchTestCase;

import java.util.Locale;

/**
 * <p>The Domain DAO must verify some tests provided by {@link BaseDaoElasticsearchTestCase}.</p>
 * <p>A complex language linked to a supported simple language is used for input and output data.</p>
 * <p>Data exist for the complex language.</p>
 */
public class DomainDaoExistingComplexLanguageTest extends DomainDaoTest {

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

        // Set locale to the complex language
        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("en-US"));
        threadLocaleHolder.setOutputLocales(Locale.LanguageRange.parse("en;q=0.5,en-US;q=1"));
    }
}
