package info.jallaix.message.dao;

import info.jallaix.spring.data.es.test.testcase.BaseDaoElasticsearchTestCase;

import java.util.Locale;

/**
 * <p>The Domain DAO must verify some tests provided by {@link BaseDaoElasticsearchTestCase}.</p>
 * <p>A supported simple language is used for input and output data.</p>
 * <p>Data don't exist for the simple language.</p>
 */
public class DomainDaoMissingLanguageTest extends DomainDaoTest {

    /**
     * Apply customization before executing a test.
     *
     * @param domainDaoTestsCustomizer Domain customizer for DAO tests
     */
    @Override
    public void customizeTest(DomainDaoTestsCustomizer domainDaoTestsCustomizer) {

        // Descriptions fixture for default language
        super.customizeTest(domainDaoTestsCustomizer);

        // Set locale to the simple language
        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("es"));
        threadLocaleHolder.setOutputLocales(Locale.LanguageRange.parse("es"));
    }
}
