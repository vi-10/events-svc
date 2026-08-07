package app.repository;

import app.model.Event;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    Optional<Event> findByActiveTrue();

    boolean existsByTitle(String title);

    @Query("""
       SELECT COUNT(e) > 0
       FROM Event e
       WHERE e.start < :end
       AND e.end > :start
       """)
    boolean existsOverlappingEvent(
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<Event> findByTitle(String title);

    @Query("""
       SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
       FROM Event e
       WHERE e.id <> :id
       AND e.start < :end
       AND e.end > :start
       """)
    boolean existsOverlappingEventExceptCurrent(
            UUID id,
            LocalDateTime start,
            LocalDateTime end
    );
}
