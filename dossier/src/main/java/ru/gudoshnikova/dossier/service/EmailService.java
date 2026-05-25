package ru.gudoshnikova.dossier.service;

import ru.gudoshnikova.dossier.dto.EmailMessage;

public interface EmailService {
    void sendEmail(EmailMessage emailMessage);
}
