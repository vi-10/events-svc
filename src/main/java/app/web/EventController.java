package app.web;

import app.service.EventService;
import app.web.dto.ActiveEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<ActiveEventResponse> getActiveEvent() {

        ActiveEventResponse event = eventService.getActiveEvent();

        if (event == null) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.ok(event);
    }
}
