package com.agenda.agendaespacios.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.Map;

class ConfigDataTest {

    @Test
    void testConstructorAndGetters() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        assertEquals(2024, config.getYear());
        assertEquals(7, config.getMonth());
        assertEquals("ESP", config.getSourceLanguage());
        assertEquals("ENG", config.getTargetLanguage());
    }

    @Test
    void testGetLanguageKey() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        assertEquals("ESP_ENG", config.getLanguageKey());

        ConfigData config2 = new ConfigData(2023, 1, "CAT", "FR");
        assertEquals("CAT_FR", config2.getLanguageKey());
    }

    @Test
    void testGetDayCodeMapping_existingMapping() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        Map<Character, Character> mapping = config.getDayCodeMapping();
        assertNotNull(mapping);
        assertEquals('M', mapping.get('L')); // Lunes -> Monday
        assertEquals('T', mapping.get('M')); // Martes -> Tuesday
        assertEquals('W', mapping.get('C')); // Miércoles -> Wednesday
        assertEquals('T', mapping.get('J')); // Jueves -> Thursday
        assertEquals('F', mapping.get('V')); // Viernes -> Friday
        assertEquals('S', mapping.get('S')); // Sábado -> Saturday
        assertEquals('S', mapping.get('G')); // Domingo -> Sunday
    }

    @Test
    void testGetDayCodeMapping_nonExistingMapping() {
        ConfigData config = new ConfigData(2024, 7, "NON", "EXIST");
        Map<Character, Character> mapping = config.getDayCodeMapping();
        assertNotNull(mapping);
        assertTrue(mapping.isEmpty());
    }

    @Test
    void testGetDayCodeMapping_identityMapping() {
        ConfigData config = new ConfigData(2024, 7, "CAT", "CAT");
        Map<Character, Character> mapping = config.getDayCodeMapping();
        assertNotNull(mapping);
        assertEquals('L', mapping.get('L'));
        assertEquals('M', mapping.get('M'));
        assertEquals('C', mapping.get('C'));
        assertEquals('J', mapping.get('J'));
        assertEquals('V', mapping.get('V'));
        assertEquals('S', mapping.get('S'));
        assertEquals('G', mapping.get('G'));
    }

    @Test
    void testTranslateDayCodes_nullOrEmpty() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        assertNull(config.translateDayCodes(null));
        assertEquals("", config.translateDayCodes(""));
    }

    @Test
    void testTranslateDayCodes_successfulTranslation() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        assertEquals("MTW", config.translateDayCodes("LMC"));

        ConfigData configCatToFr = new ConfigData(2024, 1, "CAT", "FR");
        assertEquals("LMMJVSD", configCatToFr.translateDayCodes("LMCJVSD"));
    }

    @Test
    void testTranslateDayCodes_unmappedCharacters() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        assertEquals("MTWXYZ", config.translateDayCodes("LMCXYZ")); // X, Y, Z are not in ESP_ENG mapping
    }

    @Test
    void testTranslateDayCodes_noMappingAvailable() {
        ConfigData config = new ConfigData(2024, 7, "UNKNOWN", "LANG");
        assertEquals("LMC", config.translateDayCodes("LMC")); // Should return original if no mapping
    }

    @Test
    void testTranslateDayCodes_identityMapping() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ESP");
        assertEquals("LMCJVSD", config.translateDayCodes("LMCJVSD"));
    }

    @Test
    void testGetLocale() {
        ConfigData configEng = new ConfigData(2024, 7, "ANY", "ENG");
        assertEquals(Locale.ENGLISH, configEng.getLocale());

        ConfigData configEn = new ConfigData(2024, 7, "ANY", "en");
        assertEquals(Locale.ENGLISH, configEn.getLocale());

        ConfigData configFr = new ConfigData(2024, 7, "ANY", "FR");
        assertEquals(Locale.FRENCH, configFr.getLocale());
        
        ConfigData configFra = new ConfigData(2024, 7, "ANY", "fra");
        assertEquals(Locale.FRENCH, configFra.getLocale());

        ConfigData configFre = new ConfigData(2024, 7, "ANY", "fre");
        assertEquals(Locale.FRENCH, configFre.getLocale());

        ConfigData configCat = new ConfigData(2024, 7, "ANY", "CAT");
        assertEquals(new Locale("ca"), configCat.getLocale());

        ConfigData configCa = new ConfigData(2024, 7, "ANY", "ca");
        assertEquals(new Locale("ca"), configCa.getLocale());

        ConfigData configUnknown = new ConfigData(2024, 7, "ANY", "UNKNOWN");
        assertEquals(Locale.ENGLISH, configUnknown.getLocale()); // Default

        ConfigData configNullLang = new ConfigData(2024, 7, "ANY", null);
        assertEquals(Locale.ENGLISH, configNullLang.getLocale()); // Default for null

        ConfigData configBlankLang = new ConfigData(2024, 7, "ANY", "   ");
        assertEquals(Locale.ENGLISH, configBlankLang.getLocale()); // Default for blank
    }
} 