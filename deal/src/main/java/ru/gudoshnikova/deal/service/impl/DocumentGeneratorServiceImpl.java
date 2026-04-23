package ru.gudoshnikova.deal.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import ru.gudoshnikova.deal.exception.DocumentGenerationException;
import ru.gudoshnikova.deal.service.DocumentGeneratorService;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class DocumentGeneratorServiceImpl implements DocumentGeneratorService {
    @Override
    public byte[] generateCreditDocument(UUID statementId, String clientName, Double amount, Integer term, Double rate) {
        log.info("Generating credit document for statement: {}", statementId);

        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setText("КРЕДИТНЫЙ ДОГОВОР");
            titleRun.addBreak();

            XWPFParagraph number = document.createParagraph();
            number.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun numberRun = number.createRun();
            numberRun.setText("№ " + statementId.toString().substring(0, 8) + "/" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            numberRun.addBreak();
            numberRun.addBreak();

            XWPFParagraph date = document.createParagraph();
            XWPFRun dateRun = date.createRun();
            dateRun.setText("г. Москва, " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            dateRun.addBreak();
            dateRun.addBreak();

            XWPFParagraph parties = document.createParagraph();
            XWPFRun partiesRun = parties.createRun();
            partiesRun.setBold(true);
            partiesRun.setText("СТОРОНЫ ДОГОВОРА:");
            partiesRun.addBreak();
            partiesRun.addBreak();

            partiesRun.setText("1. Банк: ООО \"Кредитный Банк\"");
            partiesRun.addBreak();
            partiesRun.setText("2. Заемщик: " + clientName);
            partiesRun.addBreak();
            partiesRun.addBreak();

            XWPFParagraph subject = document.createParagraph();
            XWPFRun subjectRun = subject.createRun();
            subjectRun.setBold(true);
            subjectRun.setText("ПРЕДМЕТ ДОГОВОРА:");
            subjectRun.addBreak();
            subjectRun.addBreak();

            subjectRun.setText("Банк предоставляет Заемщику кредит на следующих условиях:");
            subjectRun.addBreak();
            subjectRun.addBreak();

            XWPFParagraph terms = document.createParagraph();
            XWPFRun termsRun = terms.createRun();
            termsRun.setText("- Сумма кредита: " + String.format("%.2f", amount) + " руб.");
            termsRun.addBreak();
            termsRun.setText("- Срок кредита: " + term + " месяцев");
            termsRun.addBreak();
            termsRun.setText("- Процентная ставка: " + String.format("%.2f", rate) + "% годовых");
            termsRun.addBreak();
            termsRun.setText("- Валюта кредита: Российский рубль");
            termsRun.addBreak();
            termsRun.addBreak();

            XWPFParagraph signatures = document.createParagraph();
            XWPFRun signaturesRun = signatures.createRun();
            signaturesRun.setBold(true);
            signaturesRun.setText("ПОДПИСИ СТОРОН:");
            signaturesRun.addBreak();
            signaturesRun.addBreak();
            signaturesRun.addBreak();

            signaturesRun.setText("_______________ (Банк)");
            signaturesRun.addBreak();
            signaturesRun.setText("_______________ (Заемщик)");

            document.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate credit document: {}", e.getMessage(), e);
            throw new DocumentGenerationException("Failed to generate credit document for statement: " + statementId, e);
        }
    }
}
