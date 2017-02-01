package info.jallaix.message.dao;

import info.jallaix.message.dto.Domain;
import info.jallaix.message.dto.Language;
import info.jallaix.message.dto.Message;
import info.jallaix.spring.data.es.test.SpringDataEsTestConfiguration;
import info.jallaix.spring.data.es.test.testcase.BaseDaoElasticsearchTestCase;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * The Domain DAO must verify some tests provided by {@link BaseDaoElasticsearchTestCase}.
 */
@Configuration
@Import({SpringDataEsTestConfiguration.class, DomainDaoConfiguration.class})
@EnableElasticsearchRepositories(basePackageClasses = {DomainDao.class, MessageDao.class})
@EnableAspectJAutoProxy
@ContextConfiguration(classes = DomainDaoTest.class)
public class DomainDaoTest extends BaseDaoElasticsearchTestCase<Domain, String, DomainDao> {

    /**
     * Spring class rule
     */
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    /**
     * Spring method rule
     */
    @Rule
    public final SpringMethodRule SPRING_METHOD_RULE = new SpringMethodRule();

    @Resource
    private Domain messageDomain;

    @Autowired
    private ElasticsearchOperations esOperations;

    @After
    public void clearLocales() {
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Overriden methods                                            */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Constructor that defines the tests to pass
     */
    public DomainDaoTest() {
        super(/*DaoTestedMethod.Exist.class*/);
    }

    /**
     * Return a new document for insertion.
     *
     * @return A document that will be inserted
     */
    @Override
    protected Domain newDocumentToInsert() {

        return new Domain("4", "project4", "project4.description", "es-ES", Arrays.asList("en-US", "fr-FR", "es-ES"));
    }

    /**
     * Return a new document for update.
     *
     * @return A document that will update an existing one
     */
    @Override
    protected Domain newDocumentToUpdate() {

        return new Domain("2", "project4", "project4.description", "es-ES", Arrays.asList("en-US", "fr-FR", "es-ES"));
    }

    /**
     * Return a new existing document.
     *
     * @return A document that exists
     */
    @Override
    protected Domain newExistingDocument() {

        return new Domain("2", "project2", "project2.description", "fr-FR", Arrays.asList("en-US", "fr-FR", "es-ES"));
    }

    /**
     * Return the sort field
     *
     * @return The sort field
     */
    @Override
    protected Field getSortField() {

        try {
            return Language.class.getDeclaredField("code");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the size of a page to get
     *
     * @return The size of a page to get
     */
    @Override
    protected int getPageSize() {
        return 2;
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                     Custom tests                                               */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Looking for a language in any domain is successful if the language code is used.
     */
    @Test
    public void languageIsUsed() {
        assertThat(getRepository().isLanguageUsed("en-US"), is(true));
    }

    /**
     * Looking for a language in any domain fails if the language code doesn't exist.
     */
    @Test
    public void languageIsNotUsed() {
        assertThat(getRepository().isLanguageUsed("es-ES"), is(false));
    }

    /**
     * Saving a new domain inserts the domain in the index.
     * It also inserts the domain description in the message's index type, localized with the domain's default language.
     */
    @Override
    public void saveNewDocument() {

        // Insert a new document in the index
        super.saveNewDocument();

        // The domain description in the Elasticsearch index must contain a message code
        Domain inserted = newDocumentToInsert();
        GetQuery getQuery = new GetQuery();
        getQuery.setId(inserted.getId());
        Domain savedDomain = esOperations.queryForObject(getQuery, Domain.class);
        assertNotEquals(inserted.getDescription(), savedDomain.getDescription());

        List<Message> l = esOperations.queryForList(new CriteriaQuery(new Criteria("code").is(savedDomain.getDescription())), Message.class);

        // Get the message domain to verify that all supported languages have a matching description
        messageDomain.getAvailableLanguageTags().forEach(languageTag -> {

            // A message with the localized description must be linked to the message code
            Message message = esOperations.queryForObject(
                    new CriteriaQuery(
                            new Criteria("code").is(savedDomain.getDescription())
                                    .and(new Criteria("languageTag").is(languageTag))
                                    .and(new Criteria("domainCode").is(inserted.getCode()))),
                    Message.class);
            assertNotNull(message);
            assertEquals(inserted.getDescription(), message.getContent());
        });
    }
}
