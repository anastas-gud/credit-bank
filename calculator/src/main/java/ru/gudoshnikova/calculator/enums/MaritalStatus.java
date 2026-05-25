package ru.gudoshnikova.calculator.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Семейное положение")
public enum MaritalStatus {
    SINGLE,
    MARRIED,
    DIVORCED,
    WIDOW_WIDOWER
}
