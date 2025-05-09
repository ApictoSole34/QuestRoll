package com.fizzycoyote.qusetroll.model.feature_language;

import static org.junit.Assert.assertEquals;

import com.fizzycoyote.qusetroll.feature_language.model.CombinedLanguage;

import org.junit.Test;

public class CombinedLanguageTest {
    @Test
    public void equals_sameFields_returnsTrue() {
        CombinedLanguage lang1 = new CombinedLanguage(
                "Lang", "Desc", false, false, null, null, null, null, "key", null
        );
        CombinedLanguage lang2 = new CombinedLanguage(
                "Lang", "Desc", false, false, null, null, null, null, "key", null
        );

        assertEquals(lang1, lang2);
    }

    @Test
    public void getUniqueKey_customId_returnsCorrectKey() {
        CombinedLanguage lang = new CombinedLanguage(
                null, null, false, false, null, null, null, null, null, 123L
        );

        assertEquals("custom_123", lang.getUniqueKey());
    }
}