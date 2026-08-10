package app.web;

import app.model.QuestType;
import app.service.EventService;
import app.web.dto.ActiveEventResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(EventController.class)
public class EventControllerApiTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    void getActiveEvent_shouldReturnEvent_whenActiveEventExists() throws Exception {

        ActiveEventResponse event = ActiveEventResponse.builder()
                .title("Double XP Weekend")
                .description("All quests give bonus XP.")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(50)
                .bonusGold(25)
                .start(LocalDateTime.of(2026, 8, 10, 12, 0))
                .end(LocalDateTime.of(2026, 8, 12, 12, 0))
                .build();

        when(eventService.getActiveEvent()).thenReturn(event);

        mockMvc.perform(get("/api/v1/event"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Double XP Weekend"))
                .andExpect(jsonPath("$.description").value("All quests give bonus XP."))
                .andExpect(jsonPath("$.affectedQuestType").value("COMBAT"))
                .andExpect(jsonPath("$.bonusXp").value(50))
                .andExpect(jsonPath("$.bonusGold").value(25))
                .andExpect(jsonPath("$.start").value("2026-08-10T12:00:00"))
                .andExpect(jsonPath("$.end").value("2026-08-12T12:00:00"));

        verify(eventService).getActiveEvent();
    }

    @Test
    void getActiveEvent_shouldReturnEmptyBody_whenNoActiveEventExists() throws Exception {

        when(eventService.getActiveEvent()).thenReturn(null);

        mockMvc.perform(get("/api/v1/event"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(eventService).getActiveEvent();
    }

}
