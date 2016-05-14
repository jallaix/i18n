package info.jallaix.common.language.dao;

import com.github.tlrx.elasticsearch.test.EsSetup;
import info.jallaix.common.language.dto.Language;
import org.elasticsearch.client.Client;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;
import static com.github.tlrx.elasticsearch.test.EsSetup.*;

/**
 * The Language DAO must verify the following tests related to <b>language saving</b> :
 * <ul>
 *     <li>Indexing a null language throws an IllegalArgumentException.</li>
 *     <li>Indexing a non existing language inserts the language in the index.</li>
 *     <li>Indexing an existing language replaces the language in the index.</li>
 * </ul>
 */
//@SpringApplicationConfiguration(classes = Application.class)
@ContextConfiguration(classes = LanguageDaoTestConfiguration.class)
public class LanguageDaoTest {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    public static final String INDEX = "message";
    public static final String TYPE = "language";
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private Client elasticsearchClient;
    EsSetup esSetup;

    @Autowired
    private LanguageDao languageDao;


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                      Initialization of Elastic index                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Create a "message" Elastic index with "language" type data
     */
    @Before
    public void initElasticIndex() {

        esSetup = new EsSetup(elasticsearchClient, false);
        esSetup.execute(
                deleteAll(),
                createIndex(INDEX)
                        .withMapping(TYPE, fromClassPath("info/jallaix/common/language/dao/LanguageDaoTest.mapping.json"))
                        .withData(fromClassPath("info/jallaix/common/language/dao/LanguageDaoTest.json.bulk"))
                );
    }

    /**
     * Free resources used by Elastic
     */
    @After
    public void terminateElasticIndex() {

        esSetup.terminate();
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                    Tests related to language indexing                                          */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Indexing a language must return a language with the same data
     */
    @Test(expected=IllegalArgumentException.class)
    public void indexNullLanguage() {

        languageDao.index(null);
    }

    /**
     * Indexing a non existing language inserts the language in the index.
     */
    @Test
    public void indexNewLanguage() {

        assertEquals(2, countLanguages());

        Language toInsert = new Language("esp", "Español", "Spanish");
        Language inserted = languageDao.index(toInsert);

        assertEquals(3, countLanguages());
        assertEquals(toInsert, inserted);
    }

    /**
     * Indexing an existing language replaces the language in the index.
     */
    @Test
    public void indexExistingLanguage() {

        assertEquals(2, countLanguages());
        Language toUpdate = new Language("fra", "Español", "Spanish");
        Language updated = languageDao.index(toUpdate);

        assertEquals(2, countLanguages());
        assertEquals(toUpdate, updated);
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                              Private methods                                                   */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Count the number of languages in the index
     * @return The number of languages found
     */
    private long countLanguages() {

        return elasticsearchClient.prepareCount(INDEX).setTypes(TYPE).get().getCount();
    }
}