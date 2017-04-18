package info.jallaix.message.dao;

import info.jallaix.message.bean.DomainTestFixture;
import info.jallaix.spring.data.es.test.testcase.BaseDaoElasticsearchTestCase;
import org.junit.Before;

import java.util.Locale;

/**
 * The Domain DAO must verify some tests provided by {@link BaseDaoElasticsearchTestCase}.
 */
public class DomainDaoTest2 extends DomainDaoTest {


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Tests lifecycle                                              */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Initialize custom testing objects.
     */
    @Before
    public void initTest() {
        super.initTest();
        threadLocaleHolder.setInputLocale(Locale.forLanguageTag("fr"));
        threadLocaleHolder.setOutputLocales(Locale.LanguageRange.parse("fr;q=1"));

        // Descriptions fixture for default language
        domainDaoTestsCustomizer.setDescriptionFixture(DomainTestFixture.DOMAIN3_FR_DESCRIPTION);
        domainDaoTestsCustomizer.setDescriptionsFixture(DomainDaoTestUtils.getFrenchDescriptions());
    }
}
