package info.jallaix.message.dao;

import info.jallaix.message.dto.Language;
import info.jallaix.spring.data.es.test.SpringDataEsCrudTestCase;
import info.jallaix.spring.data.es.test.SpringDataEsTestConfiguration;
import info.jallaix.spring.data.es.test.DaoTestedMethod;
import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.lang.reflect.Field;

/**
 * The Language DAO must verify some tests provided by SpringDataEsCrudTestCase.
 */
@Configuration
@Import(SpringDataEsTestConfiguration.class)
@EnableElasticsearchRepositories(basePackageClasses = LanguageDao.class)
@ContextConfiguration(classes = LanguageDaoTest.class)
public class LanguageDaoTest extends SpringDataEsCrudTestCase<Language, String, LanguageDao> {

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


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                   SpringDataElasticsearchTestCase overriden methods                            */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Constructor that defines the tests to pass
     */
    public LanguageDaoTest() {
        super(
                DaoTestedMethod.Exist.class,
                DaoTestedMethod.Index.class,
                DaoTestedMethod.FindOne.class,
                DaoTestedMethod.FindAll.class,
                DaoTestedMethod.DeleteById.class
        );
    }

    @Override
    protected Language newDocumentToInsert() {
        return new Language("4", "esp", "Español", "Spanish");
    }

    @Override
    protected Language newDocumentToUpdate() {
        return new Language("2", "esp", "Español", "Spanish");
    }

    @Override
    protected Field getSortField() {

        try {
            return Language.class.getDeclaredField("code");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected int getPageSize() { return 2; }
}