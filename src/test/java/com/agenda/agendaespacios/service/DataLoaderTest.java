package com.agenda.agendaespacios.service;

import com.agenda.agendaespacios.model.ConfigData;
import com.agenda.agendaespacios.model.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataLoaderTest {

    private DataLoader dataLoader;

    @BeforeEach
    void setUp() {
        dataLoader = new DataLoader();
    }

    // Tests for loadConfig
    @Test
    void loadConfig_validFile_shouldReturnConfigData() throws IOException {
        String content = "2024 07\n" +
                         "ES EN";
        MultipartFile configFile = new MockMultipartFile("config.txt", "config.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        ConfigData configData = dataLoader.loadConfig(configFile);

        assertNotNull(configData);
        assertEquals(2024, configData.getYear());
        assertEquals(7, configData.getMonth());
        assertEquals("ES", configData.getSourceLanguage());
        assertEquals("EN", configData.getTargetLanguage());
    }

    @Test
    void loadConfig_emptyFile_shouldThrowIllegalArgumentException() {
        MultipartFile configFile = new MockMultipartFile("config.txt", "config.txt", "text/plain", new byte[0]);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dataLoader.loadConfig(configFile);
        });
        assertEquals("Config file is required", exception.getMessage());
    }

    @Test
    void loadConfig_nullFile_shouldThrowIllegalArgumentException() {
         IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dataLoader.loadConfig(null);
        });
        assertEquals("Config file is required", exception.getMessage());
    }


    @Test
    void loadConfig_invalidDateFormat_shouldThrowIllegalArgumentException() {
        String content = "2024-07\n" + // Invalid separator
                         "ES EN";
        MultipartFile configFile = new MockMultipartFile("config.txt", "config.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dataLoader.loadConfig(configFile);
        });
        assertEquals("Date format must be 'YYYY MM'", exception.getMessage());
    }

    @Test
    void loadConfig_notEnoughDateParts_shouldThrowIllegalArgumentException() {
        String content = "2024\n" + 
                         "ES EN";
        MultipartFile configFile = new MockMultipartFile("config.txt", "config.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dataLoader.loadConfig(configFile);
        });
        assertEquals("Date format must be 'YYYY MM'", exception.getMessage());
    }


    @Test
    void loadConfig_invalidLanguageFormat_shouldThrowIllegalArgumentException() {
        String content = "2024 07\n" +
                         "ES-EN"; // Invalid separator
        MultipartFile configFile = new MockMultipartFile("config.txt", "config.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dataLoader.loadConfig(configFile);
        });
        assertEquals("Language format must be 'SOURCE TARGET'", exception.getMessage());
    }

    @Test
    void loadConfig_notEnoughLangParts_shouldThrowIllegalArgumentException() {
        String content = "2024 07\n" +
                         "ES";
        MultipartFile configFile = new MockMultipartFile("config.txt", "config.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dataLoader.loadConfig(configFile);
        });
        assertEquals("Language format must be 'SOURCE TARGET'", exception.getMessage());
    }

    @Test
    void loadConfig_missingDateLine_shouldThrowIllegalArgumentException() {
        String content = "\nES EN"; // Missing date line
        MultipartFile configFile = new MockMultipartFile("config.txt", "config.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));
         IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dataLoader.loadConfig(configFile);
        });
        assertEquals("Config file must contain date information", exception.getMessage());
    }
    
    @Test
    void loadConfig_emptyDateLine_shouldThrowIllegalArgumentException() {
        String content = "   \nES EN"; // Empty date line
        MultipartFile configFile = new MockMultipartFile("config.txt", "config.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));
         IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dataLoader.loadConfig(configFile);
        });
        assertEquals("Config file must contain date information", exception.getMessage());
    }

    @Test
    void loadConfig_missingLanguageLine_shouldThrowIllegalArgumentException() {
        String content = "2024 07\n"; // Missing language line
        MultipartFile configFile = new MockMultipartFile("config.txt", "config.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));
         IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dataLoader.loadConfig(configFile);
        });
        assertEquals("Config file must contain language information", exception.getMessage());
    }

    @Test
    void loadConfig_emptyLanguageLine_shouldThrowIllegalArgumentException() {
        String content = "2024 07\n   "; // Empty language line
        MultipartFile configFile = new MockMultipartFile("config.txt", "config.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));
         IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dataLoader.loadConfig(configFile);
        });
        assertEquals("Config file must contain language information", exception.getMessage());
    }

    @Test
    void loadConfig_nonNumericYear_shouldThrowIllegalArgumentException() {
        String content = "YYYY 07\n" +
                         "ES EN";
        MultipartFile configFile = new MockMultipartFile("config.txt", "config.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dataLoader.loadConfig(configFile);
        });
        assertTrue(exception.getMessage().startsWith("Invalid year or month format:"));
    }

    @Test
    void loadConfig_nonNumericMonth_shouldThrowIllegalArgumentException() {
        String content = "2024 MM\n" +
                         "ES EN";
        MultipartFile configFile = new MockMultipartFile("config.txt", "config.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dataLoader.loadConfig(configFile);
        });
        assertTrue(exception.getMessage().startsWith("Invalid year or month format:"));
    }
    
    @Test
    void loadConfig_extraContentAfterLanguage_shouldStillSucceed() throws IOException {
        String content = "2024 07\n" +
                         "ES EN\n" + 
                         "Some other data";
        MultipartFile configFile = new MockMultipartFile("config.txt", "config.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        ConfigData configData = dataLoader.loadConfig(configFile);

        assertNotNull(configData);
        assertEquals(2024, configData.getYear());
        assertEquals(7, configData.getMonth());
        assertEquals("ES", configData.getSourceLanguage());
        assertEquals("EN", configData.getTargetLanguage());
    }


    // Tests for loadReservations
    @Test
    void loadReservations_validFile_shouldReturnListOfReservations() throws IOException {
        String content = "Yoga Sala1 2024-07-01 2024-07-31 LMXJV 10-11\n" +
                         "Pilates Sala2 2024-07-01 2024-07-31 S 09-10";
        MultipartFile reservationsFile = new MockMultipartFile("reservations.txt", "reservations.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        List<Reservation> reservations = dataLoader.loadReservations(reservationsFile);

        assertNotNull(reservations);
        assertEquals(2, reservations.size());

        Reservation r1 = reservations.get(0);
        assertEquals("Yoga", r1.getActivityName());
        assertEquals("Sala1", r1.getRoomName());
        assertEquals("2024-07-01", r1.getStartDate().toString());
        assertEquals("2024-07-31", r1.getEndDate().toString());
        assertEquals("LMXJV", r1.getDayPattern());
        assertNotNull(r1.getTimeRanges());
        assertEquals(1, r1.getTimeRanges().size());
        assertEquals("10-11", r1.getTimeRanges().get(0).toString());

        Reservation r2 = reservations.get(1);
        assertEquals("Pilates", r2.getActivityName());
        assertEquals("Sala2", r2.getRoomName());
        assertEquals("2024-07-01", r2.getStartDate().toString());
        assertEquals("2024-07-31", r2.getEndDate().toString());
        assertEquals("S", r2.getDayPattern());
        assertNotNull(r2.getTimeRanges());
        assertEquals(1, r2.getTimeRanges().size());
        assertEquals("09-10", r2.getTimeRanges().get(0).toString());
    }

    @Test
    void loadReservations_emptyFile_shouldThrowIllegalArgumentException() {
        MultipartFile reservationsFile = new MockMultipartFile("reservations.txt", "reservations.txt", "text/plain", new byte[0]);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dataLoader.loadReservations(reservationsFile);
        });
        assertEquals("Reservations file is required", exception.getMessage());
    }

    @Test
    void loadReservations_nullFile_shouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dataLoader.loadReservations(null);
        });
        assertEquals("Reservations file is required", exception.getMessage());
    }
    
    @Test
    void loadReservations_emptyLines_shouldBeSkipped() throws IOException {
        String content = "Yoga Sala1 2024-07-01 2024-07-31 LMXJV 10-11\n" +
                         "\n" + // Empty line
                         "Pilates Sala2 2024-07-01 2024-07-31 S 09-10\n" +
                         "   "; // Line with only spaces
        MultipartFile reservationsFile = new MockMultipartFile("reservations.txt", "reservations.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        List<Reservation> reservations = dataLoader.loadReservations(reservationsFile);

        assertNotNull(reservations);
        assertEquals(2, reservations.size());
    }

    @Test
    void loadReservations_invalidDateFormat_shouldSkipInvalidAndLog() throws IOException {
        // Line 1: Valid (dd/MM/yyyy for Reservation, HH-MM for time)
        // Line 2: Invalid start date format for Reservation (yyyy/MM/dd)
        // Line 3: Valid
        // Line 4: Invalid end date format for Reservation (MM-dd-yyyy)
        String content = "Yoga Sala1 01/07/2024 31/07/2024 LMXJV 10-11\n" +
                         "BadDate SalaX 2024/07/01 15/07/2024 M 10-11\n" + 
                         "Pilates Sala2 02/07/2024 16/07/2024 LMXJV 11-12\n" +
                         "AnotherBad SalaY 03/07/2024 07-31-2024 M 10-11";
        MultipartFile reservationsFile = new MockMultipartFile("reservations.txt", "reservations.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        List<Reservation> reservations = dataLoader.loadReservations(reservationsFile);

        assertNotNull(reservations);
        assertEquals(2, reservations.size()); // Yoga and Pilates
        assertEquals("Yoga", reservations.get(0).getActivityName());
        assertEquals("Pilates", reservations.get(1).getActivityName());
        // Add assertions for logger if mockito is introduced for logger
    }

    @Test
    void loadReservations_insufficientParts_shouldSkipInvalidAndLog() throws IOException {
         // Line 1: Valid
         // Line 2: Missing time pattern
         // Line 3: Valid
         // Line 4: Missing day and time pattern
        String content = "Yoga Sala1 2024-07-01 2024-07-31 LMXJV 10-11\n" +
                         "TooFew SalaX 2024-07-01 2024-07-15 LMXJV\n" + // Missing time pattern
                         "Pilates Sala2 2024-07-02 2024-07-16 S 09-10\n" +
                         "EvenFewer SalaY 2024-07-03 2024-07-17"; // Missing day pattern and time pattern
        MultipartFile reservationsFile = new MockMultipartFile("reservations.txt", "reservations.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        List<Reservation> reservations = dataLoader.loadReservations(reservationsFile);

        assertNotNull(reservations);
        assertEquals(2, reservations.size());
        assertEquals("Yoga", reservations.get(0).getActivityName());
        assertEquals("Pilates", reservations.get(1).getActivityName());
        // Add assertions for logger if mockito is introduced for logger
    }

    @Test
    void loadReservations_mixedValidAndInvalidReservations_shouldLoadValid() throws IOException {
        String content = "Yoga Sala1 01/07/2024 31/07/2024 LMXJV 10-11\n" + // Valid
                         "InvalidActivity SalaInv 01/07/2024 31/07/2024 LMXJVBADPATTERN 10-11\n" + // Invalid day pattern in Reservation.isValid()
                         "Pilates Sala2 01/07/2024 31/07/2024 S 09-10\n" + // Valid
                         "BadTimeFormat SalaT 05/07/2024 10/07/2024 M 1000-1100"; // Invalid time pattern format for parseTimePattern
        MultipartFile reservationsFile = new MockMultipartFile("reservations.txt", "reservations.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        List<Reservation> reservations = dataLoader.loadReservations(reservationsFile);

        assertNotNull(reservations);
        assertEquals(2, reservations.size());
        assertEquals("Yoga", reservations.get(0).getActivityName());
        assertEquals("Pilates", reservations.get(1).getActivityName());
        // Note: The current DataLoader relies on Reservation.isValid() for some internal validation.
        // These tests verify that reservations deemed invalid by Reservation.isValid() are skipped and logged.
    }
    
     @Test
    void loadReservations_lineWithLessThanSixParts_shouldSkipAndLog() throws IOException {
        String content = "Activity Room 2024-01-01 2024-01-31 Mon"; // Missing time pattern
        MultipartFile reservationsFile = new MockMultipartFile("reservations.txt", "reservations.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        List<Reservation> reservations = dataLoader.loadReservations(reservationsFile);
        assertTrue(reservations.isEmpty());
        // We would assert logger output here if we were mocking the logger
    }

    @Test
    void loadReservations_lineWithGeneralExceptionDuringParsing_shouldSkipAndLog() throws IOException {
        // This test requires a way to make Reservation constructor throw a generic Exception
        // For now, we can simulate a problematic line that might cause an unexpected issue,
        // although specifically crafting one for a generic Exception is hard without deeper knowledge of Reservation's internal parsing.
        // We'll assume a line that leads to an error after the initial split but before full Reservation object construction.
        // Let's use a line that would pass the split but where Reservation.isValid() might fail due to internal logic throwing an unexpected error
        // For the current Reservation class, DateTimeParseException is the most likely specific exception.
        // If Reservation constructor or isValid() could throw other RuntimeExceptions, this test would be more relevant.

        // Simulating a case that results in a DateTimeParseException for demonstration, as it's caught by the generic Exception block too
        // DataLoader passes the date string as is. Reservation expects "dd/MM/yyyy".
        String content = "Yoga Sala1 01-07-2024-INVALID 31/07/2024 LMXJV 10-11\n" + // This will cause DateTimeParseException in Reservation constructor
                         "Pilates Sala2 01/07/2024 31/07/2024 S 09-10"; // Valid
        MultipartFile reservationsFile = new MockMultipartFile("reservations.txt", "reservations.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        List<Reservation> reservations = dataLoader.loadReservations(reservationsFile);
        assertNotNull(reservations);
        assertEquals(1, reservations.size()); // Only Pilates
        assertEquals("Pilates", reservations.get(0).getActivityName());
        // Assert logger output (error for the first line)
    }
} 