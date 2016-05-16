package info.jallaix.common.language.dao;

import info.jallaix.common.language.dto.Language;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * The Language DAO must verify the following tests related to <b>language saving</b> :
 * <ul>
 *     <li>Indexing a null language throws an IllegalArgumentException.</li>
 *     <li>Indexing a non existing language inserts the language in the index.</li>
 *     <li>Indexing an existing language replaces the language in the index.</li>
 * </ul>
 */
//@SpringApplicationConfiguration(classes = Application.class)
public class LanguageDaoTest extends InitializedSpringDataEsTestCase<Language, String, LanguageDao> {

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
    /*                                    Tests related to language indexing                                          */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Indexing a language must return a language with the same data
     */
    @Test(expected=IllegalArgumentException.class)
    public void indexNullLanguage() {

        getRepository().index(null);
    }

    /**
     * Indexing a non existing language inserts the language in the index.
     */
    @Test
    public void indexNewLanguage() {

        assertEquals(2, countDocuments());

        Language toInsert = new Language("esp", "Español", "Spanish");
        Language inserted = getRepository().index(toInsert);

        assertEquals(3, countDocuments());
        assertEquals(toInsert, inserted);
    }

    /**
     * Indexing an existing language replaces the language in the index.
     */
    @Test
    public void indexExistingLanguage() {

        assertEquals(2, countDocuments());
        Language toUpdate = new Language("fra", "Español", "Spanish");
        Language updated = getRepository().index(toUpdate);

        assertEquals(2, countDocuments());
        assertEquals(toUpdate, updated);
    }
}