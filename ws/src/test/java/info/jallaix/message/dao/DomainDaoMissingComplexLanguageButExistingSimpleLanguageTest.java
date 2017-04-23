package info.jallaix.message.dao;

import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.spring.data.es.test.testcase.BaseDaoElasticsearchTestCase;

import java.util.Locale;

/**
 * <p>The Domain DAO must verify some tests provided by {@link BaseDaoElasticsearchTestCase}.</p>
 * <p>A complex language linked to a supported simple language is used for input and output data.</p>
 * <p>Data don't exist for the complex language but exist for the simple language.</p>
 */
public class DomainDaoMissingComplexLanguageButExistingSimpleLanguageTest extends DomainDaoTest {

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

        // Set locale to the complex language
        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("fr-BE"));
        threadLocaleHolder.setOutputLocales(Locale.LanguageRange.parse("fr;q=0.5,fr-BE;q=1,en;q=0.1"));
    }
}
