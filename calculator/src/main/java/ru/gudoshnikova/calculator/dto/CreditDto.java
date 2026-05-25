package ru.gudoshnikova.calculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Информация о кредите")
public class CreditDto {
    @Schema(description = "Сумма кредита", example = "500000.00")
    private BigDecimal amount;

    @Schema(description = "Срок кредита в месяцах", example = "24")
    private Integer term;

    @Schema(description = "Ежемесячный платеж", example = "22500.00")
    private BigDecimal monthlyPayment;

    @Schema(description = "Процентная ставка", example = "12.5")
    private BigDecimal rate;

    @Schema(description = "Полная стоимость кредита", example = "15.2")
    private BigDecimal psk;

    @Schema(description = "Флаг наличия страховки", example = "true")
    private Boolean isInsuranceEnabled;

    @Schema(description = "Флаг зарплатного клиента", example = "true")
    private Boolean isSalaryClient;

    @Schema(description = "График ежемесячных платежей")
    private List<PaymentScheduleElementDto> paymentSchedule;
}
