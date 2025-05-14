package com.agenda.agendaespacios.service;

import com.agenda.agendaespacios.model.ConfigData;
import com.agenda.agendaespacios.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    
    public ConfigData loadConfig(MultipartFile configFile) throws IOException {
        if (configFile == null || configFile.isEmpty()) {
            logger.error("Config file is empty or null");
            throw new IllegalArgumentException("Config file is required");
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(configFile.getInputStream(), StandardCharsets.UTF_8))) {
            // Read year and month
            String dateLine = reader.readLine();
            
            if (dateLine == null || dateLine.trim().isEmpty()) {
                logger.error("Config file first line is empty");
                throw new IllegalArgumentException("Config file must contain date information");
            }
            
            // Log hex values to detect hidden characters
            StringBuilder hexBuilder = new StringBuilder("Hex values for first line: ");
            for (byte b : dateLine.getBytes(StandardCharsets.UTF_8)) {
                hexBuilder.append(String.format("%02X ", b));
            }
            
            String[] dateParts = dateLine.trim().split("\\s+");
            
            if (dateParts.length != 2) {
                logger.error("Invalid date format in config file: '{}'", dateLine);
                throw new IllegalArgumentException("Date format must be 'YYYY MM'");
            }
            
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            
            // Read source and target languages
            String langLine = reader.readLine();
            
            // Log hex values for second line
            hexBuilder = new StringBuilder("Hex values for second line: ");
            for (byte b : langLine.getBytes(StandardCharsets.UTF_8)) {
                hexBuilder.append(String.format("%02X ", b));
            }
            
            if (langLine == null || langLine.trim().isEmpty()) {
                logger.error("Config file second line is empty");
                throw new IllegalArgumentException("Config file must contain language information");
            }
            
            String[] langParts = langLine.trim().split("\\s+");
            
            if (langParts.length != 2) {
                logger.error("Invalid language format in config file: '{}'", langLine);
                throw new IllegalArgumentException("Language format must be 'SOURCE TARGET'");
            }
            
            ConfigData config = new ConfigData(year, month, langParts[0], langParts[1]);
            return config;
        } catch (NumberFormatException e) {
            logger.error("Error parsing year/month in config file", e);
            throw new IllegalArgumentException("Invalid year or month format: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error loading config file", e);
            throw new IllegalArgumentException("Error processing config file: " + e.getMessage());
        }
    }
    
    public List<Reservation> loadReservations(MultipartFile reservationsFile) throws IOException {
        if (reservationsFile == null || reservationsFile.isEmpty()) {
            logger.error("Reservations file is empty or null");
            throw new IllegalArgumentException("Reservations file is required");
        }
        
        List<Reservation> reservations = new ArrayList<>();
        int lineNumber = 0;
        int validReservations = 0;
        int invalidReservations = 0;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(reservationsFile.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] parts = line.trim().split("\\s+");
                
                if (parts.length >= 6) {
                    try {
                        String activityName = parts[0];
                        String roomName = parts[1];
                        String startDate = parts[2];
                        String endDate = parts[3];
                        String dayPattern = parts[4];
                        String timePattern = parts[5];
                        
                        Reservation reservation = new Reservation(
                            activityName, roomName, startDate, endDate, dayPattern, timePattern);
                        
                        if (reservation.isValid()) {
                            reservations.add(reservation);
                            validReservations++;
                        } else {
                            invalidReservations++;
                            logger.warn("Invalid reservation at line {}: '{}', Error: {}", 
                                      lineNumber, line, reservation.getErrorMessage());
                        }
                    } catch (DateTimeParseException e) {
                        invalidReservations++;
                        logger.error("Error parsing date in reservation at line {}: {}", lineNumber, e.getMessage());
                        // Continue processing other lines
                    } catch (Exception e) {
                        invalidReservations++;
                        logger.error("Error parsing reservation at line {}: {}", lineNumber, e.getMessage());
                        // Continue processing other lines
                    }
                } else {
                    invalidReservations++;
                    logger.warn("Invalid format at line {}, expected at least 6 parts but got {}: '{}'",
                               lineNumber, parts.length, line);
                }
            }
        }
        
        return reservations;
    }
}
