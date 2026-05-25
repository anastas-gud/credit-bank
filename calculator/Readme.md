# Calculator Microservice

Микросервис для расчета кредитных предложений и полного скоринга клиентов. Сервис предоставляет API для предварительного расчета условий кредита и полного расчета с учетом скоринга.

### Архитектура
Микросервис построен на основе Spring Boot и следует принципам REST API. Основные компоненты:
* Контроллер (CalculatorController) - обработка HTTP запросов
* Сервисы - бизнес-логика:
  * CalculatorService - основной сервис для расчетов
  * PrescoringService - предварительная проверка заявок
  * ScoringService - скоринг клиентов и расчет ставки
* Утилиты - константы и вспомогательные классы

### Архитектурный стек
* Java 21
* Spring Boot 3.5.11
* Maven - сборка проекта
* Lombok - сокращение шаблонного кода
* SpringDoc OpenAPI - документация Swagger
* JUnit 5 - тестирование
* Mockito - мокирование в тестах

### API Endpoints

#### Расчет кредитных предложений
POST /calculator/offers

Тело запроса:

{
"amount": 300000.00,
"term": 12,
"firstName": "Ivan",
"lastName": "Ivanov",
"middleName": "Ivanovich",
"email": "ivan@mail.ru",
"birthdate": "1996-12-23",
"passportSeries": "3756",
"passportNumber": "127539"
}

---

#### Полный расчет кредита
POST /calculator/calc

Тело запроса:

{
"amount": 300000.00,
"term": 12,
"firstName": "Ivan",
"lastName": "Ivanov",
"middleName": "Ivanovich",
"gender": "MALE",
"birthdate": "1990-01-01",
"passportSeries": "1238",
"passportNumber": "567890",
"passportIssueDate": "2010-05-15",
"passportIssueBranch": "770-001",
"maritalStatus": "MARRIED",
"dependentAmount": 2,
"employment": {
"employmentStatus": "BUSINESS_OWNER",
"employerINN": "7701234567",
"salary": 100000,
"position": "WORKER",
"workExperienceTotal": 60,
"workExperienceCurrent": 24
},
"accountNumber": "40817810000012345678",
"isInsuranceEnabled": true,
"isSalaryClient": true
}

### Документация Swagger

* Swagger UI: http://localhost:8080/swagger-ui/index.html
* OpenAPI JSON: http://localhost:8080/v3/api-docs

