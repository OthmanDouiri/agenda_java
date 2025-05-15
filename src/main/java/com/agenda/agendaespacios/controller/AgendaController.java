package com.agenda.agendaespacios.controller;

import com.agenda.agendaespacios.model.Agenda;
import com.agenda.agendaespacios.model.AgendaViewModel;
import com.agenda.agendaespacios.model.Reservation;
import com.agenda.agendaespacios.service.AgendaProcessor;
import com.agenda.agendaespacios.service.DataLoader;

import ch.qos.logback.core.model.Model;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AgendaController {
    private static final Logger logger = LoggerFactory.getLogger(AgendaController.class);
    private final DataLoader dataLoader;
    private final AgendaProcessor agendaProcessor;

    @Autowired
    public AgendaController(DataLoader dataLoader, AgendaProcessor agendaProcessor) {
        this.dataLoader = dataLoader;
        this.agendaProcessor = agendaProcessor;
    }
    
    @GetMapping("/")
    public String index() {
        return "upload";
    }
    
    @GetMapping("/upload")
    public String uploadForm() {
        return "upload";
    }
    
    @PostMapping("/procesar")
    public String processFiles(@RequestParam("configFile") MultipartFile configFile,
                               @RequestParam("peticionesFile") MultipartFile reservationsFile,
                               Model model,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               RedirectAttributes redirectAttributes) {
        
        try {
            logger.info("Processing files: config={}, peticiones={}", 
                       configFile.getOriginalFilename(), reservationsFile.getOriginalFilename());
            
            // Validate files
            if (configFile == null || configFile.isEmpty() || reservationsFile == null || reservationsFile.isEmpty()) {
                logger.error("One or both uploaded files are empty");
                redirectAttributes.addFlashAttribute("error", "Please upload both configuration and bookings files.");
                return "redirect:/upload";
            }
            
            // Load configuration data
            try {
                com.agenda.agendaespacios.model.ConfigData configData = dataLoader.loadConfig(configFile);
                logger.info("Loaded config: year={}, month={}, languages={}_{}",
                           configData.getYear(), configData.getMonth(), 
                           configData.getSourceLanguage(), configData.getTargetLanguage());
                
                // Set locale based on target language
                try {
                    Locale targetLocale = configData.getLocale();
                    LocaleResolver localeResolver = org.springframework.web.servlet.support.RequestContextUtils.getLocaleResolver(request);
                    if (localeResolver != null) {
                        localeResolver.setLocale(request, response, targetLocale);
                    } else {
                        logger.warn("LocaleResolver is not available.");
                    }
                    LocaleContextHolder.setLocale(targetLocale);
                    logger.info("Set locale to: {}", targetLocale);
                    
                    // Debug message resources
                    logger.debug("Locale set to: {}", LocaleContextHolder.getLocale());
                    
                } catch (Exception e) {
                    // If there's an issue setting the locale, log it but continue
                    logger.warn("Error setting locale from target language: {}", e.getMessage());
                }
                
                // Load reservations
                List<Reservation> reservations;
                try {
                    reservations = dataLoader.loadReservations(reservationsFile);
                    logger.info("Loaded {} reservation requests", reservations.size());
                } catch (Exception e) {
                    logger.error("Error loading reservations", e);
                    reservations = Collections.emptyList();
                }
                
                // Process agenda
                Agenda agenda = agendaProcessor.createAgenda(configData, reservations);
                
                // Create view model
                AgendaViewModel viewModel = new AgendaViewModel(configData, agenda);
                logger.info("Created agenda with {} room schedules and {} conflicts", 
                           viewModel.getRoomSchedules().size(), viewModel.getConflicts().size());
                
                // Add data to model
                ((RedirectAttributes) model).addAttribute("agendaViewModel", viewModel);
                ((RedirectAttributes) model).addAttribute("locale", configData.getLocale());
                ((RedirectAttributes) model).addAttribute("targetLanguage", configData.getTargetLanguage());
                
                return "agenda";
            } catch (Exception e) {
                logger.error("Error processing configuration file", e);
                redirectAttributes.addFlashAttribute("error", 
                    "Error processing configuration file: " + e.getMessage());
                return "redirect:/upload";
            }
            
        } catch (Exception e) {
            logger.error("Error processing files", e);
            redirectAttributes.addFlashAttribute("error", 
                "Error processing files: " + e.getMessage());
            return "redirect:/upload";
        }
    }
    
    /**
     * Global exception handler for template processing errors
     */
    @ExceptionHandler({Exception.class})
    public ModelAndView handleAllExceptions(Exception ex) {
        logger.error("Unhandled exception occurred", ex);
        
        ModelAndView model = new ModelAndView("error");
        
        // Create a more informative error message
        String errorType = ex.getClass().getSimpleName();
        String errorMsg = ex.getMessage();
        
        // If no error message is available, provide a generic one
        if (errorMsg == null || errorMsg.trim().isEmpty()) {
            errorMsg = "An unexpected error occurred while processing your request.";
        }
        
        // Provide a more user-friendly error message
        model.addObject("errorMessage", errorType + ": " + errorMsg);
        
        // Include the stack trace for debugging
        model.addObject("stackTrace", getStackTraceAsString(ex));
        
        return model;
    }
    
    /**
     * Converts a stack trace to a string for displaying in the error page
     */
    private String getStackTraceAsString(Exception ex) {
        if (ex == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append("\n");
        
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        
        return sb.toString();
    }
}