package ru.gudoshnikova.deal.service;

import java.util.UUID;

public interface DocumentGeneratorService {
    byte[] generateCreditDocument(UUID statementId, String clientName,
                                  Double amount, Integer term, Double rate);
}
