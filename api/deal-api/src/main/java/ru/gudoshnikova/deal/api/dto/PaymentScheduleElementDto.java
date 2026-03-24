package ru.gudoshnikova.deal.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Элемент графика платежей")
public class PaymentScheduleElementDto {
    @Schema(description = "Номер платежа", example = "1")
    @JsonProperty("number")
    private Integer number;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Дата платежа", example = "2024-01-15")
    @JsonProperty("date")
    private LocalDate date;

    @Schema(description = "Общая сумма платежа", example = "22500.00")
    @JsonProperty("total_payment")
    private BigDecimal totalPayment;

    @Schema(description = "Сумма процентов в платеже", example = "5000.00")
    @JsonProperty("interest_payment")
    private BigDecimal interestPayment;

    @Schema(description = "Сумма погашения основного долга", example = "17500.00")
    @JsonProperty("debt_payment")
    private BigDecimal debtPayment;

    @Schema(description = "Остаток долга после платежа", example = "482500.00")
    @JsonProperty("remaining_debt")
    private BigDecimal remainingDebt;
}
