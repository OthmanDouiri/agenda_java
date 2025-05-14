package com.agenda.agendaespacios.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class ReservationTest {

    @Test
    void testValidReservation() {
        Reservation reservation = new Reservation("Yoga Class", "Room A", "01/01/2025", "01/01/2025", "L", "9-10");
        assertTrue(reservation.isValid());
        assertNull(reservation.getErrorMessage());
        assertEquals("Yoga Class", reservation.getActivityName());
        assertEquals("Room A", reservation.getRoomName());
        assertEquals("01/01/2025", reservation.getStartDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        assertEquals("01/01/2025", reservation.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        assertEquals("L", reservation.getDayPattern());
        assertNotNull(reservation.getTimeRanges());
        assertEquals(1, reservation.getTimeRanges().size());
        assertEquals(9, reservation.getTimeRanges().get(0).getStartHour());
        assertEquals(10, reservation.getTimeRanges().get(0).getEndHour());
    }

    @Test
    void testInvalidStartDateFormate() {
        Reservation reservation = new Reservation("Pilates", "Room B", "2025/01/01", "01/01/2025", "M", "10-11");
        assertFalse(reservation.isValid());
        assertNotNull(reservation.getErrorMessage());
        assertTrue(reservation.getErrorMessage().contains("Invalid start date format"));
    }

    @Test
    void testInvalidEndDateFormate() {
        Reservation reservation = new Reservation("Zumba", "Room C", "01/01/2025", "2025-01-01", "X", "11-12");
        assertFalse(reservation.isValid());
        assertNotNull(reservation.getErrorMessage());
        assertTrue(reservation.getErrorMessage().contains("Invalid end date format"));
    }

    @Test
    void testStartDateAfterEndDate() {
        Reservation reservation = new Reservation("Spinning", "Room D", "02/01/2025", "01/01/2025", "J", "12-13");
        assertFalse(reservation.isValid());
        assertNotNull(reservation.getErrorMessage());
        assertTrue(reservation.getErrorMessage().contains("Start date cannot be after end date"));
    }

    @Test
    void testEmptyDayPattern() {
        Reservation reservation = new Reservation("Boxing", "Room E", "01/01/2025", "01/01/2025", "", "13-14");
        assertFalse(reservation.isValid());
        assertNotNull(reservation.getErrorMessage());
        assertTrue(reservation.getErrorMessage().contains("Day pattern cannot be empty"));
    }

    @Test
    void testInvalidDayInPattern() {
        Reservation reservation = new Reservation("Yoga", "Main Hall", "01/08/2024", "01/08/2024", "LMW", "10-12");
        assertFalse(reservation.isValid());
        assertNotNull(reservation.getErrorMessage());
        assertTrue(reservation.getErrorMessage().contains("Invalid day in pattern: W"));
    }
    
    @Test
    void testFrenchDayPatternIsValid() {
        Reservation reservation = new Reservation("French Class", "Room F", "03/03/2025", "03/03/2025", "LMMJVSD", "10-11");
        assertTrue(reservation.isValid());
        assertNull(reservation.getErrorMessage());
    }

    @Test
    void testEnglishDayPatternIsValid() {
        Reservation reservation = new Reservation("English Class", "Room G", "04/04/2025", "04/04/2025", "MTWTFSS", "11-12");
        assertTrue(reservation.isValid());
        assertNull(reservation.getErrorMessage());
    }


    @Test
    void testInvalidTimePatternFormat() {
        Reservation reservation = new Reservation("Aerobics", "Room H", "01/01/2025", "01/01/2025", "V", "14_15"); // Missing dash
        assertFalse(reservation.isValid());
        assertNotNull(reservation.getErrorMessage());
        assertTrue(reservation.getErrorMessage().contains("Invalid time pattern format: 14_15"));
    }

    @Test
    void testInvalidTimePatternHourFormat() {
        Reservation reservation = new Reservation("Crossfit", "Room I", "01/01/2025", "01/01/2025", "S", "9-1A"); // Non-numeric hour
        assertFalse(reservation.isValid());
        assertNotNull(reservation.getErrorMessage());
        assertTrue(reservation.getErrorMessage().contains("Invalid hour format: 9-1A"));
    }

    @Test
    void testInvalidTimeRange() {
        Reservation reservation = new Reservation("HIIT", "Room J", "01/01/2025", "01/01/2025", "G", "25-26"); // Hour out of bounds
        assertFalse(reservation.isValid());
        assertNotNull(reservation.getErrorMessage());
        assertTrue(reservation.getErrorMessage().contains("Invalid time range: 25-26"));
    }

    @Test
    void testTimeRangeStartNotBeforeEnd() {
        Reservation reservation = new Reservation("Dance", "Room K", "01/01/2025", "01/01/2025", "L", "10-9"); // Start hour after end hour
        assertFalse(reservation.isValid());
        assertNotNull(reservation.getErrorMessage());
        assertTrue(reservation.getErrorMessage().contains("Invalid time range: 10-9"));
    }
    
    @Test
    void testMultipleTimeRanges() {
        Reservation reservation = new Reservation("Workshop", "Room L", "05/05/2025", "05/05/2025", "M", "9-11_14-16");
        assertTrue(reservation.isValid());
        assertNull(reservation.getErrorMessage());
        List<Reservation.TimeRange> timeRanges = reservation.getTimeRanges();
        assertNotNull(timeRanges);
        assertEquals(2, timeRanges.size());
        assertEquals(9, timeRanges.get(0).getStartHour());
        assertEquals(11, timeRanges.get(0).getEndHour());
        assertEquals(14, timeRanges.get(1).getStartHour());
        assertEquals(16, timeRanges.get(1).getEndHour());
    }

    @Test
    void testMultipleErrors() {
        // Invalid start date, invalid day pattern, invalid time pattern
        Reservation reservation = new Reservation("Test Event", "Test Room", "32/01/2025", "01/02/2025", "LX", "25-26");
        assertFalse(reservation.isValid());
        String errorMessage = reservation.getErrorMessage();
        assertNotNull(errorMessage);
        assertTrue(errorMessage.contains("Invalid start date format"));
        assertTrue(errorMessage.contains("Invalid day in pattern: X"));
        assertTrue(errorMessage.contains("Invalid time range: 25-26"));
    }

    // Tests for TimeRange inner class
    @Test
    void testTimeRangeToString() {
        Reservation.TimeRange timeRange = new Reservation.TimeRange(9, 17);
        assertEquals("9-17", timeRange.toString());
    }

    @Test
    void testTimeRangeOverlaps() {
        Reservation.TimeRange range1 = new Reservation.TimeRange(9, 12);
        Reservation.TimeRange range2 = new Reservation.TimeRange(11, 14);
        Reservation.TimeRange range3 = new Reservation.TimeRange(14, 16);
        Reservation.TimeRange range4 = new Reservation.TimeRange(10, 11); // Contained within range1
        Reservation.TimeRange range5 = new Reservation.TimeRange(8, 13); // Contains range1

        assertTrue(range1.overlaps(range2));
        assertTrue(range2.overlaps(range1)); // Overlap is symmetric

        assertFalse(range1.overlaps(range3));
        assertFalse(range3.overlaps(range1));

        assertTrue(range1.overlaps(range4));
        assertTrue(range4.overlaps(range1));
        
        assertTrue(range1.overlaps(range5));
        assertTrue(range5.overlaps(range1));
        
        Reservation.TimeRange rA = new Reservation.TimeRange(9,10);
        Reservation.TimeRange rB = new Reservation.TimeRange(10,11);
        assertFalse(rA.overlaps(rB), "Adjacent non-overlapping ranges should return false");
        assertFalse(rB.overlaps(rA), "Adjacent non-overlapping ranges should return false");


    }
    
     @Test
    void testTimeRangeNoOverlapSameEndStart() {
        Reservation.TimeRange range1 = new Reservation.TimeRange(9, 10);
        Reservation.TimeRange range2 = new Reservation.TimeRange(10, 11);
        assertFalse(range1.overlaps(range2));
        assertFalse(range2.overlaps(range1));
    }
} 