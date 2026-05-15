package ru.gudoshnikova.gateway.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус заявки")
public enum ApplicationStatus {
    PREAPPROVAL,
    APPROVED,
    CC_DENIED,
    CC_APPROVED,
    PREPARE_DOCUMENTS,
    DOCUMENT_CREATED,
    CLIENT_DENIED,
    DOCUMENT_SIGNED,
    CREDIT_ISSUED
}
