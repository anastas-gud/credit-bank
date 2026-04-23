package ru.gudoshnikova.deal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gudoshnikova.deal.enums.Theme;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {
    private String address;
    private Theme theme;
    private UUID statementId;
    private String text;
    private byte[] document;
}
