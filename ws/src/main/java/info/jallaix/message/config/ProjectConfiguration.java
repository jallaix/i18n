package info.jallaix.message.config;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import info.jallaix.message.dao.interceptor.ThreadLocaleHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Project configuration
 */
@Configuration
//@PropertySource("classpath:/info/jallaix/message/config/project.properties}")
public class ProjectConfiguration {

    /**
     * Elasticsearch operations
     */
    @Autowired
    private ElasticsearchOperations esOperations;

    /**
     * Property resource configurer that resolves ${} in @Value annotations.
     *
     * @return The property resource configurer
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfig() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * The i18n domain holder gives access to the domain data for the current application.
     *
     * @return The i18n domain holder
     */
    @Bean
    public DomainHolder i18nDomainHolder() {
        return new I18nDomainHolder(esOperations);
    }

    /**
     * This bean holds locale data linked to the current thread.
     *
     * @return The thread locale holder
     */
    @Bean
    public ThreadLocaleHolder threadLocaleHolder() {
        return new ThreadLocaleHolder(i18nDomainHolder());
    }

    /**
     * Access to the serialization framework.
     *
     * @return The access to the serialization framework
     */
    @Bean
    public Kryo kryo() {

        Kryo kryo = new Kryo();
        kryo.register(Arrays.asList().getClass(), new AsListCollectionSerializer());

        return kryo;
    }

    public class AsListCollectionSerializer extends CollectionSerializer {

        @Override
        protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
            return new ArrayList();
        }

        @Override
        public Collection createCopy (Kryo kryo, Collection original) {
            return new ArrayList();
        }
    }
}
