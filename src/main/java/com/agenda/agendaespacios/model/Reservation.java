package com.agenda.agendaespacios.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class Reservation {
    private String activityName;
    private String roomName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String dayPattern; // e.g., "LMCJVSG" or "MTWTFSS"
    private List<TimeRange> timeRanges;
    private String errorMessage;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    // Valid day patterns for various language formats
    private static final String SPANISH_CAT_DAY_PATTERN = "LMCJVSG";
    private static final String ENGLISH_DAY_PATTERN = "MTWTFSS";
    private static final String FRENCH_DAY_PATTERN = "LMMJVSD"; // French uses D for Dimanche (Sunday)
    
    public Reservation(String activityName, String roomName, String startDate, String endDate, 
                      String dayPattern, String timePattern) {
        this.activityName = activityName;
        this.roomName = roomName;
        this.dayPattern = dayPattern;
        
        try {
            this.startDate = LocalDate.parse(startDate, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            this.errorMessage = "Invalid start date format: " + startDate + ". Expected format: dd/MM/yyyy";
        }
        
        try {
            this.endDate = LocalDate.parse(endDate, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            this.errorMessage = (this.errorMessage != null ? this.errorMessage + "; " : "") + 
                        "Invalid end date format: " + endDate + ". Expected format: dd/MM/yyyy";
        }
        
        try {
            this.timeRanges = parseTimePattern(timePattern);
            if (this.timeRanges.isEmpty()) {
                this.errorMessage = (this.errorMessage != null ? this.errorMessage + "; " : "") + 
                            "Invalid time pattern: " + timePattern;
            }
        } catch (Exception e) {
            this.errorMessage = (this.errorMessage != null ? this.errorMessage + "; " : "") + 
                        "Error parsing time pattern: " + timePattern + " - " + e.getMessage();
        }
        
        // Validate day pattern (should contain valid days in either Spanish or English format)
        if (dayPattern == null || dayPattern.trim().isEmpty()) {
            this.errorMessage = (this.errorMessage != null ? this.errorMessage + "; " : "") + 
                        "Day pattern cannot be empty";
        } else {
            validateDayPattern(dayPattern);
        }
        
        // Further validation for dates
        if (startDate != null && endDate != null && this.startDate != null && this.endDate != null) {
            if (this.startDate.isAfter(this.endDate)) {
                this.errorMessage = (this.errorMessage != null ? this.errorMessage + "; " : "") + 
                            "Start date cannot be after end date";
            }
        }
    }
    
    /**
     * Validates if the day pattern contains valid characters from Spanish day pattern (LMCJVSG)
     */
    private void validateDayPattern(String pattern) {
        for (char c : pattern.toCharArray()) {
            // Check if the character is valid in Spanish format only
            if (SPANISH_CAT_DAY_PATTERN.indexOf(c) == -1) {
                this.errorMessage = (this.errorMessage != null ? this.errorMessage + "; " : "") + 
                            "Invalid day in pattern: " + c + ". Must be one of: " + 
                            SPANISH_CAT_DAY_PATTERN;
                break;
            }
        }
    }
    
    private List<TimeRange> parseTimePattern(String timePattern) {
        List<TimeRange> ranges = new ArrayList<>();
        if (timePattern == null || timePattern.isEmpty()) {
            return ranges;
        }
        
        String[] patterns = timePattern.split("_");
        
        for (String pattern : patterns) {
            String[] hours = pattern.split("-");
            if (hours.length == 2) {
                try {
                    int start = Integer.parseInt(hours[0]);
                    int end = Integer.parseInt(hours[1]);
                    
                    // Validate time range
                    if (start < 0 || start > 23 || end < 1 || end > 24 || start >= end) {
                        throw new IllegalArgumentException(
                            "Invalid time range: " + start + "-" + end + 
                            ". Hours must be between 0-24 and start must be before end.");
                    }
                    
                    ranges.add(new TimeRange(start, end));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid hour format: " + pattern);
                }
            } else {
                throw new IllegalArgumentException("Invalid time pattern format: " + pattern);
            }
        }
        
        return ranges;
    }
    
    public String getActivityName() {
        return activityName;
    }
    
    public String getRoomName() {
        return roomName;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public String getDayPattern() {
        return dayPattern;
    }
    
    public List<TimeRange> getTimeRanges() {
        return timeRanges;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public boolean isValid() {
        // Basic validation
        return errorMessage == null && 
               startDate != null && 
               endDate != null && 
               timeRanges != null && 
               !timeRanges.isEmpty() && 
               startDate.isBefore(endDate.plusDays(1)) && 
               dayPattern != null && 
               !dayPattern.isEmpty();
    }
    
    public static class TimeRange {
        private int startHour;
        private int endHour;
        
        public TimeRange(int startHour, int endHour) {
            this.startHour = startHour;
            this.endHour = endHour;
        }
        
        public int getStartHour() {
            return startHour;
        }
        
        public int getEndHour() {
            return endHour;
        }
        
        public boolean overlaps(TimeRange other) {
            return (startHour < other.endHour && endHour > other.startHour);
        }
        
        @Override
        public String toString() {
            return startHour + "-" + endHour;
        }
    }

    public void setValid(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setValid'");
    }

    public void setErrorMessage(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setErrorMessage'");
    }
} 