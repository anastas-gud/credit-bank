package ru.gudoshnikova.gateway.config;

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

    @Value("server.port:8084")
    private String serverPort;

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .openapi("3.0.0")
                .info(new Info()
                        .title("Gateway Microservice API")
                        .description("""
                                Единая точка входа для всех микросервисов кредитного сервиса.
                                
                                ## Доступные сервисы через Gateway:
                                
                                ### Statement Service (через Gateway)
                                * **POST /gateway/statement** - создание заявки на кредит
                                * **POST /gateway/statement/offer** - выбор кредитного предложения
                                
                                ### Deal Service (через Gateway)
                                * **POST /gateway/deal/calculate/{statementId}** - полный расчет кредита
                                * **POST /gateway/deal/document/{statementId}/send** - формирование документов
                                * **POST /gateway/deal/document/{statementId}/sign** - запрос подписания
                                * **POST /gateway/deal/document/{statementId}/code** - подтверждение кода
                                
                                ### Admin API (через Gateway)
                                * **GET /gateway/deal/admin/statement/{statementId}** - получение заявки по ID
                                * **GET /gateway/deal/admin/statement** - получение всех заявок
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
