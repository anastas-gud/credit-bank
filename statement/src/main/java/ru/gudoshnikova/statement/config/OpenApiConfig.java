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
                                Микросервис для обработки кредитных заявок.
                                
                                ## Функциональность:
                                * **POST /statement** - прескоринг заявки и отправка в Deal микросервис для расчета предложений
                                * **POST /statement/offer** - выбор кредитного предложения и отправка в Deal микросервис
                                
                                ## Логика работы:
                                1. Прескоринг входных данных
                                2. Отправка запроса в Deal микросервис
                                3. Возврат кредитных предложений
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
