package app.service;

import app.repository.EventRepository;
import app.web.dto.ActiveEventResponse;
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
}
