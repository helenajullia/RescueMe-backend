package com.rescueme.repository.dto;

import com.rescueme.repository.entity.AttendanceStatus;
import com.rescueme.repository.entity.Event;
import com.rescueme.repository.entity.EventAttendee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String location;
    private String address;
    private String eventType;
    private Integer maxAttendees;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Long shelterId;
    private String shelterName;
    private String city;
    private String county;
    private Integer interestedCount;
    private Integer goingCount;

    private AttendanceStatus userAttendanceStatus;

    public static EventResponseDTO fromEntity(Event event) {
        return fromEntity(event, null);
    }

    public static EventResponseDTO fromEntity(Event event, Long currentUserId) {
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStartDateTime(event.getStartDateTime());
        dto.setEndDateTime(event.getEndDateTime());
        dto.setLocation(event.getLocation());
        dto.setAddress(event.getAddress());
        dto.setEventType(event.getEventType());
        dto.setMaxAttendees(event.getMaxAttendees());
        dto.setCity(event.getCity());
        dto.setCounty(event.getCounty());
        dto.setIsActive(event.getIsActive());
        dto.setCreatedAt(event.getCreatedAt());

        dto.setShelterId(event.getShelter().getId());
        dto.setShelterName(event.getShelter().getUsername());

        Map<AttendanceStatus, Long> attendanceCounts = event.getAttendees().stream()
                .collect(Collectors.groupingBy(EventAttendee::getStatus, Collectors.counting()));

        dto.setInterestedCount(attendanceCounts.getOrDefault(AttendanceStatus.INTERESTED, 0L).intValue());
        dto.setGoingCount(attendanceCounts.getOrDefault(AttendanceStatus.GOING, 0L).intValue());

        if (currentUserId != null) {
            event.getAttendees().stream()
                    .filter(a -> a.getUser().getId().equals(currentUserId))
                    .findFirst()
                    .ifPresent(attendee -> dto.setUserAttendanceStatus(attendee.getStatus()));
        }

        return dto;
    }

    public static List<EventResponseDTO> fromEntities(List<Event> events, Long currentUserId) {
        return events.stream()
                .map(event -> fromEntity(event, currentUserId))
                .collect(Collectors.toList());
    }
}