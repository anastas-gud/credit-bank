package ru.gudoshnikova.deal.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Пол")
public enum Gender {
    MALE,
    FEMALE,
    NON_BINARY
}
