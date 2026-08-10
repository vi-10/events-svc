package app.scheduler;

import app.model.Event;
import app.repository.EventRepository;
import app.service.EventService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final EventService eventService;

    @Scheduled(cron = "0 * * * * *")
    public void updateEventStatus() {
        log.debug("Starting scheduled event status check...");

        boolean updated = eventService.updateEventStatus();

        if(updated){
            log.info("Scheduled job: Event statuses updated and caches evicted successfully.");
        } else {
            log.debug("Scheduled job: Event status check completed — no status changes required.");
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void deactivateExpiredEvents() {
        log.debug("Starting scheduled expired events deactivation...");

        boolean deactivated = eventService.deactivateExpiredEvents();

        if (deactivated) {
            log.info("Scheduled job: Expired events deactivated and caches evicted successfully.");
        } else {
            log.debug("Scheduled job: Expired events check completed — no expired events found.");
        }
    }
}
