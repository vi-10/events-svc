package app.scheduler;

import app.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EventSchedulerUnitTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventScheduler eventScheduler;

    @Test
    void updateEventStatus_shouldCallEventService() {

        when(eventService.updateEventStatus()).thenReturn(true);

        eventScheduler.updateEventStatus();

        verify(eventService).updateEventStatus();
    }

    @Test
    void updateEventStatus_shouldCallEventService_whenNoChangesAreRequired() {

        when(eventService.updateEventStatus()).thenReturn(false);

        eventScheduler.updateEventStatus();

        verify(eventService).updateEventStatus();
    }

    @Test
    void deactivateExpiredEvents_shouldCallEventService_whenEventsAreDeactivated() {

        when(eventService.deactivateExpiredEvents()).thenReturn(true);

        eventScheduler.deactivateExpiredEvents();

        verify(eventService).deactivateExpiredEvents();
    }

    @Test
    void deactivateExpiredEvents_shouldCallEventService_whenNoEventsAreDeactivated() {

        when(eventService.deactivateExpiredEvents()).thenReturn(false);

        eventScheduler.deactivateExpiredEvents();

        verify(eventService).deactivateExpiredEvents();
    }
}
