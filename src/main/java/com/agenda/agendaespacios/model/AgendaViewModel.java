package com.agenda.agendaespacios.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgendaViewModel {
    
    private static final Logger logger = LoggerFactory.getLogger(AgendaViewModel.class);
    
    private ConfigData config;
    private Agenda agenda;
    private Map<String, List<WeekSchedule>> roomSchedules = new LinkedHashMap<>();
    private List<String> conflicts;
    
    private static final String FREE_STATUS = "free";
    private static final String CLOSED_STATUS = "Closed";
    
    public AgendaViewModel(ConfigData config, Agenda agenda) {
        this.config = config;
        this.agenda = agenda;
        this.conflicts = agenda != null ? agenda.getConflicts() : new ArrayList<>();
        
        try {
            prepareViewModel();
        } catch (Exception e) {
            logger.error("Error preparing view model", e);
            if (conflicts == null) {
                conflicts = new ArrayList<>();
            }
            conflicts.add("Error preparing view model: " + e.getMessage());
        }
    }
    
    private void prepareViewModel() {
        if (agenda == null || config == null) {
            logger.warn("Agenda or config is null, creating default empty room schedules");
            createDefaultRoomSchedules();
            return;
        }
        
        try {
            Map<String, Agenda.RoomSchedule> agendaSchedules = agenda.getRoomSchedules();
            
            // If there are no room schedules in the agenda, create default entries for Sala1 and Sala2
            if (agendaSchedules == null || agendaSchedules.isEmpty()) {
                createDefaultRoomSchedules();
            } else {
                // Process actual room schedules from the agenda
                for (Map.Entry<String, Agenda.RoomSchedule> entry : agendaSchedules.entrySet()) {
                    try {
                        String roomName = entry.getKey();
                        if (roomName == null) {
                            logger.warn("Found null room name, skipping");
                            continue;
                        }
                        
                        Agenda.RoomSchedule roomSchedule = entry.getValue();
                        if (roomSchedule == null) {
                            logger.warn("Found null room schedule for room {}, creating empty one", roomName);
                            roomSchedule = new Agenda.RoomSchedule(YearMonth.of(config.getYear(), config.getMonth()));
                        }
                        
                        List<WeekSchedule> weekSchedules = createWeekSchedules(roomName, roomSchedule);
                        roomSchedules.put(roomName, weekSchedules);
                    } catch (Exception e) {
                        logger.error("Error processing room schedule", e);
                        if (conflicts == null) {
                            conflicts = new ArrayList<>();
                        }
                        conflicts.add("Error processing room: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in prepareViewModel", e);
            throw e;
        }
    }
    
    private void createDefaultRoomSchedules() {
        // Add a note to conflicts about empty schedule
        if (conflicts != null && conflicts.isEmpty()) {
            conflicts.add("No valid bookings found for the specified month. Check your booking requests file.");
        }
        
        try {
            // Create default empty schedules
            YearMonth yearMonth = YearMonth.of(
                config != null ? config.getYear() : LocalDate.now().getYear(), 
                config != null ? config.getMonth() : LocalDate.now().getMonthValue()
            );
            
            for (String roomName : Arrays.asList("Sala1", "Sala2")) {
                Agenda.RoomSchedule roomSchedule = new Agenda.RoomSchedule(yearMonth);
                List<WeekSchedule> weekSchedules = createWeekSchedules(roomName, roomSchedule);
                roomSchedules.put(roomName, weekSchedules);
            }
        } catch (Exception e) {
            logger.error("Error creating default room schedules", e);
            if (conflicts == null) {
                conflicts = new ArrayList<>();
            }
            conflicts.add("Error creating default schedules: " + e.getMessage());
        }
    }
    
    private List<WeekSchedule> createWeekSchedules(String roomName, Agenda.RoomSchedule roomSchedule) {
        List<WeekSchedule> weekSchedules = new ArrayList<>();
        YearMonth yearMonth = roomSchedule.getYearMonth();
        
        // Group days into weeks
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();
        
        // Move to the first day of the week (Monday)
        LocalDate weekStart = firstDay.with(DayOfWeek.MONDAY);
        if (weekStart.isAfter(firstDay)) {
            weekStart = weekStart.minusWeeks(1);
        }
        
        // Create weekly schedules
        while (!weekStart.isAfter(lastDay)) {
            LocalDate weekEnd = weekStart.plusDays(6); // End of the week (Sunday)
            
            WeekSchedule weekSchedule = new WeekSchedule(weekStart);
            
            // Add days to the week
            for (int i = 0; i < 7; i++) {
                LocalDate day = weekStart.plusDays(i);
                
                Map<Integer, String> daySchedule;
                
                // For days within the month, get the schedule or create an empty one
                if (day.getMonth() == yearMonth.getMonth()) {
                    daySchedule = roomSchedule.getSchedule().getOrDefault(day, new HashMap<>());
                    
                    // Removed code that set hours 0-7 and 21-24 as CLOSED_STATUS
                } else {
                    // For days outside the month, create an empty schedule
                    daySchedule = new HashMap<>();
                }
                
                weekSchedule.addDay(day, daySchedule);
            }
            
            weekSchedules.add(weekSchedule);
            
            // Move to next week
            weekStart = weekStart.plusWeeks(1);
        }
        
        return weekSchedules;
    }
    
    public ConfigData getConfig() {
        return config;
    }
    
    public Map<String, List<WeekSchedule>> getRoomSchedules() {
        return roomSchedules;
    }
    
    public List<String> getConflicts() {
        return conflicts;
    }
    
    // Class to represent a week in the schedule
    public static class WeekSchedule {
        private LocalDate startDate;
        private List<DaySchedule> days = new ArrayList<>();
        
        public WeekSchedule(LocalDate startDate) {
            this.startDate = startDate;
        }
        
        public void addDay(LocalDate date, Map<Integer, String> schedule) {
            days.add(new DaySchedule(date, schedule));
        }
        
        public LocalDate getStartDate() {
            return startDate;
        }
        
        public List<DaySchedule> getDays() {
            return days;
        }
        
        public String getWeekDisplay() {
            return "Week " + startDate.format(DateTimeFormatter.ofPattern("d MMM"));
        }
    }
    
    // Class to represent a day in the schedule
    public static class DaySchedule {
        private LocalDate date;
        private Map<Integer, String> hourSchedule;
        
        private static final String FREE_STATUS = "free";
        private static final String CLOSED_STATUS = "Closed";
        
        public DaySchedule(LocalDate date, Map<Integer, String> hourSchedule) {
            this.date = date;
            this.hourSchedule = hourSchedule != null ? hourSchedule : new HashMap<>();
        }
        
        public LocalDate getDate() {
            return date;
        }
        
        public String getDayOfWeek() {
            if (date == null) return "-";
            try {
                return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            } catch (Exception e) {
                return date.getDayOfWeek().toString();
            }
        }
        
        public String getDateDisplay() {
            if (date == null) return "-";
            try {
                return date.format(DateTimeFormatter.ofPattern("d"));
            } catch (Exception e) {
                return String.valueOf(date.getDayOfMonth());
            }
        }
        
        public String getActivityForHour(int hour) {
            try {
                if (hourSchedule == null) {
                    return FREE_STATUS;
                }
                
                // Check if the hour is within valid range (0-23)
                if (hour < 0 || hour > 23) {
                    return FREE_STATUS;
                }
                
                // Always return FREE_STATUS if no schedule exists for this hour
                return hourSchedule.getOrDefault(hour, FREE_STATUS);
            } catch (Exception e) {
                // If any error occurs, return free status as a safe default
                return FREE_STATUS;
            }
        }
        
        public Map<Integer, String> getHourSchedule() {
            return hourSchedule != null ? hourSchedule : new HashMap<>();
        }
        
        public boolean isHourFree(int hour) {
            if (hourSchedule == null) return true;
            
            try {
                String activity = hourSchedule.get(hour);
                return activity == null || activity.equals(FREE_STATUS);
            } catch (Exception e) {
                return true;
            }
        }
    }
} 