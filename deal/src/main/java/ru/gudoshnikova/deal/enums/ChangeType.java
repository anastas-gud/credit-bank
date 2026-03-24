package ru.gudoshnikova.deal.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Тип изменения статуса")
public enum ChangeType {
    AUTOMATIC,
    MANUAL
}
