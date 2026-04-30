package ru.gudoshnikova.dossier.util;

public final class EmailConstants {

    private EmailConstants() {
    }

    public static final String SUBJECT_FINISH_REGISTRATION = "Завершение регистрации заявки";
    public static final String SUBJECT_CREATE_DOCUMENTS = "Создание документов";
    public static final String SUBJECT_SEND_DOCUMENTS = "Документы для подписания";
    public static final String SUBJECT_SEND_SES = "Код подтверждения";
    public static final String SUBJECT_CREDIT_ISSUED = "Решение по кредиту";
    public static final String SUBJECT_STATEMENT_DENIED = "Заявка отклонена";

    public static final String TEXT_FINISH_REGISTRATION = """
            Уважаемый клиент!
            
            Ваша заявка #%s предварительно одобрена.
            Пожалуйста, завершите регистрацию, заполнив все необходимые данные.
            
            С уважением,
            Кредитный отдел
            """;

    public static final String TEXT_SEND_DOCUMENTS = """
            Уважаемый клиент!
            
            Документы для заявки #%s готовы к подписанию.
            К данному письму прикреплен файл credit_agreement.docx с кредитным договором.
            
            Для подписания документов перейдите по ссылке: [Подписать документы]
            
            С уважением,
            Кредитный отдел
            """;

    public static final String TEXT_SEND_SES = """
            Уважаемый клиент!
            
            Ваш код подтверждения для заявки #%s: %s
            
            Для подтверждения подписания документов введите этот код на сайте.
            
            Внимание! Код действителен в течение 15 минут.
            
            С уважением,
            Кредитный отдел
            """;

    public static final String TEXT_CREDIT_APPROVED = """
            Уважаемый клиент!
            
            Ваша заявка #%s одобрена!
            
            Для формирования документов перейдите по ссылке: [Сформировать документы]
            
            С уважением,
            Кредитный отдел
            """;

    public static final String TEXT_CREDIT_ISSUED = """
            Уважаемый клиент!
            
            Поздравляем! Кредит по заявке #%s успешно выдан.
            
            %s
            
            Спасибо, что выбрали наш банк!
            
            С уважением,
            Кредитный отдел
            """;

    public static final String TEXT_STATEMENT_DENIED = """
            Уважаемый клиент!
            
            К сожалению, ваша заявка #%s была отклонена.
            
            Причина: %s
            
            Вы можете подать новую заявку через 30 дней.
            
            С уважением,
            Кредитный отдел
            """;
}
