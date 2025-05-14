package com.agenda.agendaespacios.model;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConfigData {
    private int year;
    private int month;
    private String sourceLanguage;
    private String targetLanguage;
    
    // Map of day codes to their corresponding values in different languages
    private static final Map<String, Map<Character, Character>> DAY_CODES = new HashMap<>();
    
    static {
        // Spanish to English
        Map<Character, Character> espToEng = new HashMap<>();
        espToEng.put('L', 'M'); // Lunes -> Monday
        espToEng.put('M', 'T'); // Martes -> Tuesday
        espToEng.put('C', 'W'); // Miércoles -> Wednesday
        espToEng.put('J', 'T'); // Jueves -> Thursday
        espToEng.put('V', 'F'); // Viernes -> Friday
        espToEng.put('S', 'S'); // Sábado -> Saturday
        espToEng.put('G', 'S'); // Domingo -> Sunday
        
        DAY_CODES.put("ESP_ENG", espToEng);
        
        // Catalan to English
        Map<Character, Character> catToEng = new HashMap<>();
        catToEng.put('L', 'M'); // Dilluns -> Monday
        catToEng.put('M', 'T'); // Dimarts -> Tuesday
        catToEng.put('C', 'W'); // Dimecres -> Wednesday
        catToEng.put('J', 'T'); // Dijous -> Thursday
        catToEng.put('V', 'F'); // Divendres -> Friday
        catToEng.put('S', 'S'); // Dissabte -> Saturday
        catToEng.put('G', 'S'); // Diumenge -> Sunday
        
        DAY_CODES.put("CAT_ENG", catToEng);
        
        // Catalan to French
        Map<Character, Character> catToFr = new HashMap<>();
        catToFr.put('L', 'L'); // Dilluns -> Lundi
        catToFr.put('M', 'M'); // Dimarts -> Mardi
        catToFr.put('C', 'M'); // Dimecres -> Mercredi
        catToFr.put('J', 'J'); // Dijous -> Jeudi
        catToFr.put('V', 'V'); // Divendres -> Vendredi
        catToFr.put('S', 'S'); // Dissabte -> Samedi
        catToFr.put('G', 'D'); // Diumenge -> Dimanche
        
        DAY_CODES.put("CAT_FR", catToFr);
        
        // For same language mappings, create identity maps (no translation needed)
        Map<Character, Character> catToCat = new HashMap<>();
        catToCat.put('L', 'L');
        catToCat.put('M', 'M');
        catToCat.put('C', 'C');
        catToCat.put('J', 'J');
        catToCat.put('V', 'V');
        catToCat.put('S', 'S');
        catToCat.put('G', 'G');
        
        DAY_CODES.put("CAT_CAT", catToCat);
        
        // Spanish to Spanish (identity mapping)
        Map<Character, Character> espToEsp = new HashMap<>();
        espToEsp.put('L', 'L');
        espToEsp.put('M', 'M');
        espToEsp.put('C', 'C');
        espToEsp.put('J', 'J');
        espToEsp.put('V', 'V');
        espToEsp.put('S', 'S');
        espToEsp.put('G', 'G');
        
        DAY_CODES.put("ESP_ESP", espToEsp);
        
        // Spanish to French
        Map<Character, Character> espToFr = new HashMap<>();
        espToFr.put('L', 'L');
        espToFr.put('M', 'M');
        espToFr.put('C', 'M');
        espToFr.put('J', 'J');
        espToFr.put('V', 'V');
        espToFr.put('S', 'S');
        espToFr.put('G', 'D');
        
        DAY_CODES.put("ESP_FR", espToFr);
    }
    
    public ConfigData(int year, int month, String sourceLanguage, String targetLanguage) {
        this.year = year;
        this.month = month;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
    }
    
    public int getYear() {
        return year;
    }
    
    public int getMonth() {
        return month;
    }
    
    public String getSourceLanguage() {
        return sourceLanguage;
    }
    
    public String getTargetLanguage() {
        return targetLanguage;
    }
    
    public String getLanguageKey() {
        return sourceLanguage + "_" + targetLanguage;
    }
    
    public Map<Character, Character> getDayCodeMapping() {
        return DAY_CODES.getOrDefault(getLanguageKey(), new HashMap<>());
    }
    
    public String translateDayCodes(String dayCodes) {
        if (dayCodes == null || dayCodes.isEmpty()) {
            return dayCodes;
        }
        
        Map<Character, Character> mapping = getDayCodeMapping();
        if (mapping.isEmpty()) {
            return dayCodes; // No translation needed
        }
        
        StringBuilder result = new StringBuilder();
        for (char c : dayCodes.toCharArray()) {
            result.append(mapping.getOrDefault(c, c));
        }
        
        return result.toString();
    }
    
    public Locale getLocale() {
        // Convert target language code to Locale for message resolution
        if (targetLanguage == null) {
            return Locale.ENGLISH; // Default
        }
        
        String lang = targetLanguage.trim().toLowerCase();
        switch (lang) {
            case "eng":
            case "en":
                return Locale.ENGLISH;
            case "fr":
            case "fre":
            case "fra":
                return Locale.FRENCH;
            case "cat":
            case "ca":
                return new Locale("ca"); // ISO 639 code for Catalan
            default:
                System.out.println("Using default locale for unknown language: " + targetLanguage);
                return Locale.ENGLISH; // Default to English
        }
    }
} 