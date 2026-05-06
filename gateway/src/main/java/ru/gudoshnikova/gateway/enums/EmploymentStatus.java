package ru.gudoshnikova.gateway.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус занятости")
public enum EmploymentStatus {
    UNEMPLOYED,
    SELF_EMPLOYED,
    EMPLOYED,
    BUSINESS_OWNER
}
