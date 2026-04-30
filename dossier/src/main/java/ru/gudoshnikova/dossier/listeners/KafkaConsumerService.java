package ru.gudoshnikova.dossier.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.gudoshnikova.dossier.dto.EmailMessage;
import ru.gudoshnikova.dossier.service.EmailService;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final EmailService emailService;

    @KafkaListener(topics = "finish-registration", groupId = "${spring.kafka.consumer.group-id}")
    public void handleFinishRegistration(EmailMessage emailMessage) {
        log.info("Received finish-registration message for statement: {}", emailMessage.getStatementId());
        emailService.sendEmail(emailMessage);
    }

    @KafkaListener(topics = "create-documents", groupId = "${spring.kafka.consumer.group-id}")
    public void handleCreateDocuments(EmailMessage emailMessage) {
        log.info("Received create-documents message for statement: {}", emailMessage.getStatementId());
        emailService.sendEmail(emailMessage);
    }

    @KafkaListener(topics = "send-documents", groupId = "${spring.kafka.consumer.group-id}")
    public void handleSendDocuments(EmailMessage emailMessage) {
        log.info("Received send-documents message for statement: {}", emailMessage.getStatementId());
        emailService.sendEmail(emailMessage);
    }

    @KafkaListener(topics = "send-ses", groupId = "${spring.kafka.consumer.group-id}")
    public void handleSendSes(EmailMessage emailMessage) {
        log.info("Received send-ses message for statement: {}", emailMessage.getStatementId());
        emailService.sendEmail(emailMessage);
    }

    @KafkaListener(topics = "credit-issued", groupId = "${spring.kafka.consumer.group-id}")
    public void handleCreditIssued(EmailMessage emailMessage) {
        log.info("Received credit-issued message for statement: {}", emailMessage.getStatementId());
        emailService.sendEmail(emailMessage);
    }

    @KafkaListener(topics = "statement-denied", groupId = "${spring.kafka.consumer.group-id}")
    public void handleStatementDenied(EmailMessage emailMessage) {
        log.info("Received statement-denied message for statement: {}", emailMessage.getStatementId());
        emailService.sendEmail(emailMessage);
    }
}
