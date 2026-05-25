package ru.gudoshnikova.statement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Ответ с ошибкой")
public class ErrorResponseDto {
    @Schema(description = "Время ошибки", example = "2024-03-05T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP статус", example = "400")
    private int status;

    @Schema(description = "Тип ошибки", example = "Validation Failed")
    private String error;

    @Schema(description = "Сообщение об ошибке", example = "Проверьте корректность введенных данных")
    private String message;

    @Schema(description = "Детальные ошибки валидации полей")
    private Map<String, String> validationErrors;
}
