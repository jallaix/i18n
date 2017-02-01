package info.jallaix.message.dao;

import info.jallaix.message.dao.interceptor.DomainDaoInterceptor;
import info.jallaix.message.dto.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collections;

/**
 * Created by Julien on 01/02/2017.
 */
@Configuration
@Import({DomainDaoInterceptor.class})
public class DomainDaoConfiguration {

    @Autowired
    private DomainDao domainDao;

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
