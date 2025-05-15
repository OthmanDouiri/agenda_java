package com.agenda.agendaespacios.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class AgendaTest {

    @Test
    void testConstructorInitializesEmptySchedulesAndConflicts() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        Agenda agenda = new Agenda(config);
        assertNotNull(agenda.getRoomSchedules());
        assertNotNull(agenda.getConflicts());
        assertTrue(agenda.getRoomSchedules().isEmpty());
        assertTrue(agenda.getConflicts().isEmpty());
    }

    @Test
    void testAddValidReservationNoConflict() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        Agenda agenda = new Agenda(config);
        Reservation reservation = new Reservation(
            "Clase",
            "Aula 1",
            "01/07/2024",
            "05/07/2024",
            "LMCJV",
            "08:00-09:00"
        );
        agenda.addReservation(reservation);
        assertTrue(agenda.getConflicts().isEmpty());
        // Como no hay lógica de almacenamiento, roomSchedules sigue vacío
        assertTrue(agenda.getRoomSchedules().isEmpty());
    }

    @Test
    void testAddInvalidReservationAddsConflict() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        Agenda agenda = new Agenda(config);
        Reservation reservation = new Reservation(
            "", // nombre vacío
            "Aula 1",
            "01/07/2024",
            "05/07/2024",
            "LMCJV",
            "08:00-09:00"
        );
        reservation.setValid(false);
        reservation.setErrorMessage("Nombre de actividad vacío");
        agenda.addReservation(reservation);
        List<String> conflicts = agenda.getConflicts();
        assertFalse(conflicts.isEmpty());
        assertTrue(conflicts.get(0).contains("Invalid reservation format"));
    }

    @Test
    void testAddReservationOutOfMonthIgnored() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        Agenda agenda = new Agenda(config);
        Reservation reservation = new Reservation(
            "Clase",
            "Aula 1",
            "01/06/2024",
            "30/06/2024",
            "LMCJV",
            "08:00-09:00"
        );
        agenda.addReservation(reservation);
        assertTrue(agenda.getConflicts().isEmpty());
        assertTrue(agenda.getRoomSchedules().isEmpty());
    }

    @Test
    void testAddReservationSpanningMonthIncluded() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        Agenda agenda = new Agenda(config);
        Reservation reservation = new Reservation(
            "Clase",
            "Aula 1",
            "28/06/2024",
            "05/07/2024",
            "LMCJV",
            "08:00-09:00"
        );
        agenda.addReservation(reservation);
        // Debe ser aceptada porque incluye días de julio
        assertTrue(agenda.getConflicts().isEmpty());
    }

    @Test
    void testAddReservationWithNullConfigDoesNotThrow() {
        Agenda agenda = new Agenda(null);
        Reservation reservation = new Reservation(
            "Clase",
            "Aula 1",
            "01/07/2024",
            "05/07/2024",
            "LMCJV",
            "08:00-09:00"
        );
        assertDoesNotThrow(() -> agenda.addReservation(reservation));
    }

    @Test
    void testAddEventAndGetEventsThrowsUnsupportedOperation() {
        ConfigData config = new ConfigData(2024, 7, "ESP", "ENG");
        Agenda agenda = new Agenda(config);
        org.yaml.snakeyaml.events.Event event = null;
        assertThrows(UnsupportedOperationException.class, () -> agenda.addEvent(event));
        assertThrows(UnsupportedOperationException.class, () -> agenda.getEvents());
    }
}
