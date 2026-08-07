package app.web.dto;

import app.model.QuestType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EventDTO {

    private UUID id;
    private String title;
    private String description;
    private QuestType affectedQuestType;
    private Integer bonusXp;
    private Integer bonusGold;
    private LocalDateTime start;
    private LocalDateTime end;

}
