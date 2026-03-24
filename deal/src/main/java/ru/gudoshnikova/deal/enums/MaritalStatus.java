package ru.gudoshnikova.deal.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Семейное положение")
public enum MaritalStatus {
    MARRIED,
    DIVORCED,
    SINGLE,
    WIDOW_WIDOWER
}
