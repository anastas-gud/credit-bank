package ru.gudoshnikova.deal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gudoshnikova.deal.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO заявки для админских API")
public class StatementDto {

    @Schema(description = "ID заявки", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID statementId;

    @Schema(description = "ID клиента", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID clientId;

    @Schema(description = "ID кредита", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID creditId;

    @Schema(description = "Статус заявки", example = "APPROVED")
    private ApplicationStatus status;

    @Schema(description = "Дата создания", example = "2024-05-04T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime creationDate;

    @Schema(description = "Выбранное предложение")
    private LoanOfferDto appliedOffer;

    @Schema(description = "Дата подписания", example = "2024-05-04T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime signDate;

    @Schema(description = "Код подтверждения", example = "123456")
    private String sesCode;

    @Schema(description = "История статусов")
    private List<StatementStatusHistoryDto> statusHistory;
}
