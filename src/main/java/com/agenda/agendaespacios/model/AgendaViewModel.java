package com.agenda.agendaespacios.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AgendaViewModel {
    
    private static final Logger logger = LoggerFactory.getLogger(AgendaViewModel.class);
    
    private ConfigData config;
    private Agenda agenda;
    private Map<String, Object> roomSchedules = new LinkedHashMap<>(); // Changed to Object
    private List<String> conflicts;

    private static final String FREE_STATUS = "free";
    private static final String CLOSED_STATUS = "Closed";

    public AgendaViewModel(ConfigData config, Agenda agenda) {
        this.config = config;
        this.agenda = agenda;
        this.conflicts = new LinkedList<>();
    }

    public ConfigData getConfig() {
        return config;
    }

    public Map<String, Object> getRoomSchedules() {
        return roomSchedules;
    }

    public List<String> getConflicts() {
        return conflicts;
    }
}