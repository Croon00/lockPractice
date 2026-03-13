package com.example.lock.concurrency;

import com.example.lock.domain.budget.Budget;
import com.example.lock.domain.department.Department;
import com.example.lock.domain.user.AppUser;
import com.example.lock.domain.user.UserRole;
import com.example.lock.repository.AppUserRepository;
import com.example.lock.repository.ApprovalHistoryRepository;
import com.example.lock.repository.ApprovalRequestRepository;
import com.example.lock.repository.BudgetRepository;
import com.example.lock.repository.DepartmentRepository;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LockConcurrencyTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ApprovalRequestRepository approvalRequestRepository;

    @Autowired
    private ApprovalHistoryRepository approvalHistoryRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        cleanup();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @Test
    void optimisticLock_throwsException_whenStaleBudgetIsSaved() {
        Budget budget = createBudget(1_000L, 1_000L);

        Budget staleBudget1 = transactionTemplate.execute(status ->
                budgetRepository.findById(budget.getId()).orElseThrow());
        Budget staleBudget2 = transactionTemplate.execute(status ->
                budgetRepository.findById(budget.getId()).orElseThrow());

        staleBudget1.updateAmounts(1_000L, 900L);
        transactionTemplate.executeWithoutResult(status -> budgetRepository.saveAndFlush(staleBudget1));

        staleBudget2.updateAmounts(1_000L, 800L);

        assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> transactionTemplate.executeWithoutResult(status -> budgetRepository.saveAndFlush(staleBudget2))
        );
    }

    @Test
    void pessimisticLock_blocksSecondTransaction_untilFirstTransactionCommits() throws Exception {
        Budget budget = createBudget(1_000L, 1_000L);
        CountDownLatch firstLockAcquired = new CountDownLatch(1);
        CountDownLatch releaseFirstTransaction = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Future<?> firstTx = executorService.submit(() -> transactionTemplate.executeWithoutResult(status -> {
                budgetRepository.findForUpdateByDepartmentId(budget.getDepartment().getId()).orElseThrow();
                firstLockAcquired.countDown();
                awaitLatch(releaseFirstTransaction);
            }));

            Future<Long> secondTx = executorService.submit(() -> {
                awaitLatch(firstLockAcquired);
                long start = System.nanoTime();
                transactionTemplate.executeWithoutResult(status ->
                        budgetRepository.findForUpdateByDepartmentId(budget.getDepartment().getId()).orElseThrow());
                return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            });

            Thread.sleep(Duration.ofSeconds(1));
            releaseFirstTransaction.countDown();

            firstTx.get(5, TimeUnit.SECONDS);
            long elapsedMillis = secondTx.get(5, TimeUnit.SECONDS);

            assertTrue(
                    elapsedMillis >= 900,
                    "Expected the second transaction to wait for the pessimistic lock, but it took only "
                            + elapsedMillis + "ms"
            );
        } finally {
            executorService.shutdownNow();
        }
    }

    private Budget createBudget(Long totalAmount, Long remainingAmount) {
        Department department = departmentRepository.save(Department.builder()
                .name("ENGINEERING")
                .build());

        appUserRepository.save(AppUser.builder()
                .employeeNo("E-100")
                .name("requester")
                .role(UserRole.USER)
                .department(department)
                .build());

        return budgetRepository.saveAndFlush(Budget.builder()
                .department(department)
                .totalAmount(totalAmount)
                .remainingAmount(remainingAmount)
                .build());
    }

    private void cleanup() {
        approvalHistoryRepository.deleteAllInBatch();
        approvalRequestRepository.deleteAllInBatch();
        budgetRepository.deleteAllInBatch();
        appUserRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out while waiting for latch");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread was interrupted", exception);
        }
    }
}