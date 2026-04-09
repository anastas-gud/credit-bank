package ru.gudoshnikova.statement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "statementId cannot be null")
    private UUID statementId;

    @Schema(description = "Запрашиваемая сумма кредита", example = "500000.00")
    @NotNull(message = "requestedAmount cannot be null")
    @DecimalMin(value = "20000.0", message = "The requested amount must be at least 20,000")
    @DecimalMax(value = "15000000", message = "The requested amount cannot exceed 15000000")
    @Digits(integer = 8, fraction = 2,
            message = "The loan amount must have no more than 8 integers and 2 fractional digits.")
    private BigDecimal requestedAmount;

    @Schema(description = "Общая сумма кредита (с учетом страховки)", example = "510000.00")
    @NotNull(message = "totalAmount cannot be null")
    @DecimalMin(value = "20000.0", message = "The total amount must be at least 20,000")
    @DecimalMax(value = "15000000", message = "The total amount cannot exceed 15000000")
    private BigDecimal totalAmount;

    @Schema(description = "Срок кредита в месяцах", example = "24")
    @NotNull(message = "term cannot be null")
    @Min(value = 6, message = "The loan term must be at least 6 months.")
    @Max(value = 360, message = "The loan term cannot exceed 360 months")
    private Integer term;

    @Schema(description = "Ежемесячный платеж", example = "22500.00")
    @NotNull(message = "monthlyPayment cannot be null")
    @DecimalMin(value = "0.01", message = "The monthlyPayment must be positive")
    @DecimalMax(value = "1000000.00", message = "The monthlyPayment cannot exceed 1,000,000")
    private BigDecimal monthlyPayment;

    @Schema(description = "Процентная ставка", example = "12.5")
    @NotNull(message = "rate cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "The rate should be positive")
    @DecimalMax(value = "100.0", message = "The rate cannot exceed 100%")
    private BigDecimal rate;

    @Schema(description = "Флаг наличия страховки", example = "false")
    @NotNull(message = "isInsuranceEnabled cannot be null")
    private Boolean isInsuranceEnabled;

    @Schema(description = "Флаг зарплатного клиента", example = "false")
    @NotNull(message = "isSalaryClient cannot be null")
    private Boolean isSalaryClient;
}
