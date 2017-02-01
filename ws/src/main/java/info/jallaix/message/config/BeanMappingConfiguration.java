package info.jallaix.message.config;

import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.dto.Domain;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Collections;

/**
 * Bean mapping configuration
 */
@Configuration
@PropertySource("classpath:/info/jallaix/message/config/config.properties}")
public class BeanMappingConfiguration {

    /**
     * Repository for domain data
     */
    @Autowired
    private DomainDao domainDao;


    /**
     * Default dozer mapper
     *
     * @return the default dozer mapper
     */
    @Bean
    public Mapper beanMapper() {
        return new DozerBeanMapper();
    }

    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfig() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * The message domain bean contains the domain data for the current application.
     *
     * @return The message domain
     */
    @Bean
    public Domain messageDomain() {

        // Get the message domain
        Domain messageDomain = domainDao.findByCode("i18n.message");

        // Index the message domain if it's unavailable in the ES index
        if (messageDomain == null) {

            Domain messageDomainToIndex = new Domain(null, "i18n.message", "Internationalized messages", "en-US", Collections.singleton("en-US"));
            messageDomain = domainDao.index(messageDomainToIndex);
        }

        return messageDomain;
    }
}
