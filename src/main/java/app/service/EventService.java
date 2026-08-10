package app.service;

import app.exception.EventAlreadyExistsException;
import app.exception.EventNotFoundException;
import app.exception.InvalidEventException;
import app.model.Event;
import app.repository.EventRepository;
import app.web.dto.ActiveEventResponse;
import app.web.dto.CreateEventRequest;
import app.web.dto.EditEventRequest;
import app.web.dto.EventDTO;
import app.web.mapper.EventMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Cacheable("activeEvent")
    public ActiveEventResponse getActiveEvent() {
        log.debug("Fetching active event");

        return eventRepository.findByActiveTrue()
                .map(EventMapper::toActiveEventResponse)
                .orElse(null);
    }

    @CacheEvict(value = {"events", "activeEvent"}, allEntries = true)
    public void createEvent(CreateEventRequest request) {
        log.info("Creating event with title '{}'", request.getTitle());

        if (eventRepository.existsByTitle(request.getTitle())) {
            throw new EventAlreadyExistsException(request.getTitle());
        }

        if (!request.getStart().isBefore(request.getEnd())) {
            throw new InvalidEventException(
                    "Event start date must be before end date."
            );
        }

        if(eventRepository.existsOverlappingEvent(request.getStart(), request.getEnd())) {
            throw new InvalidEventException(
                    "Another event is already active during this period."
            );
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .affectedQuestType(request.getAffectedQuestType())
                .bonusXp(request.getBonusXp())
                .bonusGold(request.getBonusGold())
                .start(request.getStart())
                .end(request.getEnd())
                .build();

        eventRepository.save(event);

        log.info("Event '{}' created successfully with ID {}", event.getTitle(), event.getId());
    }

    @CacheEvict(value = {"events", "activeEvent"}, allEntries = true)
    public void editEvent(EditEventRequest request) {
        log.info("Editing event with ID {}", request.getId());

        Event event = eventRepository.findById(request.getId())
                .orElseThrow(EventNotFoundException::new);

        Optional<Event> existingEvent = eventRepository.findByTitle(request.getTitle());

        if (existingEvent.isPresent() && !existingEvent.get().getId().equals(request.getId())) {
            throw new EventAlreadyExistsException(request.getTitle());
        }

        if (!request.getStart().isBefore(request.getEnd())) {
            throw new InvalidEventException(
                    "Event start date must be before end date."
            );
        }

        if(eventRepository.existsOverlappingEventExceptCurrent(request.getId(), request.getStart(), request.getEnd())) {
            throw new InvalidEventException(
                    "Another event is already active during this period."
            );
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setAffectedQuestType(request.getAffectedQuestType());
        event.setBonusXp(request.getBonusXp());
        event.setBonusGold(request.getBonusGold());
        event.setStart(request.getStart());
        event.setEnd(request.getEnd());

        eventRepository.save(event);

        log.info("Event with ID {} edited successfully", event.getId());
    }

    @Cacheable("events")
    public List<EventDTO> getAllEvents() {
        log.debug("Fetching all events");

        List<EventDTO> events =  eventRepository.findAll().stream().map(EventMapper::toEventDTO).toList();

        log.debug("Fetched {} events", events.size());

        return events;
    }

    @CacheEvict(value = {"events", "activeEvent"}, allEntries = true)
    public void deleteEvent(UUID eventId) {
        log.info("Deleting event with ID {}", eventId);

        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException();
        }

        eventRepository.deleteById(eventId);

        log.info("Event with ID {} deleted successfully", eventId);
    }

    @Transactional
    @CacheEvict(value = {"events", "activeEvent"}, allEntries = true)
    public boolean updateEventStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findAll();
        boolean changed = false;

        for (Event event : events) {
            boolean shouldBeActive = event.getStart() != null &&
                    event.getEnd() != null &&
                    !now.isBefore(event.getStart()) &&
                    now.isBefore(event.getEnd());

            if (event.isActive() != shouldBeActive) {
                event.setActive(shouldBeActive);
                changed = true;
            }
        }

        if (changed) {
            eventRepository.saveAll(events);
        }

        return changed;
    }

    @Transactional
    @CacheEvict(value = {"events", "activeEvent"}, allEntries = true)
    public boolean deactivateExpiredEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> expiredEvents = eventRepository.findByActiveTrueAndEndBefore(now);

        if (expiredEvents.isEmpty()) {
            return false;
        }

        expiredEvents.forEach(event -> event.setActive(false));
        eventRepository.saveAll(expiredEvents);
        return true;
    }
}
