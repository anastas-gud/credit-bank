# Deal Microservice

Микросервис для управления кредитными заявками и сделками. Сервис отвечает за создание клиентов, обработку заявок, взаимодействие с калькулятором и сохранение кредитных сделок в базе данных.

## Архитектура

Микросервис построен на основе Spring Boot и следует принципам REST API. Основные компоненты:

- **Контроллер** (`DealController`) - обработка HTTP запросов
- **Сервисы** - бизнес-логика:
    - `DealService` - основной сервис для управления заявками
- **Репозитории** - работа с БД через Spring Data JPA
- **Мапперы** - преобразование DTO в Entity с использованием MapStruct
- **Сущности** - JPA Entity с поддержкой JSONB полей

## Технологический стек

- **Java 21**
- **Spring Boot 3.5.11**
- **Spring Data JPA** - работа с базой данных
- **PostgreSQL 16** - основная база данных
- **MapStruct 1.5.5** - маппинг DTO в Entity
- **Lombok** - сокращение шаблонного кода
- **SpringDoc OpenAPI** - документация Swagger
- **JUnit 5** - тестирование
- **Mockito** - мокирование в тестах
- **Maven** - сборка проекта

## API Endpoints

### 1. Создание заявки на кредит

POST /deal/statement

**Тело запроса:**
```json
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
```

### 2. Выбор кредитного предложения

POST /deal/offer/select

**Тело запроса:**
```json
{
  "statementId": "18737f62-d305-41c9-b99b-097a7240921b",
  "requestedAmount": 300000.00,
  "totalAmount": 309000.00,
  "term": 12,
  "monthlyPayment": 27309.93,
  "rate": 11.0,
  "isInsuranceEnabled": true,
  "isSalaryClient": true
}
```

### 3. Полный расчет кредита

POST /deal/calculate/{statementId}

**Тело запроса:**
```json
{
  "gender": "MALE",
  "maritalStatus": "MARRIED",
  "dependentAmount": 2,
  "passportIssueDate": "2010-05-15",
  "passportIssueBranch": "770-001",
  "employment": {
    "employmentStatus": "UNEMPLOYED",
    "employerINN": "7701234567",
    "salary": 100000,
    "position": "WORKER",
    "workExperienceTotal": 60,
    "workExperienceCurrent": 24
  },
  "accountNumber": "40817810000012345678"
}
```

### Документация Swagger

* Swagger UI: http://localhost:8081/swagger-ui/index.html
* OpenAPI JSON: http://localhost:8081/v3/api-docs