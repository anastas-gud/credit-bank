package ru.gudoshnikova.gateway.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Должность")
public enum EmploymentPosition {
    WORKER,
    MIDDLE_MANAGER,
    TOP_MANAGER,
    OWNER
}
