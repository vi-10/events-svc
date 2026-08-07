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
}
