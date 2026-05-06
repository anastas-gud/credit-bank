package ru.gudoshnikova.gateway.util;

public final class PathConstants {
    private PathConstants() {
    }

    public static final String STATEMENT_PATH = "/statement";
    public static final String STATEMENT_OFFER_PATH = STATEMENT_PATH + "/offer";
    public static final String DEAL_PATH = "/deal";
    public static final String DEAL_CALCULATE_PATH = DEAL_PATH + "/calculate/{statementId}";
    public static final String DEAL_DOCUMENT_PATH = DEAL_PATH + "/document";
    public static final String DEAL_DOCUMENT_SEND_PATH = DEAL_DOCUMENT_PATH + "/{statementId}/send";
    public static final String DEAL_DOCUMENT_SIGN_PATH = DEAL_DOCUMENT_PATH + "/{statementId}/sign";
    public static final String DEAL_DOCUMENT_CODE_PATH = DEAL_DOCUMENT_PATH + "/{statementId}/code";

    public static final String DEAL_ADMIN_PATH = DEAL_PATH + "/admin";
    public static final String DEAL_ADMIN_STATEMENT = DEAL_ADMIN_PATH + "/statement/{statementId}";
    public static final String DEAL_ADMIN_STATEMENTS = DEAL_ADMIN_PATH + "/statement";
}
