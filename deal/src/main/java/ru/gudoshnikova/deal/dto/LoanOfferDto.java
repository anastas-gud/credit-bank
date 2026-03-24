package ru.gudoshnikova.deal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Кредитное предложение")
public class LoanOfferDto {
    @Schema(description = "UUID заявки", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID statementId;

    @Schema(description = "Запрашиваемая сумма кредита", example = "500000.00")
    private BigDecimal requestedAmount;

    @Schema(description = "Общая сумма кредита (с учетом страховки)", example = "510000.00")
    private BigDecimal totalAmount;

    @Schema(description = "Срок кредита в месяцах", example = "24")
    private Integer term;

    @Schema(description = "Ежемесячный платеж", example = "22500.00")
    private BigDecimal monthlyPayment;

    @Schema(description = "Процентная ставка", example = "12.5")
    private BigDecimal rate;

    @Schema(description = "Флаг наличия страховки", example = "false")
    private Boolean isInsuranceEnabled;

    @Schema(description = "Флаг зарплатного клиента", example = "false")
    private Boolean isSalaryClient;
}
