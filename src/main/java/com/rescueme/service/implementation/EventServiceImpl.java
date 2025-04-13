package com.rescueme.service.implementation;

import com.rescueme.repository.EventAttendeeRepository;
import com.rescueme.repository.EventRepository;
import com.rescueme.repository.UserRepository;
import com.rescueme.repository.dto.EventResponseDTO;
import com.rescueme.repository.entity.*;
import com.rescueme.service.EventService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventAttendeeRepository attendeeRepository;

    @Override
    @Transactional
    public Event createEvent(Event event, Long shelterId) {
        User shelter = userRepository.findById(shelterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelter not found"));

        // Validate event dates
        if (event.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event start date cannot be in the past");
        }

        if (event.getEndDateTime().isBefore(event.getStartDateTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event end date must be after start date");
        }

        // Validate fields
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event title is required");
        }

        if (event.getLocation() == null || event.getLocation().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event location is required");
        }

        if (event.getAddress() == null || event.getAddress().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event address is required");
        }

        if (event.getCity() == null || event.getCity().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event city is required");
        }

        if (event.getCounty() == null || event.getCounty().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event county is required");
        }

        event.setShelter(shelter);
        return eventRepository.save(event);
    }

    @Override
    public EventResponseDTO getEventById(Long eventId, Long currentUserId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Event not found with ID: " + eventId));

        return EventResponseDTO.fromEntity(event, currentUserId);
    }

    @Override
    public List<EventResponseDTO> getAllEvents(Long currentUserId) {
        return EventResponseDTO.fromEntities(eventRepository.findAll(), currentUserId);
    }

    @Override
    public List<EventResponseDTO> getEventsByShelter(Long shelterId, Long currentUserId) {
        return EventResponseDTO.fromEntities(eventRepository.findByShelterId(shelterId), currentUserId);
    }

    @Override
    public List<EventResponseDTO> getUpcomingEvents(Long currentUserId) {
        return EventResponseDTO.fromEntities(
                eventRepository.findUpcomingEvents(LocalDateTime.now()),
                currentUserId);
    }

    @Override
    public List<EventResponseDTO> getUpcomingEventsByShelter(Long shelterId, Long currentUserId) {
        return EventResponseDTO.fromEntities(
                eventRepository.findUpcomingEventsByShelter(LocalDateTime.now(), shelterId),
                currentUserId);
    }

    @Override
    public List<EventResponseDTO> getEventsBetweenDates(LocalDateTime startDate, LocalDateTime endDate, Long currentUserId) {
        return EventResponseDTO.fromEntities(
                eventRepository.findEventsBetweenDates(startDate, endDate),
                currentUserId);
    }

    @Override
    public List<EventResponseDTO> getEventsUserIsAttending(Long userId, AttendanceStatus status) {
        List<EventAttendee> attendees = attendeeRepository.findByUserId(userId);

        if (status != null) {
            attendees = attendees.stream()
                    .filter(a -> a.getStatus() == status)
                    .toList();
        }

        return attendees.stream()
                .map(a -> EventResponseDTO.fromEntity(a.getEvent(), userId))
                .toList();
    }

    @Override
    @Transactional
    public Event updateEvent(Long eventId, Event eventDetails, Long shelterId) {
        Event existingEvent = validateEventOwnership(eventId, shelterId);

        // Validate event dates
        if (eventDetails.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event start date cannot be in the past");
        }

        if (eventDetails.getEndDateTime().isBefore(eventDetails.getStartDateTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event end date must be after start date");
        }

        // Update fields
        existingEvent.setTitle(eventDetails.getTitle());
        existingEvent.setDescription(eventDetails.getDescription());
        existingEvent.setStartDateTime(eventDetails.getStartDateTime());
        existingEvent.setEndDateTime(eventDetails.getEndDateTime());
        existingEvent.setLocation(eventDetails.getLocation());
        existingEvent.setAddress(eventDetails.getAddress());
        existingEvent.setEventType(eventDetails.getEventType());
        existingEvent.setMaxAttendees(eventDetails.getMaxAttendees());
        existingEvent.setIsActive(eventDetails.getIsActive());
        existingEvent.setCity(eventDetails.getCity());
        existingEvent.setCounty(eventDetails.getCounty());

        return eventRepository.save(existingEvent);
    }

    @Override
    @Transactional
    public Event partialUpdateEvent(Long eventId, Map<String, Object> updates, Long shelterId) {
        Event existingEvent = validateEventOwnership(eventId, shelterId);

        // Apply the partial updates
        updates.forEach((key, value) -> {
            switch (key) {
                case "title":
                    existingEvent.setTitle((String) value);
                    break;
                case "description":
                    existingEvent.setDescription((String) value);
                    break;
                case "location":
                    existingEvent.setLocation((String) value);
                    break;
                case "address":
                    existingEvent.setAddress((String) value);
                    break;
                case "eventType":
                    existingEvent.setEventType((String) value);
                    break;
                case "maxAttendees":
                    existingEvent.setMaxAttendees((Integer) value);
                    break;
                case "isActive":
                    existingEvent.setIsActive((Boolean) value);
                    break;
                case "city":
                    existingEvent.setCity((String) value);
                    break;
                case "county":
                    existingEvent.setCounty((String) value);
                    break;

                // Handle date fields separately if they're sent as strings
                default:
                    log.warn("Unknown field: {}", key);
            }
        });

        return eventRepository.save(existingEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId, Long shelterId) {
        Event event = validateEventOwnership(eventId, shelterId);
        eventRepository.delete(event);
    }

    @Override
    @Transactional
    public void setAttendanceStatus(Long eventId, Long userId, AttendanceStatus status) {
        // Verify the event exists
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Event not found with ID: " + eventId));

        // Verify the user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found with ID: " + userId));

        // Check if a maximum attendee limit exists and if it's reached for GOING status
        if (status == AttendanceStatus.GOING && event.getMaxAttendees() != null) {
            Long goingCount = attendeeRepository.countByEventIdAndStatus(eventId, AttendanceStatus.GOING);
            if (goingCount >= event.getMaxAttendees()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "This event has reached its maximum capacity");
            }
        }

        // Check if the user is already attending
        Optional<EventAttendee> existingAttendee = attendeeRepository.findByEventIdAndUserId(eventId, userId);

        if (existingAttendee.isPresent()) {
            // Update the existing status
            EventAttendee attendee = existingAttendee.get();
            attendee.setStatus(status);
            attendeeRepository.save(attendee);
        } else {
            // Create a new attendee record
            EventAttendee newAttendee = new EventAttendee();
            newAttendee.setEvent(event);
            newAttendee.setUser(user);
            newAttendee.setStatus(status);
            attendeeRepository.save(newAttendee);
        }
    }

    @Override
    @Transactional
    public void removeAttendanceStatus(Long eventId, Long userId) {
        // Check if the user is attending
        Optional<EventAttendee> existingAttendee = attendeeRepository.findByEventIdAndUserId(eventId, userId);

        if (existingAttendee.isPresent()) {
            attendeeRepository.delete(existingAttendee.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User is not registered for this event");
        }
    }

    @Override
    public Map<String, Integer> getEventAttendanceCounts(Long eventId) {
        // Verify the event exists
        if (!eventRepository.existsById(eventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Event not found with ID: " + eventId);
        }

        Map<String, Integer> counts = new HashMap<>();
        counts.put("interested", attendeeRepository.countByEventIdAndStatus(eventId, AttendanceStatus.INTERESTED).intValue());
        counts.put("going", attendeeRepository.countByEventIdAndStatus(eventId, AttendanceStatus.GOING).intValue());
        counts.put("total", attendeeRepository.countByEventId(eventId).intValue());

        return counts;
    }

    @Override
    public List<Event> getEventsByCounty(String county) {
        return eventRepository.findByCounty(county);
    }

    @Override
    public List<Event> getEventsByCity(String city) {
        return eventRepository.findByCity(city);
    }

    @Override
    public List<Event> getEventsByCountyAndCity(String county, String city) {
        return eventRepository.findByCountyAndCity(county, city);
    }

    @Override
    public List<Event> searchEvents(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return eventRepository.findAll();
        }
        return eventRepository.searchByCountyOrCity(searchTerm.trim());
    }

    @Override
    public List<Map<String, Object>> getEventsGroupedByShelter() {
        List<User> shelters = userRepository.findAllByRole(Role.SHELTER);


        return shelters.stream().map(shelter -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", shelter.getId());
            map.put("username", shelter.getUsername());
            map.put("city", shelter.getCity());
            map.put("county", shelter.getCounty());
            map.put("shelterType", shelter.getShelterType());
            map.put("mission", shelter.getMission());
            map.put("profilePicture", shelter.getProfilePicture());

            List<Event> events = eventRepository.findByShelterId(shelter.getId());
            map.put("events", events.stream().map(EventResponseDTO::fromEntity).toList());

            return map;
        }).collect(Collectors.toList());
    }


    // Helper method to validate event ownership
    private Event validateEventOwnership(Long eventId, Long shelterId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Event not found with ID: " + eventId));

        if (!event.getShelter().getId().equals(shelterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to modify this event");
        }

        return event;
    }
}