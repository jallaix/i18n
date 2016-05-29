package info.jallaix.message.dao;

import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactoryBean;

/**
 * Created by Julien on 25/05/2016.
 */
public class RestElasticsearchRepositoryFactoryBean extends ElasticsearchRepositoryFactoryBean {

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() {
        setMappingContext(new SimpleElasticsearchMappingContext());
        super.afterPropertiesSet();
    }
}
