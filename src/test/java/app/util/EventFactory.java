package app.util;

import app.model.Event;
import app.model.QuestType;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class EventFactory {
    public static Event getActiveEvent(){
        return Event.builder()
                .title("Double XP Weekend")
                .description("Earn extra XP from quests.")
                .affectedQuestType(QuestType.COMBAT)
                .bonusXp(100)
                .bonusGold(50)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(1))
                .active(true)
                .build();
    }
}
