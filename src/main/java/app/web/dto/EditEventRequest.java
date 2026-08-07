package app.web.dto;

import app.model.QuestType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditEventRequest {
    @NotNull
    private UUID id;

    @NotBlank(message = "Event title must not be blank.")
    private String title;

    @NotBlank(message = "Description must not be blank.")
    private String description;

    @NotNull(message = "Quest type must not be null.")
    private QuestType affectedQuestType;

    @NotNull(message = "Bonus XP must not be null.")
    @Min(value = 0, message = "Bonus XP cannot be negative.")
    private Integer bonusXp;

    @NotNull(message = "Bonus gold must not be null.")
    @Min(value = 0, message = "Bonus gold cannot be negative.")
    private Integer bonusGold;

    @NotNull(message = "Start time must not be null.")
    private LocalDateTime start;

    @NotNull(message = "End time must not be null.")
    private LocalDateTime end;
}
