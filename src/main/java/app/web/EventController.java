package app.web;

import app.service.EventService;
import app.web.dto.ActiveEventResponse;
import app.web.dto.CreateEventRequest;
import app.web.dto.EditEventRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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

    @PostMapping
    public ResponseEntity<Void> createEvent(
            @Valid @RequestBody CreateEventRequest request) {

        eventService.createEvent(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<Void> editEvent(
            @Valid @RequestBody EditEventRequest request) {

        eventService.editEvent(request);

        return ResponseEntity.ok().build();
    }


}
