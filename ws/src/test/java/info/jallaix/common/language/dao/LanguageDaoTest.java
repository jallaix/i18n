package info.jallaix.common.language.dao;

import com.github.tlrx.elasticsearch.test.EsSetup;
import info.jallaix.common.language.dto.Language;
import org.elasticsearch.client.Client;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.Document;
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
    /**
     * Elastic index of the document to test
     */
    private static final String INDEX = Language.class.getDeclaredAnnotation(Document.class).indexName();
    /**
     * Elastic type of the document to test
     */
    private static final String TYPE = Language.class.getDeclaredAnnotation(Document.class).type();

    /**
     * File extension for document mapping
     */
    public static final String DOCUMENT_MAPPING_EXTENSION = ".mapping.json";
    /**
     * File extension for document data
     */
    public static final String DOCUMENT_DATA_EXTENSION = ".data.bulk";

    /**
     * Elastic client
     */
    @Autowired
    private Client elasticsearchClient;
    /**
     * Elastic setup for index/type initialization
     */
    private EsSetup esSetup;

    /**
     * Language DAO to test
     */
    @Autowired
    private LanguageDao languageDao;


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                      Initialization of Elastic index                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Create a "message" Elastic index with "language" data type
     */
    @Before
    public void initElasticIndex() {

        String mappingClassPath = this.getClass().getName().replace(".", "/") + DOCUMENT_MAPPING_EXTENSION;
        String dataClassPath = this.getClass().getName().replace(".", "/") + DOCUMENT_DATA_EXTENSION;

        esSetup = new EsSetup(elasticsearchClient, false);
        esSetup.execute(
                deleteAll(),
                createIndex(INDEX)
                        .withMapping(TYPE, fromClassPath(mappingClassPath))
                        .withData(fromClassPath(dataClassPath))
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