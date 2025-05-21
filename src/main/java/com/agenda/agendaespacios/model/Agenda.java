package com.agenda.agendaespacios.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Agenda {
    private static final Logger logger = LoggerFactory.getLogger(Agenda.class);
    
    private ConfigData config;
    private Map<String, RoomSchedule> roomSchedules;
    private List<String> conflicts;
    
    // Default statuses
    private static final String FREE_STATUS = "free";
    private static final String CLOSED_STATUS = "Closed";
    
    public Agenda(ConfigData config) {
        this.config = config;
        this.roomSchedules = new HashMap<>();
        this.conflicts = new ArrayList<>();
        
        // Initialize default schedules for Sala1 and Sala2
        initializeDefaultSchedules();
    }
    
    /**
     * Initialize default schedules for rooms with closed hours
     */
    private void initializeDefaultSchedules() {
        YearMonth yearMonth = YearMonth.of(config.getYear(), config.getMonth());
        
        // Initialize Sala1 and Sala2 with default schedules
        for (String roomName : Arrays.asList("Sala1", "Sala2")) {
            RoomSchedule roomSchedule = new RoomSchedule(yearMonth);
            
            // Set default closed hours for all days of the week
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = yearMonth.atDay(day);
                Map<Integer, String> daySchedule = roomSchedule.getSchedule().get(date);
                
                // Set hours 0-7 as closed
                for (int hour = 0; hour < 7; hour++) {
                    daySchedule.put(hour, CLOSED_STATUS);
                }
                
                // Set hours 21-24 as closed
                for (int hour = 21; hour < 24; hour++) {
                    daySchedule.put(hour, CLOSED_STATUS);
                }
            }
            
            roomSchedules.put(roomName, roomSchedule);
        }
    }
    
    public void addReservation(Reservation reservation) {
        if (!reservation.isValid()) {
            addConflict("Invalid reservation format: " + reservation.getActivityName() + 
                       (reservation.getErrorMessage() != null ? " - " + reservation.getErrorMessage() : ""));
            logger.warn("Invalid reservation: {}", reservation.getActivityName());
            return;
        }
        
        // Check if reservation is for the current month/year
        YearMonth yearMonth = YearMonth.of(config.getYear(), config.getMonth());
        
        // If reservation's date range doesn't include the target month, skip it
        if (reservation.getEndDate().isBefore(yearMonth.atDay(1)) || 
            reservation.getStartDate().isAfter(yearMonth.atEndOfMonth())) {
            return;
        }
        
        String roomName = reservation.getRoomName();
        RoomSchedule roomSchedule = roomSchedules.computeIfAbsent(roomName, 
                name -> new RoomSchedule(yearMonth));
        
        // Try to add the reservation to the room schedule
        List<String> conflictDetails = roomSchedule.addReservationWithDetails(reservation);
        if (!conflictDetails.isEmpty()) {
            for (String detail : conflictDetails) {
                addConflict("Conflict in room " + roomName + " for activity " + 
                           reservation.getActivityName() + ": " + detail);
            }
            logger.warn("Conflicts detected for reservation {} in room {}", 
                       reservation.getActivityName(), roomName);
        }
    }
    
    private void addConflict(String message) {
        conflicts.add(message);
    }
    
    public Map<String, RoomSchedule> getRoomSchedules() {
        return roomSchedules;
    }
    
    public List<String> getConflicts() {
        return conflicts;
    }
    
    public static class RoomSchedule {
        private static final Logger logger = LoggerFactory.getLogger(RoomSchedule.class);
        
        private YearMonth yearMonth;
        private Map<LocalDate, Map<Integer, String>> schedule; // date -> hour -> activity
        
        public RoomSchedule(YearMonth yearMonth) {
            this.yearMonth = yearMonth;
            this.schedule = new HashMap<>();
            
            // Initialize all days of the month
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = yearMonth.atDay(day);
                schedule.put(date, new HashMap<>());
            }
        }
        
        public List<String> addReservationWithDetails(Reservation reservation) {
            List<String> conflicts = new ArrayList<>();
            
            LocalDate start = reservation.getStartDate().isBefore(yearMonth.atDay(1)) ? 
                yearMonth.atDay(1) : reservation.getStartDate();
            
            LocalDate end = reservation.getEndDate().isAfter(yearMonth.atEndOfMonth()) ? 
                yearMonth.atEndOfMonth() : reservation.getEndDate();
            
            // Check each day in the reservation period
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                // Get day of week (1-7, where 1 is Monday)
                int dayOfWeek = date.getDayOfWeek().getValue();
                
                // Convert to index in the pattern string (0-6)
                int dayIndex = dayOfWeek - 1;
                
                // Get the correct day code based on pattern language
                char dayCode = getCorrectDayCodeForIndex(reservation.getDayPattern(), dayIndex);
                
                // Check if this day is included in the day pattern
                if (dayCode != '-') {
                    // Check and reserve each time slot
                    for (Reservation.TimeRange timeRange : reservation.getTimeRanges()) {
                        for (int hour = timeRange.getStartHour(); hour < timeRange.getEndHour(); hour++) {
                            Map<Integer, String> daySchedule = schedule.get(date);
                            
                            // Special handling for "Closed" entries
                            // If current reservation is "Closed" and the slot is already booked for "Closed",
                            // we don't consider it a conflict
                            boolean isClosedConflict = "Closed".equals(reservation.getActivityName()) &&
                                daySchedule.containsKey(hour) &&
                                "Closed".equals(daySchedule.get(hour));
                            
                            // If time slot already taken and it's not a Closed-Closed conflict, add to conflicts
                            if (daySchedule.containsKey(hour) && !isClosedConflict) {
                                // Add conflict details
                                conflicts.add("Time slot " + hour + "-" + (hour + 1) + 
                                    " on " + date + " already booked for '" + daySchedule.get(hour) + "'");
                            } else if (!daySchedule.containsKey(hour) || !isClosedConflict) {
                                // Otherwise, reserve the slot (but don't overwrite existing Closed with new Closed)
                                daySchedule.put(hour, reservation.getActivityName());
                            }
                        }
                    }
                }
            }
            
            return conflicts;
        }
        
        public boolean addReservation(Reservation reservation) {
            return addReservationWithDetails(reservation).isEmpty();
        }
        
        public Map<LocalDate, Map<Integer, String>> getSchedule() {
            return schedule;
        }
        
        public YearMonth getYearMonth() {
            return yearMonth;
        }
        
        /**
         * Gets the correct day code for the given day index based on the pattern format
         */
        private char getCorrectDayCodeForIndex(String dayPattern, int dayIndex) {
            if (dayIndex < 0 || dayIndex >= 7 || dayPattern == null || dayPattern.isEmpty()) {
                return '-';
            }
            
            // Always use Spanish format (LMCJVSG) for day codes
            String daysPattern = "LMCJVSG";
            if (dayIndex < daysPattern.length()) {
                char dayCode = daysPattern.charAt(dayIndex);
                return dayPattern.indexOf(dayCode) >= 0 ? dayCode : '-';
            }
            
            return '-';
        }
    }
} 