package ru.gudoshnikova.calculator.api.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Должность")
public enum Position {
    WORKER,
    MIDDLE_MANAGER,
    TOP_MANAGER,
    OWNER
}
