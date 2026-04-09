package ru.gudoshnikova.deal.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
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
    }

    @Autowired
    private DealServiceImpl dealService;

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private UUID statementId;
    private LoanOfferDto offer1;
    private LoanOfferDto offer2;

    @BeforeEach
    void setUp() {
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
                firstCallStartTime.set(System.nanoTime());
                System.out.println("[" + Thread.currentThread().getName() + "] Первый вызов: начало");

                dealService.selectOffer(offer1);

                firstCallEndTime.set(System.nanoTime());
                System.out.println("[" + Thread.currentThread().getName() + "] Первый вызов: завершен");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bothCompleted.countDown();
            }
        });

        Thread.sleep(100);

        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            try {
                secondCallStartTime.set(System.nanoTime());
                System.out.println("[" + Thread.currentThread().getName() + "] Второй вызов: начало (ждет блокировку)");

                dealService.selectOffer(offer2);

                secondCallEndTime.set(System.nanoTime());
                System.out.println("[" + Thread.currentThread().getName() + "] Второй вызов: завершен");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bothCompleted.countDown();
            }
        });

        boolean completed = bothCompleted.await(15, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        long firstCallDuration = TimeUnit.NANOSECONDS.toMillis(firstCallEndTime.get() - firstCallStartTime.get());
        long secondCallDuration = TimeUnit.NANOSECONDS.toMillis(secondCallEndTime.get() - secondCallStartTime.get());

        System.out.println("Первый вызов длился: " + firstCallDuration + " ms");
        System.out.println("Второй вызов длился: " + secondCallDuration + " ms");
        System.out.println("Второй вызов начался позже первого на: " +
                TimeUnit.NANOSECONDS.toMillis(secondCallStartTime.get() - firstCallStartTime.get()) + " ms");

        assertThat(secondCallStartTime.get())
                .isGreaterThan(firstCallStartTime.get());

        assertThat(secondCallEndTime.get())
                .isGreaterThan(firstCallEndTime.get());

        assertThat(secondCallEndTime.get())
                .isGreaterThanOrEqualTo(firstCallEndTime.get());

        assertThat(future1.isCompletedExceptionally()).isFalse();
        assertThat(future2.isCompletedExceptionally()).isFalse();

        Statement updatedStatement = statementRepository.findById(statementId).orElseThrow();
        assertThat(updatedStatement.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(updatedStatement.getAppliedOffer()).isNotNull();
    }

    @Test
    void shouldWaitForLock() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);

        executor.submit(() -> {
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.execute(status -> {
                statementRepository.findByIdWithLock(statementId).orElseThrow();

                lockAcquired.countDown();

                try {
                    releaseLock.await();
                } catch (InterruptedException ignored) {
                }

                return null;
            });
        });

        lockAcquired.await();

        Future<?> secondCall = executor.submit(() -> {
            dealService.selectOffer(offer2);
        });

        Thread.sleep(1000);

        assertThat(secondCall.isDone()).isFalse();

        releaseLock.countDown();

        secondCall.get(5, TimeUnit.SECONDS);

        Statement updated = statementRepository.findById(statementId).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(updated.getAppliedOffer()).isNotNull();

        executor.shutdown();
    }
}