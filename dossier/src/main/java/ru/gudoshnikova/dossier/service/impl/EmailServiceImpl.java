package ru.gudoshnikova.dossier.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.gudoshnikova.dossier.config.EmailConfig;
import ru.gudoshnikova.dossier.dto.EmailMessage;
import ru.gudoshnikova.dossier.enums.Theme;
import ru.gudoshnikova.dossier.exception.EmailSendingException;
import ru.gudoshnikova.dossier.service.EmailService;
import ru.gudoshnikova.dossier.util.EmailConstants;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailConfig emailConfig;

    @Override
    public void sendEmail(EmailMessage emailMessage) {
        log.info("Sending email to: {}, theme: {}, statementId: {}",
                emailMessage.getAddress(), emailMessage.getTheme(), emailMessage.getStatementId());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getFrom());
            helper.setTo(emailMessage.getAddress());
            helper.setSubject(generateSubject(emailMessage.getTheme()));
            helper.setText(generateText(emailMessage), false);

            if (emailMessage.getTheme() == Theme.SEND_DOCUMENTS && emailMessage.getDocument() != null) {
                helper.addAttachment("credit_agreement.docx",
                        new ByteArrayResource(emailMessage.getDocument()),
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                log.info("Document attached for statement: {}", emailMessage.getStatementId());
            }

            mailSender.send(message);
            log.info("Email sent successfully to: {}", emailMessage.getAddress());
        } catch (Exception e) {
            log.error("Failed to send email to: {}, error: {}",
                    emailMessage.getAddress(), e.getMessage(), e);
            throw new EmailSendingException("Failed to send email to: " + emailMessage.getAddress(), e);
        }
    }

    private String generateSubject(Theme theme) {
        return switch (theme) {
            case FINISH_REGISTRATION -> EmailConstants.SUBJECT_FINISH_REGISTRATION;
            case CREATE_DOCUMENTS -> EmailConstants.SUBJECT_CREATE_DOCUMENTS;
            case SEND_DOCUMENTS -> EmailConstants.SUBJECT_SEND_DOCUMENTS;
            case SEND_SES -> EmailConstants.SUBJECT_SEND_SES;
            case CREDIT_ISSUED -> EmailConstants.SUBJECT_CREDIT_ISSUED;
            case STATEMENT_DENIED -> EmailConstants.SUBJECT_STATEMENT_DENIED;
        };
    }

    private String generateText(EmailMessage emailMessage) {
        return switch (emailMessage.getTheme()) {
            case FINISH_REGISTRATION -> String.format(EmailConstants.TEXT_FINISH_REGISTRATION,
                    emailMessage.getStatementId());

            case CREATE_DOCUMENTS -> {
                if (emailMessage.getText().contains("одобрена")) {
                    yield String.format(EmailConstants.TEXT_CREDIT_APPROVED,
                            emailMessage.getStatementId());
                } else {
                    yield String.format(EmailConstants.TEXT_STATEMENT_DENIED,
                            emailMessage.getStatementId(), emailMessage.getText());
                }
            }

            case SEND_DOCUMENTS -> String.format(EmailConstants.TEXT_SEND_DOCUMENTS,
                    emailMessage.getStatementId());

            case SEND_SES -> String.format(EmailConstants.TEXT_SEND_SES,
                    emailMessage.getStatementId(), emailMessage.getText());

            case CREDIT_ISSUED -> String.format(EmailConstants.TEXT_CREDIT_ISSUED,
                    emailMessage.getStatementId(), emailMessage.getText());

            case STATEMENT_DENIED -> String.format(EmailConstants.TEXT_STATEMENT_DENIED,
                    emailMessage.getStatementId(), emailMessage.getText());
        };
    }
}
