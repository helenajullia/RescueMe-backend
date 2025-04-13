package com.rescueme.service;

import com.rescueme.repository.dto.EventResponseDTO;
import com.rescueme.repository.entity.AttendanceStatus;
import com.rescueme.repository.entity.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventService {

    Event createEvent(Event event, Long shelterId);

    EventResponseDTO getEventById(Long eventId, Long currentUserId);

    List<EventResponseDTO> getAllEvents(Long currentUserId);

    List<EventResponseDTO> getEventsByShelter(Long shelterId, Long currentUserId);

    List<EventResponseDTO> getUpcomingEvents(Long currentUserId);

    List<EventResponseDTO> getUpcomingEventsByShelter(Long shelterId, Long currentUserId);

    List<EventResponseDTO> getEventsBetweenDates(LocalDateTime startDate, LocalDateTime endDate, Long currentUserId);

    List<EventResponseDTO> getEventsUserIsAttending(Long userId, AttendanceStatus status);

    Event updateEvent(Long eventId, Event eventDetails, Long shelterId);

    Event partialUpdateEvent(Long eventId, Map<String, Object> updates, Long shelterId);

    void deleteEvent(Long eventId, Long shelterId);

    // User attendance methods
    void setAttendanceStatus(Long eventId, Long userId, AttendanceStatus status);

    void removeAttendanceStatus(Long eventId, Long userId);

    Map<String, Integer> getEventAttendanceCounts(Long eventId);

    List<Event> getEventsByCounty(String county);

    List<Event> getEventsByCity(String city);

    List<Event> getEventsByCountyAndCity(String county, String city);

    List<Event> searchEvents(String searchTerm);

    List<Map<String, Object>> getEventsGroupedByShelter();
}