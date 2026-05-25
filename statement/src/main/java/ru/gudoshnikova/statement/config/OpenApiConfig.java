package ru.gudoshnikova.statement.config;

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

    @Value("server.port:8082")
    private String serverPort;

    @Bean
    public OpenAPI statementOpenAPI() {
        return new OpenAPI()
                .openapi("3.0.0")
                .info(new Info()
                        .title("Statement Microservice API")
                        .description("""
                                Микросервис для отправки email уведомлений клиентам.
                                
                                ## Функциональность:
                                * **POST /dossier/email** - отправка email сообщения
                                
                                ## Топики Kafka:
                                * finish-registration - завершение регистрации
                                * create-documents - создание документов
                                * send-documents - отправка документов
                                * send-ses - отправка SES кода
                                * credit-issued - кредит выдан
                                * statement-denied - заявка отклонена
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
