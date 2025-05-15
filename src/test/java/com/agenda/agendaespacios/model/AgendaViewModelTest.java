package com.agenda.agendaespacios.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class AgendaViewModelTest {

    @Test
    void testConstructorAndGetters() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        Agenda agenda = new Agenda(config);
        agenda.getConflicts().add("Test conflict");
        AgendaViewModel viewModel = new AgendaViewModel(config, agenda);

        assertNotNull(viewModel.getConfig());
        assertEquals(config, viewModel.getConfig());
        assertNotNull(viewModel.getRoomSchedules());
        assertNotNull(viewModel.getConflicts());
        assertTrue(viewModel.getConflicts().contains("Test conflict"));
    }

    @Test
    void testRoomSchedulesIsEmptyInitially() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        Agenda agenda = new Agenda(config);
        AgendaViewModel viewModel = new AgendaViewModel(config, agenda);

        assertTrue(viewModel.getRoomSchedules().isEmpty());
    }

    @Test
    void testConflictsReflectAgendaConflicts() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        Agenda agenda = new Agenda(config);
        agenda.getConflicts().add("Error 1");
        agenda.getConflicts().add("Error 2");
        AgendaViewModel viewModel = new AgendaViewModel(config, agenda);

        List<String> conflicts = viewModel.getConflicts();
        assertEquals(2, conflicts.size());
        assertTrue(conflicts.contains("Error 1"));
        assertTrue(conflicts.contains("Error 2"));
    }

    @Test
    void testNullAgendaHandledGracefully() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        AgendaViewModel viewModel = new AgendaViewModel(config, null);

        assertNotNull(viewModel.getRoomSchedules());
        assertNotNull(viewModel.getConflicts());
        assertTrue(viewModel.getRoomSchedules().isEmpty());
        assertTrue(viewModel.getConflicts().isEmpty());
    }
}
