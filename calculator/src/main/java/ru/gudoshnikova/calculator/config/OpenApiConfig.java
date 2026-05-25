package ru.gudoshnikova.calculator.config;

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
    @Value("server.port:8080")
    private String serverPort;

    @Bean
    public OpenAPI calculatorOpenAPI() {
        return new OpenAPI()
                .openapi("3.0.0")
                .info(new Info()
                        .title("Calculator Microservice API")
                        .description("""
                                Микросервис для расчета кредитных предложений и кредита.
                                
                                ## Функциональность:
                                * **/calculator/offers** - расчет 4 кредитных предложений с разными комбинациями страховки и зарплатного клиента
                                * **/calculator/calc** - полный расчет кредита со скорингом данных
                                
                                ## Логика работы:
                                1. Прескоринг входных данных
                                2. Генерация предложений с разными условиями
                                3. Скоринг клиента с корректировкой ставки
                                4. Расчет графика платежей и ПСК
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
