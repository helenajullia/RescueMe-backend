package com.rescueme.repository;

import com.rescueme.repository.entity.AttendanceStatus;
import com.rescueme.repository.entity.EventAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventAttendeeRepository extends JpaRepository<EventAttendee, Long> {

    List<EventAttendee> findByEventId(Long eventId);

    List<EventAttendee> findByUserId(Long userId);

    List<EventAttendee> findByEventIdAndStatus(Long eventId, AttendanceStatus status);

    Optional<EventAttendee> findByEventIdAndUserId(Long eventId, Long userId);

    @Query("SELECT COUNT(ea) FROM EventAttendee ea WHERE ea.event.id = :eventId AND ea.status = :status")
    Long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") AttendanceStatus status);

    @Query("SELECT COUNT(ea) FROM EventAttendee ea WHERE ea.event.id = :eventId")
    Long countByEventId(@Param("eventId") Long eventId);

    void deleteByEventIdAndUserId(Long eventId, Long userId);
}