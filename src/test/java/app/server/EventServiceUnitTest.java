package app.server;

import app.model.Event;
import app.repository.EventRepository;
import app.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EventServiceUnitTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void updateEventStatus_shouldActivateEvent_whenCurrentlyWithinEventPeriod() {

        LocalDateTime now = LocalDateTime.now();

        Event event = Event.builder()
                .title("Active Event")
                .start(now.minusMinutes(10))
                .end(now.plusMinutes(10))
                .active(false)
                .build();

        when(eventRepository.findAll()).thenReturn(List.of(event));

        boolean result = eventService.updateEventStatus();

        assertThat(result).isTrue();
        assertThat(event.isActive()).isTrue();

        verify(eventRepository).saveAll(List.of(event));
    }

    @Test
    void updateEventStatus_shouldDeactivateEvent_whenEventHasExpired() {

        LocalDateTime now = LocalDateTime.now();

        Event event = Event.builder()
                .title("Expired Event")
                .start(now.minusHours(2))
                .end(now.minusMinutes(10))
                .active(true)
                .build();

        when(eventRepository.findAll()).thenReturn(List.of(event));

        boolean result = eventService.updateEventStatus();

        assertThat(result).isTrue();
        assertThat(event.isActive()).isFalse();

        verify(eventRepository).saveAll(List.of(event));
    }

    @Test
    void updateEventStatus_shouldKeepEventInactive_whenEventHasNotStarted() {

        LocalDateTime now = LocalDateTime.now();

        Event event = Event.builder()
                .title("Future Event")
                .start(now.plusMinutes(10))
                .end(now.plusHours(1))
                .active(false)
                .build();

        when(eventRepository.findAll()).thenReturn(List.of(event));

        boolean result = eventService.updateEventStatus();

        assertThat(result).isFalse();
        assertThat(event.isActive()).isFalse();

        verify(eventRepository, never()).saveAll(any());
    }

    @Test
    void updateEventStatus_shouldKeepEventInactive_whenStartOrEndIsNull() {

        Event eventWithoutStart = Event.builder()
                .title("No Start")
                .start(null)
                .end(LocalDateTime.now().plusHours(1))
                .active(false)
                .build();

        Event eventWithoutEnd = Event.builder()
                .title("No End")
                .start(LocalDateTime.now().minusHours(1))
                .end(null)
                .active(false)
                .build();

        when(eventRepository.findAll())
                .thenReturn(List.of(eventWithoutStart, eventWithoutEnd));

        boolean result = eventService.updateEventStatus();

        assertThat(result).isFalse();

        assertThat(eventWithoutStart.isActive()).isFalse();
        assertThat(eventWithoutEnd.isActive()).isFalse();

        verify(eventRepository, never()).saveAll(any());
    }

    @Test
    void updateEventStatus_shouldReturnFalse_whenNoStatusChangesAreRequired() {

        LocalDateTime now = LocalDateTime.now();

        Event activeEvent = Event.builder()
                .title("Active Event")
                .start(now.minusMinutes(10))
                .end(now.plusMinutes(10))
                .active(true)
                .build();

        Event inactiveEvent = Event.builder()
                .title("Future Event")
                .start(now.plusMinutes(10))
                .end(now.plusHours(1))
                .active(false)
                .build();

        when(eventRepository.findAll())
                .thenReturn(List.of(activeEvent, inactiveEvent));

        boolean result = eventService.updateEventStatus();

        assertThat(result).isFalse();

        verify(eventRepository, never()).saveAll(any());
    }
}
