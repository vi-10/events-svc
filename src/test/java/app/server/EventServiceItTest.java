package app.server;

import app.exception.EventAlreadyExistsException;
import app.exception.EventNotFoundException;
import app.exception.InvalidEventException;
import app.model.Event;
import app.model.QuestType;
import app.repository.EventRepository;
import app.service.EventService;
import app.web.dto.ActiveEventResponse;
import app.web.dto.CreateEventRequest;
import app.web.dto.EditEventRequest;
import app.web.dto.EventDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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

    @Test
    void editEvent_shouldUpdateEventSuccessfully() {

        LocalDateTime originalStart = LocalDateTime.now().plusHours(1);
        LocalDateTime originalEnd = originalStart.plusHours(2);

        Event event = Event.builder()
                .title("Original Event")
                .description("Original description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(50)
                .bonusGold(25)
                .start(originalStart)
                .end(originalEnd)
                .active(false)
                .build();

        event = eventRepository.save(event);

        LocalDateTime newStart = LocalDateTime.now().plusHours(5);
        LocalDateTime newEnd = newStart.plusHours(3);

        EditEventRequest request = EditEventRequest.builder()
                .id(event.getId())
                .title("Updated Event")
                .description("Updated description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(150)
                .bonusGold(100)
                .start(newStart)
                .end(newEnd)
                .build();

        eventService.editEvent(request);

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();

        assertThat(updatedEvent.getTitle()).isEqualTo("Updated Event");
        assertThat(updatedEvent.getDescription()).isEqualTo("Updated description");
        assertThat(updatedEvent.getAffectedQuestType())
                .isEqualTo(QuestType.COMBAT);
        assertThat(updatedEvent.getBonusXp()).isEqualTo(150);
        assertThat(updatedEvent.getBonusGold()).isEqualTo(100);
        assertThat(updatedEvent.getStart()).isEqualTo(newStart);
        assertThat(updatedEvent.getEnd()).isEqualTo(newEnd);
        assertThat(updatedEvent.isActive()).isFalse();
    }

    @Test
    void editEvent_shouldThrowException_whenEventDoesNotExist() {

        EditEventRequest request = EditEventRequest.builder()
                .id(UUID.randomUUID())
                .title("Updated Event")
                .description("Updated description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(100)
                .bonusGold(50)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        assertThatThrownBy(() -> eventService.editEvent(request))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void editEvent_shouldThrowException_whenTitleBelongsToAnotherEvent() {

        LocalDateTime firstStart = LocalDateTime.now().plusHours(1);
        LocalDateTime firstEnd = firstStart.plusHours(1);

        Event event = Event.builder()
                .title("Original Event")
                .description("Original description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(50)
                .bonusGold(25)
                .start(firstStart)
                .end(firstEnd)
                .active(false)
                .build();

        eventRepository.save(event);

        LocalDateTime secondStart = LocalDateTime.now().plusHours(3);
        LocalDateTime secondEnd = secondStart.plusHours(1);

        Event otherEvent = Event.builder()
                .title("Existing Title")
                .description("Other description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(75)
                .bonusGold(30)
                .start(secondStart)
                .end(secondEnd)
                .active(false)
                .build();

        eventRepository.save(otherEvent);

        EditEventRequest request = EditEventRequest.builder()
                .id(event.getId())
                .title("Existing Title")
                .description("Updated description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(100)
                .bonusGold(50)
                .start(firstStart)
                .end(firstEnd)
                .build();

        assertThatThrownBy(() -> eventService.editEvent(request))
                .isInstanceOf(EventAlreadyExistsException.class);
    }

    @Test
    void editEvent_shouldThrowException_whenStartIsNotBeforeEnd() {

        LocalDateTime start = LocalDateTime.now().plusHours(3);
        LocalDateTime end = start.minusHours(1);

        Event event = Event.builder()
                .title("Original Event")
                .description("Original description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(50)
                .bonusGold(25)
                .start(LocalDateTime.now().plusHours(5))
                .end(LocalDateTime.now().plusHours(6))
                .active(false)
                .build();

        eventRepository.save(event);

        EditEventRequest request = EditEventRequest.builder()
                .id(event.getId())
                .title("Updated Event")
                .description("Updated description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(100)
                .bonusGold(50)
                .start(start)
                .end(end)
                .build();

        assertThatThrownBy(() -> eventService.editEvent(request))
                .isInstanceOf(InvalidEventException.class);
    }

    @Test
    void editEvent_shouldThrowException_whenNewDatesOverlapAnotherEvent() {

        LocalDateTime firstStart = LocalDateTime.now().plusHours(1);
        LocalDateTime firstEnd = firstStart.plusHours(1);

        Event event = Event.builder()
                .title("Original Event")
                .description("Original description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(50)
                .bonusGold(25)
                .start(firstStart)
                .end(firstEnd)
                .active(false)
                .build();

        eventRepository.save(event);

        LocalDateTime otherStart = LocalDateTime.now().plusHours(5);
        LocalDateTime otherEnd = otherStart.plusHours(2);

        Event otherEvent = Event.builder()
                .title("Other Event")
                .description("Other description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(75)
                .bonusGold(30)
                .start(otherStart)
                .end(otherEnd)
                .active(false)
                .build();

        eventRepository.save(otherEvent);

        EditEventRequest request = EditEventRequest.builder()
                .id(event.getId())
                .title("Updated Event")
                .description("Updated description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(100)
                .bonusGold(50)
                .start(otherStart.plusMinutes(30))
                .end(otherEnd.plusMinutes(30))
                .build();

        assertThatThrownBy(() -> eventService.editEvent(request))
                .isInstanceOf(InvalidEventException.class);
    }

    @Test
    void getAllEvents_shouldReturnAllEvents() {

        Event firstEvent = Event.builder()
                .title("First Event")
                .description("First description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(100)
                .bonusGold(50)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .active(false)
                .build();

        Event secondEvent = Event.builder()
                .title("Second Event")
                .description("Second description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(200)
                .bonusGold(100)
                .start(LocalDateTime.now().plusHours(3))
                .end(LocalDateTime.now().plusHours(4))
                .active(false)
                .build();

        eventRepository.saveAll(List.of(firstEvent, secondEvent));

        List<EventDTO> result = eventService.getAllEvents();

        assertThat(result.size()).isEqualTo(2);

        assertThat(result.get(0).getTitle())
                .isEqualTo("First Event");

        assertThat(result.get(0).getDescription())
                .isEqualTo("First description");

        assertThat(result.get(1).getTitle())
                .isEqualTo("Second Event");

        assertThat(result.get(1).getDescription())
                .isEqualTo("Second description");
    }

    @Test
    void getAllEvents_shouldReturnCachedResult_onSubsequentCall() {

        Event event = Event.builder()
                .title("Cached Event")
                .description("Cached description")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(100)
                .bonusGold(50)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .active(false)
                .build();

        eventRepository.save(event);

        List<EventDTO> firstResult = eventService.getAllEvents();

        assertThat(firstResult.size()).isEqualTo(1);
        assertThat(firstResult.get(0).getTitle()).isEqualTo("Cached Event");

        eventRepository.deleteAll();

        List<EventDTO> secondResult = eventService.getAllEvents();

        assertThat(secondResult.size()).isEqualTo(1);
        assertThat(secondResult.get(0).getTitle()).isEqualTo("Cached Event");
    }








}
