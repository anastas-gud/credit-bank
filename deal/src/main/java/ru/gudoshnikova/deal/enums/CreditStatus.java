package ru.gudoshnikova.deal.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус кредита")
public enum CreditStatus {
    CALCULATED,
    ISSUED
}
