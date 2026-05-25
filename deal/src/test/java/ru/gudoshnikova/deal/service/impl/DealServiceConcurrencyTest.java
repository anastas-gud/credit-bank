package ru.gudoshnikova.deal.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.gudoshnikova.deal.dto.EmailMessage;
import ru.gudoshnikova.deal.dto.LoanOfferDto;
import ru.gudoshnikova.deal.enums.ApplicationStatus;
import ru.gudoshnikova.deal.model.Client;
import ru.gudoshnikova.deal.model.Statement;
import ru.gudoshnikova.deal.repository.ClientRepository;
import ru.gudoshnikova.deal.repository.StatementRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
class DealServiceConcurrencyTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9093");
    }

    @Autowired
    private DealServiceImpl dealService;

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private KafkaTemplate<String, EmailMessage> kafkaTemplate;

    private UUID statementId;
    private LoanOfferDto offer1;
    private LoanOfferDto offer2;

    @BeforeEach
    void setUp() {
        when(kafkaTemplate.send(anyString(), any(EmailMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        Client client = Client.builder()
                .firstName("Test")
                .lastName("Client")
                .email("test@example.com")
                .birthdate(LocalDate.of(1990, 1, 1))
                .build();
        Client savedClient = clientRepository.save(client);

        Statement statement = Statement.builder()
                .client(savedClient)
                .status(ApplicationStatus.PREAPPROVAL)
                .creationDate(LocalDateTime.now())
                .statusHistory(new ArrayList<>())
                .build();

        Statement savedStatement = statementRepository.save(statement);
        statementId = savedStatement.getStatementId();

        offer1 = LoanOfferDto.builder()
                .statementId(statementId)
                .requestedAmount(BigDecimal.valueOf(300000))
                .totalAmount(BigDecimal.valueOf(300000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(26500))
                .rate(BigDecimal.valueOf(12.0))
                .isInsuranceEnabled(false)
                .isSalaryClient(false)
                .build();

        offer2 = LoanOfferDto.builder()
                .statementId(statementId)
                .requestedAmount(BigDecimal.valueOf(300000))
                .totalAmount(BigDecimal.valueOf(309000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(27300))
                .rate(BigDecimal.valueOf(11.5))
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .build();
    }

    @Test
    void testSelectOfferWithLock_ProvesWaiting() throws Exception {
        AtomicLong firstCallStartTime = new AtomicLong();
        AtomicLong firstCallEndTime = new AtomicLong();
        AtomicLong secondCallStartTime = new AtomicLong();
        AtomicLong secondCallEndTime = new AtomicLong();

        CountDownLatch bothCompleted = new CountDownLatch(2);

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            try {
                firstCallStartTime.set(System.currentTimeMillis());
                System.out.println("[" + firstCallStartTime.get() + "] [ПОТОК-1] Первый вызов: начало");

                dealService.selectOffer(offer1);

                firstCallEndTime.set(System.currentTimeMillis());
                System.out.println("[" + firstCallEndTime.get() + "] [ПОТОК-1] Первый вызов: завершен");
            } catch (Exception e) {
                System.err.println("[ПОТОК-1] Ошибка: " + e.getMessage());
                e.printStackTrace();
            } finally {
                bothCompleted.countDown();
            }
        });

        Thread.sleep(100);

        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            try {
                secondCallStartTime.set(System.currentTimeMillis());
                System.out.println("[" + secondCallStartTime.get() + "] [ПОТОК-2] Второй вызов: начало (ждет блокировку)");

                dealService.selectOffer(offer2);

                secondCallEndTime.set(System.currentTimeMillis());
                System.out.println("[" + secondCallEndTime.get() + "] [ПОТОК-2] Второй вызов: завершен");
            } catch (Exception e) {
                System.err.println("[ПОТОК-2] Ошибка: " + e.getMessage());
                e.printStackTrace();
            } finally {
                bothCompleted.countDown();
            }
        });

        boolean completed = bothCompleted.await(15, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        long firstCallDuration = firstCallEndTime.get() - firstCallStartTime.get();
        long secondCallDuration = secondCallEndTime.get() - secondCallStartTime.get();

        System.out.println("Первый вызов длился: " + firstCallDuration + " ms");
        System.out.println("Второй вызов длился: " + secondCallDuration + " ms");
        System.out.println("Второй вызов начался позже первого на: " +
                (secondCallStartTime.get() - firstCallStartTime.get()) + " ms");

        assertThat(secondCallEndTime.get())
                .isGreaterThan(firstCallEndTime.get());

        assertThat(future1.isCompletedExceptionally()).isFalse();
        assertThat(future2.isCompletedExceptionally()).isFalse();

        Statement updatedStatement = statementRepository.findById(statementId).orElseThrow();
        assertThat(updatedStatement.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(updatedStatement.getAppliedOffer()).isNotNull();
    }

    @Test
    void shouldWaitForLock() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch firstCallStarted = new CountDownLatch(1);
        CountDownLatch firstCallCompleted = new CountDownLatch(1);

        Future<?> firstCall = executor.submit(() -> {
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.execute(status -> {
                try {
                    System.out.println("[" + System.currentTimeMillis() + "] [ПОТОК-1] Захватываю блокировку...");
                    firstCallStarted.countDown();

                    Statement statement = statementRepository.findByIdWithLock(statementId).orElseThrow();

                    Thread.sleep(3000);

                    statement.setStatus(ApplicationStatus.APPROVED);
                    statement.setAppliedOffer(offer1);
                    statementRepository.save(statement);

                    System.out.println("[" + System.currentTimeMillis() + "] [ПОТОК-1] Освобождаю блокировку");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });
            firstCallCompleted.countDown();
            return null;
        });

        firstCallStarted.await();
        System.out.println("[" + System.currentTimeMillis() + "] [ПОТОК-1] Блокировка захвачена");

        Future<?> secondCall = executor.submit(() -> {
            System.out.println("[" + System.currentTimeMillis() + "] [ПОТОК-2] Пытаюсь захватить блокировку (жду)...");

            dealService.selectOffer(offer2);

            System.out.println("[" + System.currentTimeMillis() + "] [ПОТОК-2] Блокировка захвачена и выполнена");
            return null;
        });

        Thread.sleep(1000);
        assertThat(secondCall.isDone()).isFalse();

        firstCallCompleted.await();

        secondCall.get(5, TimeUnit.SECONDS);

        Statement updatedStatement = statementRepository.findById(statementId).orElseThrow();
        assertThat(updatedStatement.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(updatedStatement.getAppliedOffer()).isNotNull();

        executor.shutdown();
    }
}