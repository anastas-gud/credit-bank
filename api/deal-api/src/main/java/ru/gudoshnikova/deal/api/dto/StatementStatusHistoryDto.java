package ru.gudoshnikova.deal.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gudoshnikova.deal.api.enums.ApplicationStatus;
import ru.gudoshnikova.deal.api.enums.ChangeType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Элемент истории статусов заявки")
public class StatementStatusHistoryDto {
    @Schema(description = "Статус заявки", example = "PREAPPROVAL")
    @NotNull(message = "Статус обязателен")
    @JsonProperty("status")
    private ApplicationStatus status;

    @Schema(description = "Время изменения статуса", example = "2024-03-20T10:30:00")
    @NotNull(message = "Время изменения статуса обязательно")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("time")
    private LocalDateTime time;

    @Schema(description = "Тип изменения статуса", example = "AUTOMATIC")
    @NotNull(message = "Тип изменения статуса обязателен")
    @JsonProperty("change_type")
    private ChangeType changeType;
}
