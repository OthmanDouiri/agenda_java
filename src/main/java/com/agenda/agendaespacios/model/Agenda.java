package com.agenda.agendaespacios.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Agenda {
    private static final Logger logger = LoggerFactory.getLogger(Agenda.class);
    
    private ConfigData config;
    private Map<String, Object> roomSchedules; // Changed to Object as RoomSchedule is removed
    private List<String> conflicts;
    
    // Default statuses
    private static final String FREE_STATUS = "free";
    private static final String CLOSED_STATUS = "Closed";
    
    public Agenda(ConfigData config) {
        this.config = config;
        this.roomSchedules = new HashMap<>();
        this.conflicts = new ArrayList<>();
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
        // Removed RoomSchedule logic
    }
    
    private void addConflict(String message) {
        conflicts.add(message);
    }
    
    public Map<String, Object> getRoomSchedules() {
        return roomSchedules;
    }
    
    public List<String> getConflicts() {
        return conflicts;
    }
}