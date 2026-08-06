package app.web.mapper;

import app.model.Event;
import app.web.dto.ActiveEventResponse;
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
}
