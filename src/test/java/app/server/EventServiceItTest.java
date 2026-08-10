package app.server;

import app.exception.EventAlreadyExistsException;
import app.exception.InvalidEventException;
import app.model.Event;
import app.model.QuestType;
import app.repository.EventRepository;
import app.service.EventService;
import app.web.dto.ActiveEventResponse;
import app.web.dto.CreateEventRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static app.util.EventFactory.getActiveEvent;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EventServiceItTest {
    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCaches() {
        Objects.requireNonNull(cacheManager.getCache("activeEvent")).clear();
        Objects.requireNonNull(cacheManager.getCache("events")).clear();
    }

    @Test
    void getActiveEvent_shouldReturnActiveEvent() {

        Event event = getActiveEvent();

        eventRepository.save(event);

        ActiveEventResponse result = eventService.getActiveEvent();

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Double XP Weekend");
        assertThat(result.getDescription()).isEqualTo("Earn extra XP from quests.");
        assertThat(result.getAffectedQuestType()).isEqualTo(QuestType.COMBAT);
        assertThat(result.getBonusXp()).isEqualTo(100);
        assertThat(result.getBonusGold()).isEqualTo(50);
        assertThat(result.getStart()).isEqualTo(event.getStart());
        assertThat(result.getEnd()).isEqualTo(event.getEnd());
    }


    @Test
    void getActiveEvent_shouldReturnNull_whenNoActiveEventExists() {

        ActiveEventResponse result = eventService.getActiveEvent();

        assertThat(result).isNull();
    }

    @Test
    void getActiveEvent_shouldReturnCachedResult_onSecondCall() {

        Event event = getActiveEvent();

        eventRepository.save(event);

        ActiveEventResponse firstResult = eventService.getActiveEvent();

        event.setTitle("Changed Title");
        eventRepository.save(event);

        ActiveEventResponse secondResult = eventService.getActiveEvent();

        assertThat(firstResult.getTitle()).isEqualTo("Double XP Weekend");
        assertThat(secondResult.getTitle()).isEqualTo("Double XP Weekend");
    }

    @Test
    void createEvent_shouldCreateEventSuccessfully() {

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        CreateEventRequest request = CreateEventRequest.builder()
                .title("Double XP Weekend")
                .description("Earn double XP from quests.")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(100)
                .bonusGold(50)
                .start(start)
                .end(end)
                .build();;

        eventService.createEvent(request);

        Optional<Event> savedEvent = eventRepository.findByTitle("Double XP Weekend");

        assertThat(savedEvent).isPresent();
        assertThat(savedEvent.get().getTitle()).isEqualTo("Double XP Weekend");
        assertThat(savedEvent.get().getDescription())
                .isEqualTo("Earn double XP from quests.");
        assertThat(savedEvent.get().getAffectedQuestType())
                .isEqualTo(QuestType.COMBAT);
        assertThat(savedEvent.get().getBonusXp()).isEqualTo(100);
        assertThat(savedEvent.get().getBonusGold()).isEqualTo(50);
        assertThat(savedEvent.get().getStart()).isEqualTo(start);
        assertThat(savedEvent.get().getEnd()).isEqualTo(end);
        assertThat(savedEvent.get().isActive()).isFalse();
    }

    @Test
    void createEvent_shouldThrowException_whenTitleAlreadyExists() {

        Event existingEvent = Event.builder()
                .title("Existing Event")
                .description("Existing description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(50)
                .bonusGold(25)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .active(false)
                .build();

        eventRepository.save(existingEvent);

        CreateEventRequest request = CreateEventRequest.builder()
                .title("Existing Event")
                .description("New description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(100)
                .bonusGold(50)
                .start(LocalDateTime.now().plusHours(3))
                .end(LocalDateTime.now().plusHours(4))
                .build();

        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(EventAlreadyExistsException.class);
    }

    @Test
    void createEvent_shouldThrowException_whenStartIsNotBeforeEnd() {

        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.minusHours(1);

        CreateEventRequest request = CreateEventRequest.builder()
                .title("Invalid Event")
                .description("Invalid event description.")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(100)
                .bonusGold(50)
                .start(start)
                .end(end)
                .build();

        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(InvalidEventException.class);
    }

    @Test
    void createEvent_shouldThrowException_whenEventsOverlap() {

        LocalDateTime existingStart = LocalDateTime.now().plusHours(1);
        LocalDateTime existingEnd = existingStart.plusHours(3);

        Event existingEvent = Event.builder()
                .title("Existing Event")
                .description("Existing description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(50)
                .bonusGold(25)
                .start(existingStart)
                .end(existingEnd)
                .active(false)
                .build();

        eventRepository.save(existingEvent);

        CreateEventRequest request = CreateEventRequest.builder()
                .title("Overlapping Event")
                .description("Overlapping description.")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(100)
                .bonusGold(50)
                .start(existingStart.plusHours(1))
                .end(existingEnd.plusHours(1))
                .build();

        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(InvalidEventException.class);
    }






}
