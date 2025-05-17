package com.rescueme.repository;

import com.rescueme.repository.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByShelterId(Long shelterId);
    List<Event> findByIsActiveTrue();

    @Query("SELECT e FROM Event e WHERE e.startDateTime >= :now ORDER BY e.startDateTime ASC")
    List<Event> findUpcomingEvents(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM Event e WHERE e.startDateTime >= :now AND e.shelter.id = :shelterId ORDER BY e.startDateTime ASC")
    List<Event> findUpcomingEventsByShelter(@Param("now") LocalDateTime now, @Param("shelterId") Long shelterId);

    @Query("SELECT e FROM Event e WHERE e.startDateTime BETWEEN :from AND :to ORDER BY e.startDateTime ASC")
    List<Event> findEventsBetweenDates(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    @Query("SELECT e FROM Event e WHERE e.county = :county ORDER BY e.startDateTime ASC")
    List<Event> findByCounty(@Param("county") String county);

    @Query("SELECT e FROM Event e WHERE e.city = :city ORDER BY e.startDateTime ASC")
    List<Event> findByCity(@Param("city") String city);

    @Query("SELECT e FROM Event e WHERE e.county = :county AND e.city = :city ORDER BY e.startDateTime ASC")
    List<Event> findByCountyAndCity(@Param("county") String county, @Param("city") String city);

    @Query("SELECT e FROM Event e WHERE LOWER(e.county) LIKE LOWER(CONCAT('%',:searchTerm,'%')) OR LOWER(e.city) LIKE LOWER(CONCAT('%',:searchTerm,'%'))")
    List<Event> searchByCountyOrCity(@Param("searchTerm") String searchTerm);

}