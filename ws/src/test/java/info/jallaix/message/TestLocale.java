package info.jallaix.message;

import org.junit.Test;

import java.util.Locale;

/**
 * Created by Julien on 21/01/2017.
 */
public class TestLocale {

    @Test
    public void printAvailableLanguages() {
        for (Locale l : Locale.getAvailableLocales())
        System.out.println(l.getDisplayName(l));

    }
}
