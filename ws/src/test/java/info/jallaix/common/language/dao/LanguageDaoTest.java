package info.jallaix.common.language.dao;

import info.jallaix.common.language.dto.Language;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * Created by Julien on 12/05/2016.
 */
//@SpringApplicationConfiguration(classes = Application.class)
@ContextConfiguration(classes = LanguageDaoTestConfiguration.class)
public class LanguageDaoTest {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private LanguageDao languageDao;

    /**
     * Indexing a language must return a language with the same data
     */
    @Test
    public void indexLanguage() {

        Language toIndex = new Language("esp", "Español", "Spanish");
        Language indexed = languageDao.index(toIndex);

        assertEquals(toIndex, indexed);
    }
}