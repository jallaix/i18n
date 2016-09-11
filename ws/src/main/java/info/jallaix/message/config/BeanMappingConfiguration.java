package info.jallaix.message.config;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean mapping configuration
 */
@Configuration
public class BeanMappingConfiguration {

    /**
     * Default dozer mapper
     *
     * @return the default dozer mapper
     */
    @Bean
    public Mapper beanMapper() {
        return new DozerBeanMapper();
    }
}
