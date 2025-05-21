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
        // No mappings needed - we will only use Spanish day codes (LMCJVSG)
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
        // Always preserve the Spanish day codes format (LMCJVSG)
        // regardless of source and target language
        return dayCodes;
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