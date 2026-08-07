package app.web.mapper;

import app.model.Event;
import app.web.dto.ActiveEventResponse;
import app.web.dto.EventDTO;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EventMapper {
    public static ActiveEventResponse toActiveEventResponse (Event event){
        return ActiveEventResponse.builder()
                .title(event.getTitle())
                .description(event.getDescription())
                .affectedQuestType(event.getAffectedQuestType())
                .bonusXp(event.getBonusXp())
                .bonusGold(event.getBonusGold())
                .start(event.getStart())
                .end(event.getEnd())
                .build();
    }

    public static EventDTO toEventDTO (Event event){
        return EventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .affectedQuestType(event.getAffectedQuestType())
                .bonusXp(event.getBonusXp())
                .bonusGold(event.getBonusGold())
                .start(event.getStart())
                .end(event.getEnd())
                .build();
    }


}
