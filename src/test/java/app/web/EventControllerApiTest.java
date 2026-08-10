package app.web;

import app.model.QuestType;
import app.service.EventService;
import app.web.dto.ActiveEventResponse;
import app.web.dto.CreateEventRequest;
import app.web.dto.EditEventRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ActiveProfiles("test")
@WebMvcTest(EventController.class)
public class EventControllerApiTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private CacheManager cacheManager;

    @Autowired
    private ObjectMapper objectMapper;

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


    @Test
    void createEvent_shouldReturn201_whenRequestIsValid() throws Exception {

        CreateEventRequest request = CreateEventRequest.builder()
                .title("Double XP Weekend")
                .description("Earn extra XP from quests.")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(100)
                .bonusGold(50)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(3))
                .build();

        mockMvc.perform(post("/api/v1/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        verify(eventService).createEvent(any(CreateEventRequest.class));
    }

    @Test
    void createEvent_shouldReturn400_whenRequestIsInvalid() throws Exception {

        CreateEventRequest request = CreateEventRequest.builder()
                .title("")
                .description("")
                .affectedQuestType(null)
                .bonusXp(-10)
                .bonusGold(-5)
                .start(null)
                .end(null)
                .build();

        mockMvc.perform(post("/api/v1/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).createEvent(any(CreateEventRequest.class));
    }

    @Test
    void editEvent_shouldReturn200_whenRequestIsValid() throws Exception {

        UUID eventId = UUID.randomUUID();

        EditEventRequest request = EditEventRequest.builder()
                .id(eventId)
                .title("Updated Event")
                .description("Updated event description.")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(150)
                .bonusGold(75)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(3))
                .build();

        mockMvc.perform(put("/api/v1/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(eventService).editEvent(any(EditEventRequest.class));
    }

    @Test
    void editEvent_shouldReturn400_whenRequestIsInvalid() throws Exception {

        EditEventRequest request = EditEventRequest.builder()
                .id(null)
                .title("")
                .description("")
                .affectedQuestType(null)
                .bonusXp(-10)
                .bonusGold(-5)
                .start(null)
                .end(null)
                .build();

        mockMvc.perform(put("/api/v1/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).editEvent(any(EditEventRequest.class));
    }


}
