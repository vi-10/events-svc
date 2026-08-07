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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public ActiveEventResponse getActiveEvent() {

        return eventRepository.findByActiveTrue()
                .map(EventMapper::toActiveEventResponse)
                .orElse(null);
    }

    public void createEvent(CreateEventRequest request) {
        if (eventRepository.existsByTitle(request.getTitle())) {
            throw new EventAlreadyExistsException("An event with this title already exists");
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
    }

    public void editEvent(EditEventRequest request) {
        Event event = eventRepository.findById(request.getId())
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        Optional<Event> existingEvent = eventRepository.findByTitle(request.getTitle());

        if (existingEvent.isPresent() && !existingEvent.get().getId().equals(request.getId())) {
            throw new EventAlreadyExistsException("An event with this title already exists"
            );
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
    }

    public List<EventDTO> getAllEvents() {
        return eventRepository.findAll().stream().map(EventMapper::toEventDTO).toList();
    }

    public void deleteEvent(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException("Event not found");
        }

        eventRepository.deleteById(eventId);
    }
}
