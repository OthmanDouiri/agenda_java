package com.agenda.agendaespacios.service;

import com.agenda.agendaespacios.model.Agenda;
import com.agenda.agendaespacios.model.ConfigData;
import com.agenda.agendaespacios.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgendaProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(AgendaProcessor.class);
    
    /**
     * Creates an agenda from the provided configuration and reservations
     */
    public Agenda createAgenda(ConfigData config, List<Reservation> reservations) {
        
        // Create a new agenda with the given configuration
        Agenda agenda = new Agenda(config);
        
        if (reservations == null || reservations.isEmpty()) {
            logger.warn("No reservations provided, returning empty agenda");
            return agenda;
        }
        
        // Process each reservation
        int validCount = 0;
        int invalidCount = 0;
        int closedConflicts = 0;
        
        // First, process all the "Closed" (closed) reservations
        for (Reservation reservation : reservations) {
            try {
                if ("Closed".equals(reservation.getActivityName())) {
                    processReservation(reservation, config, agenda);
                    validCount++;
                }
            } catch (Exception e) {
                logger.error("Error processing closed reservation: " + reservation.getActivityName(), e);
                agenda.getConflicts().add("Error processing closed reservation " + 
                    reservation.getActivityName() + ": " + e.getMessage());
                invalidCount++;
            }
        }
        
        // Then process all the regular activity reservations
        for (Reservation reservation : reservations) {
            try {
                if (!"Closed".equals(reservation.getActivityName())) {
                    processReservation(reservation, config, agenda);
                    validCount++;
                }
            } catch (Exception e) {
                logger.error("Error processing reservation: " + reservation.getActivityName(), e);
                agenda.getConflicts().add("Error processing reservation " + 
                    reservation.getActivityName() + ": " + e.getMessage());
                invalidCount++;
            }
        }
        return agenda;
    }

    /**
     * Process a single reservation
     */
    private void processReservation(Reservation reservation, ConfigData config, Agenda agenda) {
        if (!reservation.isValid()) {
            logger.warn("Skipping invalid reservation: {} - Error: {}", 
                      reservation.getActivityName(), 
                      reservation.getErrorMessage());
            return;
        }
        
        // Apply language translation to the day pattern if needed
        String translatedDayPattern = config.translateDayCodes(reservation.getDayPattern());
        
        // Create a new reservation with the translated day pattern if necessary
        Reservation processedReservation = translatedDayPattern.equals(reservation.getDayPattern()) 
            ? reservation 
            : new Reservation(
                reservation.getActivityName(),
                reservation.getRoomName(),
                reservation.getStartDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                reservation.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                translatedDayPattern,
                String.join("_", reservation.getTimeRanges().stream()
                    .map(range -> range.toString())
                    .toArray(String[]::new))
            );
            
        // Add the reservation to the agenda
        agenda.addReservation(processedReservation);
    }
} 