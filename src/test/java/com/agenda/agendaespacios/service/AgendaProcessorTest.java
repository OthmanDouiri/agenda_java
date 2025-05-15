package com.agenda.agendaespacios.service;

import com.agenda.agendaespacios.model.Agenda;
import com.agenda.agendaespacios.model.ConfigData;
import com.agenda.agendaespacios.model.Reservation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class AgendaProcessorTest {

    @Test
    void testCreateAgendaWithValidReservations() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        Reservation reservation = new Reservation(
                "Clase",
                "Aula 1",
                "01/07/2024",
                "05/07/2024",
                "LMCJV",
                "08-09"
        );
        AgendaProcessor processor = new AgendaProcessor();
        Agenda agenda = processor.createAgenda(config, Collections.singletonList(reservation));
        assertNotNull(agenda);
        assertTrue(agenda.getConflicts().isEmpty());
    }

    @Test
    void testCreateAgendaWithInvalidReservation() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        Reservation reservation = new Reservation(
                "", // nombre vacío
                "Aula 1",
                "01/07/2024",
                "05/07/2024",
                "LMCJV",
                "08-09"
        );
        reservation.setValid(false);
        reservation.setErrorMessage("Nombre de actividad vacío");
        AgendaProcessor processor = new AgendaProcessor();
        Agenda agenda = processor.createAgenda(config, Collections.singletonList(reservation));
        assertNotNull(agenda);
        assertFalse(agenda.getConflicts().isEmpty());
        assertTrue(agenda.getConflicts().get(0).contains("Invalid reservation format"));
    }

    @Test
    void testCreateAgendaWithEmptyReservations() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        AgendaProcessor processor = new AgendaProcessor();
        Agenda agenda = processor.createAgenda(config, Collections.emptyList());
        assertNotNull(agenda);
        assertTrue(agenda.getConflicts().isEmpty());
    }

    @Test
    void testCreateAgendaWithNullReservations() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        AgendaProcessor processor = new AgendaProcessor();
        Agenda agenda = processor.createAgenda(config, null);
        assertNotNull(agenda);
        assertTrue(agenda.getConflicts().isEmpty());
    }

    @Test
    void testCreateAgendaWithDayPatternTranslation() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        Reservation reservation = new Reservation(
                "Clase",
                "Aula 1",
                "01/07/2024",
                "05/07/2024",
                "LMC",
                "08-09"
        );
        AgendaProcessor processor = new AgendaProcessor();
        Agenda agenda = processor.createAgenda(config, Arrays.asList(reservation));
        assertNotNull(agenda);
        assertTrue(agenda.getConflicts().isEmpty());
        // No hay forma directa de comprobar la traducción aquí, pero no debe fallar
    }

    @Test
    void testCreateAgendaWithClosedReservation() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        Reservation closed = new Reservation(
                "Closed",
                "Aula 1",
                "01/07/2024",
                "01/07/2024",
                "L",
                "08-09"
        );
        AgendaProcessor processor = new AgendaProcessor();
        Agenda agenda = processor.createAgenda(config, Collections.singletonList(closed));
        assertNotNull(agenda);
        assertTrue(agenda.getConflicts().isEmpty());
    }
}
