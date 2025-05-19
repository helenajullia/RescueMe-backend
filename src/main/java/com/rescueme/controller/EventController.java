package com.rescueme.controller;

import com.rescueme.repository.EventRepository;
import com.rescueme.repository.dto.EventResponseDTO;
import com.rescueme.repository.entity.AttendanceStatus;
import com.rescueme.repository.entity.Event;
import com.rescueme.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    private final EventRepository eventRepository;

    /**
     * Creates a new event associated with a shelter
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_SHELTER')")
    public ResponseEntity<EventResponseDTO> createEvent(
            @RequestBody Event eventData,
            @RequestHeader("shelterId") Long shelterId) {

        try {
            Event createdEvent = eventService.createEvent(eventData, shelterId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(EventResponseDTO.fromEntity(createdEvent));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating event", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error creating event: " + e.getMessage());
        }
    }

    /**
     * Returns event details for a specific event by ID
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> getEvent(
            @PathVariable Long eventId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(eventService.getEventById(eventId, userId));
    }

    /**
     * Returns all events in the system
     */
    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents(
            @RequestHeader(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(eventService.getAllEvents(userId));
    }

    /**
     * Returns all events for a specific shelter
     */
    @GetMapping("/shelter/{shelterId}")
    public ResponseEntity<List<EventResponseDTO>> getEventsByShelter(
            @PathVariable Long shelterId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(eventService.getEventsByShelter(shelterId, userId));
    }

    /**
     * Returns upcoming events (from now to future)
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<EventResponseDTO>> getUpcomingEvents(
            @RequestHeader(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(eventService.getUpcomingEvents(userId));
    }

    /**
     * Returns upcoming events for a specific shelter
     */
    @GetMapping("/upcoming/shelter/{shelterId}")
    public ResponseEntity<List<EventResponseDTO>> getUpcomingEventsByShelter(
            @PathVariable Long shelterId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(eventService.getUpcomingEventsByShelter(shelterId, userId));
    }


    /**
     * Returns events that occur between two given dates
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<EventResponseDTO>> getEventsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader(value = "userId", required = false) Long userId) {
        return ResponseEntity.ok(eventService.getEventsBetweenDates(startDate, endDate, userId));
    }

    /**
     * Returns events where a specific user has set attendance
     * Can be filtered by attendance status
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EventResponseDTO>> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(required = false) AttendanceStatus status) {
        return ResponseEntity.ok(eventService.getEventsUserIsAttending(userId, status));
    }

    /**
     * Updates the full event data for a specific event
     */
    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long eventId,
            @RequestBody Event eventData,
            @RequestHeader("shelterId") Long shelterId) {

        try {
            Event updatedEvent = eventService.updateEvent(eventId, eventData, shelterId);
            return ResponseEntity.ok(EventResponseDTO.fromEntity(updatedEvent));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating event", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error updating event: " + e.getMessage());
        }
    }

    /**
     * Partially updates fields of a specific event
     */
    @PatchMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> partialUpdateEvent(
            @PathVariable Long eventId,
            @RequestBody Map<String, Object> updates,
            @RequestHeader("shelterId") Long shelterId) {

        Event updatedEvent = eventService.partialUpdateEvent(eventId, updates, shelterId);
        return ResponseEntity.ok(EventResponseDTO.fromEntity(updatedEvent));
    }

    /**
     * Deletes a specific event by ID
     */
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ROLE_SHELTER')")
    public ResponseEntity<Map<String, String>> deleteEvent(
            @PathVariable Long eventId,
            @RequestHeader("shelterId") Long shelterId) {

        eventService.deleteEvent(eventId, shelterId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Event deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Sets the attendance status for a user to a specific event
     */
    @PostMapping("/{eventId}/attendance")
    public ResponseEntity<Map<String, String>> setAttendanceStatus(
            @PathVariable Long eventId,
            @RequestParam AttendanceStatus status,
            @RequestHeader("userId") Long userId) {

        eventService.setAttendanceStatus(eventId, userId, status);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Attendance status set to " + status);
        return ResponseEntity.ok(response);
    }

    /**
     * Removes a user's attendance status from a specific event
     */
    @DeleteMapping("/{eventId}/attendance")
    public ResponseEntity<Map<String, String>> removeAttendanceStatus(
            @PathVariable Long eventId,
            @RequestHeader("userId") Long userId) {

        eventService.removeAttendanceStatus(eventId, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Attendance status removed");
        return ResponseEntity.ok(response);
    }

    /**
     * Returns the number of users for each attendance status for an event
     */
    @GetMapping("/{eventId}/attendance/counts")
    public ResponseEntity<Map<String, Integer>> getAttendanceCounts(
            @PathVariable Long eventId) {

        return ResponseEntity.ok(eventService.getEventAttendanceCounts(eventId));
    }

    /**
     * Returns all events grouped by shelter
     */
    @GetMapping("/shelters-with-events")
    public ResponseEntity<List<Map<String, Object>>> getAllEventsGroupedByShelter() {
        return ResponseEntity.ok(eventService.getEventsGroupedByShelter());
    }

    /**
     * Returns all active events
     */
    @GetMapping("/active")
    public ResponseEntity<List<Event>> getActiveEvents() {
        List<Event> activeEvents = eventRepository.findByIsActiveTrue();
        return ResponseEntity.ok(activeEvents);
    }
}