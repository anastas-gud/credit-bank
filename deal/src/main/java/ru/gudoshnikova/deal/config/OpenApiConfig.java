package ru.gudoshnikova.deal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Value("server.port:8081")
    private String serverPort;

    @Bean
    public OpenAPI calculatorOpenAPI() {
        return new OpenAPI()
                .openapi("3.0.0")
                .info(new Info()
                        .title("Deal Microservice API")
                        .description("""
                                Микросервис для управления кредитными заявками и сделками.
                                
                                ## Функциональность:
                                * **POST /deal/statement** - создание заявки на кредит, сохранение клиента и отправка запроса в калькулятор для получения предложений
                                * **POST /deal/offer/select** - выбор одного из кредитных предложений и обновление статуса заявки
                                * **POST /deal/calculate/{statementId}** - завершение регистрации, полный расчет кредита и сохранение кредитной сделки
                                
                                ## Логика работы:
                                1. При получении заявки создается клиент и заявка в БД
                                2. Отправляется запрос в калькулятор для получения 4 кредитных предложений
                                3. Клиент выбирает подходящее предложение
                                4. После завершения регистрации отправляется запрос в калькулятор для полного расчета
                                5. Сохраняется кредит и обновляется статус заявки
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Gudoshnikova")
                                .email("nastya@list.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local server")
                ));
    }
}
