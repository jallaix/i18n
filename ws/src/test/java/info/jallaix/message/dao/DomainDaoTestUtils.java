package info.jallaix.message.dao;

import info.jallaix.message.bean.DomainTestFixture;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for DomainDao tests.
 */
public class DomainDaoTestUtils {

    /**
     * Get french descriptions for domains.
     *
     * @return The french domain descriptions
     */
    public static Map<String, String> getFrenchDescriptions() {

        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("1", DomainTestFixture.DOMAIN1_FR_DESCRIPTION);
        descriptions.put("2", DomainTestFixture.DOMAIN2_FR_DESCRIPTION);
        descriptions.put("3", DomainTestFixture.DOMAIN3_FR_DESCRIPTION);
        descriptions.put("4", DomainTestFixture.DOMAIN4_FR_DESCRIPTION);

        return descriptions;
    }

    /**
     * Get english descriptions for domains.
     *
     * @return The english domain descriptions
     */
    public static Map<String, String> getEnglishDescriptions() {

        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("1", DomainTestFixture.DOMAIN1_EN_DESCRIPTION);
        descriptions.put("2", DomainTestFixture.DOMAIN2_EN_DESCRIPTION);
        descriptions.put("3", DomainTestFixture.DOMAIN3_EN_DESCRIPTION);
        descriptions.put("4", DomainTestFixture.DOMAIN4_EN_DESCRIPTION);

        return descriptions;
    }

    /**
     * Get english US descriptions for domains.
     *
     * @return The english US domain descriptions
     */
    public static Map<String, String> getEnglishUsDescriptions() {

        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("1", DomainTestFixture.DOMAIN1_EN_US_DESCRIPTION);
        descriptions.put("2", DomainTestFixture.DOMAIN2_EN_DESCRIPTION);
        descriptions.put("3", DomainTestFixture.DOMAIN3_EN_US_DESCRIPTION);
        descriptions.put("4", DomainTestFixture.DOMAIN4_EN_DESCRIPTION);

        return descriptions;
    }
}
