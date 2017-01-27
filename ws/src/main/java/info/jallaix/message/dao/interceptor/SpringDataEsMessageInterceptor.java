package info.jallaix.message.dao.interceptor;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * <p>
 * This bean intercepts Spring Data Elasticsearch operations to manage internationalizable messages.
 * </p>
 * <p>
 * When creating entities, internationalizable property values are replaced by message codes and original values are saved in the message index.
 * </p>
 */
@Aspect
@Component
public class SpringDataEsMessageInterceptor {

    /**
     * Intercept entities before they are created to save internationalizable messages in the index and replace matching property values by message codes.
     *
     * @param entities The list of entities to create
     */
    @Before("execution(* org.springframework.data.elasticsearch.repository.ElasticsearchRepository+.save(Iterable,..)) && args(entities,..)"
            + " || execution(* org.springframework.data.elasticsearch.repository.ElasticsearchRepository+.index(Iterable,..)) && args(entities,..)")
    public void beforeCreation(Iterable entities) {
        System.out.println(entities);
    }

    /**
     * Intercept entities before they are created to save internationalizable messages in the index and replace matching property values by message codes.
     *
     * @param entity The entity to create
     */
    @Before("execution(* org.springframework.data.elasticsearch.repository.ElasticsearchRepository+.save(Object,..)) && args(entity,..)"
            + " || execution(* org.springframework.data.elasticsearch.repository.ElasticsearchRepository+.index(Object,..)) && args(entity,..)")
    public void beforeCreation(Object entity) {
        System.out.println(entity);
    }
}
