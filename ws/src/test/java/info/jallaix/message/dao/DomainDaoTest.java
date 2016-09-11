package info.jallaix.message.dao;

import info.jallaix.message.dto.Domain;
import info.jallaix.message.dto.Language;
import info.jallaix.spring.data.es.test.SpringDataEsTestConfiguration;
import info.jallaix.spring.data.es.test.testcase.BaseDaoElasticsearchTestCase;
import info.jallaix.spring.data.es.test.testcase.DaoTestedMethod;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * The Domain DAO must verify some tests provided by {@link BaseDaoElasticsearchTestCase}.
 */
@Configuration
@Import(SpringDataEsTestConfiguration.class)
@EnableElasticsearchRepositories(basePackageClasses = DomainDao.class)
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


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                                   Overriden methods                                            */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Constructor that defines the tests to pass
     */
    public DomainDaoTest() {
        super(DaoTestedMethod.Exist.class);
    }

    /**
     * Return a new document for insertion.
     *
     * @return A document that will be inserted
     */
    @Override
    protected Domain newDocumentToInsert() {

        return new Domain("4", "project4", "esp", Arrays.asList(
                new Domain.Language("esp", Arrays.asList(
                        new Domain.Language.Message("label1", "Fraseología 1"),
                        new Domain.Language.Message("label2", "Fraseología 2")
                )),
                new Domain.Language("rom", Arrays.asList(
                        new Domain.Language.Message("label1", "Formulare 1"),
                        new Domain.Language.Message("label2", "Formulare 2")
                ))
        ));
    }

    /**
     * Return a new document for update.
     *
     * @return A document that will update an existing one
     */
    @Override
    protected Domain newDocumentToUpdate() {

        return new Domain("2", "project4", "esp", Arrays.asList(
                new Domain.Language("esp", Arrays.asList(
                        new Domain.Language.Message("label1", "Fraseología 1"),
                        new Domain.Language.Message("label2", "Fraseología 2")
                )),
                new Domain.Language("rom", Arrays.asList(
                        new Domain.Language.Message("label1", "Formulare 1"),
                        new Domain.Language.Message("label2", "Formulare 2")
                ))
        ));
    }

    /**
     * Return a new existing document.
     *
     * @return A document that exists
     */
    @Override
    protected Domain newExistingDocument() {

        return new Domain("1", "project1", "eng", Arrays.asList(
                new Domain.Language("eng", Arrays.asList(
                        new Domain.Language.Message("label1", "label 1"),
                        new Domain.Language.Message("label2", "label 2")
                )),
                new Domain.Language("fra", Arrays.asList(
                        new Domain.Language.Message("label1", "libellé 1"),
                        new Domain.Language.Message("label2", "libellé 2")
                ))
        ));
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
        assertThat(getRepository().isLanguageUsed("eng"), is(true));
    }

    /**
     * Looking for a language in any domain fails if the language code doesn't exist.
     */
    @Test
    public void languageIsNotUsed() {
        assertThat(getRepository().isLanguageUsed("esp"), is(false));
    }
}
