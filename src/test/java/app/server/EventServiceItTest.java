package app.server;

import app.model.Event;
import app.model.QuestType;
import app.repository.EventRepository;
import app.service.EventService;
import app.web.dto.ActiveEventResponse;
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

import static app.util.EventFactory.getActiveEvent;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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



}
