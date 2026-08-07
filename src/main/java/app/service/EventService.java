package app.service;

import app.exception.EventAlreadyExistsException;
import app.exception.InvalidEventException;
import app.model.Event;
import app.repository.EventRepository;
import app.web.dto.ActiveEventResponse;
import app.web.dto.CreateEventRequest;
import app.web.mapper.EventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
