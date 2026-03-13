package ru.gudoshnikova.calculator.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус занятости")
public enum EmploymentStatus {
    EMPLOYED,
    SELF_EMPLOYED,
    UNEMPLOYED,
    BUSINESS_OWNER
}
