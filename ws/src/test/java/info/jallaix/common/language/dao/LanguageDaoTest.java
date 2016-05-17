package info.jallaix.common.language.dao;

import info.jallaix.common.language.dto.Language;
import org.junit.*;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

/**
 * The Language DAO must verify the following tests related to <b>language saving</b> :
 * <ul>
 *     <li>Indexing a null language throws an IllegalArgumentException.</li>
 *     <li>Indexing a non existing language inserts the language in the index.</li>
 *     <li>Indexing an existing language replaces the language in the index.</li>
 * </ul>
 */
//@SpringApplicationConfiguration(classes = Application.class)
public class LanguageDaoTest extends SpringDataElasticsearchTestCase<Language, String, LanguageDao> {

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

    @Override
    protected Language newDocumentToInsert() {
        return new Language("esp", "Español", "Spanish");
    }

    @Override
    protected Language newDocumentToUpdate() {
        return new Language("fra", "Español", "Spanish");
    }
}