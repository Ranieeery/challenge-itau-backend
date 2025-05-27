package challenge.dev.raniery.itaubackend.service;

import challenge.dev.raniery.itaubackend.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.DoubleSummaryStatistics;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TransactionalService Tests")
public class TransactionalServiceTest {

    private TransactionalService transactionalService;

    @BeforeEach
    void setUp() {
        transactionalService = new TransactionalService();
    }

    @Nested
    @DisplayName("AddTransaction Tests")
    class AddTransactionTests {

        @Test
        @DisplayName("Should add transaction successfully")
        void shouldAddTransactionSuccessfully() {

            Transaction transaction = new Transaction(
                BigDecimal.valueOf(100.50),
                OffsetDateTime.now().minusSeconds(30));

            transactionalService.addTransaction(transaction);

            DoubleSummaryStatistics stats = transactionalService.getStatistics();
            assertEquals(1, stats.getCount());
            assertEquals(100.50, stats.getSum(), 0.001);
        }

        @Test
        @DisplayName("Should add multiple transactions successfully")
        void shouldAddMultipleTransactionsSuccessfully() {

            Transaction transaction1 = new Transaction(
                BigDecimal.valueOf(100.50),
                OffsetDateTime.now().minusSeconds(30));
            Transaction transaction2 = new Transaction(
                BigDecimal.valueOf(200.75),
                OffsetDateTime.now().minusSeconds(20));

            transactionalService.addTransaction(transaction1);
            transactionalService.addTransaction(transaction2);

            DoubleSummaryStatistics stats = transactionalService.getStatistics();
            assertEquals(2, stats.getCount());
            assertEquals(301.25, stats.getSum(), 0.001);
        }

        @Test
        @DisplayName("Should add transaction with zero value")
        void shouldAddTransactionWithZeroValue() {

            Transaction transaction = new Transaction(
                BigDecimal.ZERO,
                OffsetDateTime.now().minusSeconds(30));

            transactionalService.addTransaction(transaction);

            DoubleSummaryStatistics stats = transactionalService.getStatistics();
            assertEquals(1, stats.getCount());
            assertEquals(0.0, stats.getSum(), 0.001);
        }

        @Test
        @DisplayName("Should handle concurrent transaction additions")
        void shouldHandleConcurrentTransactionAdditions() throws InterruptedException {

            int numberOfThreads = 10;
            int transactionsPerThread = 100;
            ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch latch = new CountDownLatch(numberOfThreads);

            for (int i = 0; i < numberOfThreads; i++) {
                executorService.submit(() -> {
                    try {
                        for (int j = 0; j < transactionsPerThread; j++) {
                            Transaction transaction = new Transaction(
                                BigDecimal.valueOf(1.0),
                                OffsetDateTime.now().minusSeconds(30));
                            transactionalService.addTransaction(transaction);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(10, TimeUnit.SECONDS));
            executorService.shutdown();

            DoubleSummaryStatistics stats = transactionalService.getStatistics();
            assertEquals(numberOfThreads * transactionsPerThread, stats.getCount());
            assertEquals(numberOfThreads * transactionsPerThread * 1.0, stats.getSum(), 0.001);
        }
    }

    @Nested
    @DisplayName("ClearTransactions Tests")
    class ClearTransactionsTests {

        @Test
        @DisplayName("Should clear all transactions")
        void shouldClearAllTransactions() {

            Transaction transaction1 = new Transaction(
                BigDecimal.valueOf(100.50),
                OffsetDateTime.now().minusSeconds(30));
            Transaction transaction2 = new Transaction(
                BigDecimal.valueOf(200.75),
                OffsetDateTime.now().minusSeconds(20));

            transactionalService.addTransaction(transaction1);
            transactionalService.addTransaction(transaction2);

            DoubleSummaryStatistics statsBefore = transactionalService.getStatistics();
            assertEquals(2, statsBefore.getCount());

            transactionalService.clearTransactions();

            DoubleSummaryStatistics statsAfter = transactionalService.getStatistics();
            assertEquals(0, statsAfter.getCount());
            assertEquals(0.0, statsAfter.getSum(), 0.001);
        }

        @Test
        @DisplayName("Should handle clearing when no transactions exist")
        void shouldHandleClearingWhenNoTransactionsExist() {

            transactionalService.clearTransactions();

            DoubleSummaryStatistics stats = transactionalService.getStatistics();
            assertEquals(0, stats.getCount());
            assertEquals(0.0, stats.getSum(), 0.001);
        }

        @Test
        @DisplayName("Should handle multiple consecutive clears")
        void shouldHandleMultipleConsecutiveClears() {

            Transaction transaction = new Transaction(
                BigDecimal.valueOf(100.50),
                OffsetDateTime.now().minusSeconds(30));
            transactionalService.addTransaction(transaction);

            transactionalService.clearTransactions();
            transactionalService.clearTransactions();
            transactionalService.clearTransactions();

            DoubleSummaryStatistics stats = transactionalService.getStatistics();
            assertEquals(0, stats.getCount());
        }

        @Test
        @DisplayName("Should handle concurrent clear operations")
        void shouldHandleConcurrentClearOperations() throws InterruptedException {

            for (int i = 0; i < 100; i++) {
                Transaction transaction = new Transaction(
                    BigDecimal.valueOf(1.0),
                    OffsetDateTime.now().minusSeconds(30));
                transactionalService.addTransaction(transaction);
            }

            int numberOfThreads = 5;
            ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch latch = new CountDownLatch(numberOfThreads);

            for (int i = 0; i < numberOfThreads; i++) {
                executorService.submit(() -> {
                    try {
                        transactionalService.clearTransactions();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(10, TimeUnit.SECONDS));
            executorService.shutdown();

            DoubleSummaryStatistics stats = transactionalService.getStatistics();
            assertEquals(0, stats.getCount());
        }
    }

    @Nested
    @DisplayName("GetStatistics Tests")
    class GetStatisticsTests {

        @Test
        @DisplayName("Should return correct statistics for transactions within 60 seconds")
        void shouldReturnCorrectStatisticsForTransactionsWithin60Seconds() {

            OffsetDateTime now = OffsetDateTime.now();
            Transaction transaction1 = new Transaction(BigDecimal.valueOf(100.0), now.minusSeconds(30));
            Transaction transaction2 = new Transaction(BigDecimal.valueOf(200.0), now.minusSeconds(45));
            Transaction transaction3 = new Transaction(BigDecimal.valueOf(50.0), now.minusSeconds(10));

            transactionalService.addTransaction(transaction1);
            transactionalService.addTransaction(transaction2);
            transactionalService.addTransaction(transaction3);

            DoubleSummaryStatistics stats = transactionalService.getStatistics();

            assertEquals(3, stats.getCount());
            assertEquals(350.0, stats.getSum(), 0.001);
            assertEquals(116.66666666666667, stats.getAverage(), 0.001);
            assertEquals(50.0, stats.getMin(), 0.001);
            assertEquals(200.0, stats.getMax(), 0.001);
        }

        @Test
        @DisplayName("Should exclude transactions older than 60 seconds")
        void shouldExcludeTransactionsOlderThan60Seconds() {

            OffsetDateTime now = OffsetDateTime.now();
            Transaction recentTransaction = new Transaction(BigDecimal.valueOf(100.0), now.minusSeconds(30));
            Transaction oldTransaction = new Transaction(BigDecimal.valueOf(200.0), now.minusSeconds(70));

            transactionalService.addTransaction(recentTransaction);
            transactionalService.addTransaction(oldTransaction);

            DoubleSummaryStatistics stats = transactionalService.getStatistics();

            assertEquals(1, stats.getCount());
            assertEquals(100.0, stats.getSum(), 0.001);
            assertEquals(100.0, stats.getAverage(), 0.001);
            assertEquals(100.0, stats.getMin(), 0.001);
            assertEquals(100.0, stats.getMax(), 0.001);
        }

        @Test
        @DisplayName("Should return empty statistics when no transactions in last 60 seconds")
        void shouldReturnEmptyStatisticsWhenNoTransactionsInLast60Seconds() {

            OffsetDateTime now = OffsetDateTime.now();
            Transaction oldTransaction = new Transaction(BigDecimal.valueOf(100.0), now.minusSeconds(70));
            transactionalService.addTransaction(oldTransaction);

            DoubleSummaryStatistics stats = transactionalService.getStatistics();

            assertEquals(0, stats.getCount());
            assertEquals(0.0, stats.getSum(), 0.001);

            assertTrue(Double.isNaN(stats.getAverage()) || stats.getAverage() == 0.0);

            assertTrue(stats.getMin() == Double.POSITIVE_INFINITY || stats.getMin() == 0.0);

            assertTrue(stats.getMax() == Double.NEGATIVE_INFINITY || stats.getMax() == 0.0);
        }

        @Test
        @DisplayName("Should return empty statistics when no transactions exist")
        void shouldReturnEmptyStatisticsWhenNoTransactionsExist() {
            DoubleSummaryStatistics stats = transactionalService.getStatistics();

            assertEquals(0, stats.getCount());
            assertEquals(0.0, stats.getSum(), 0.001);

            assertTrue(Double.isNaN(stats.getAverage()) || stats.getAverage() == 0.0);

            assertTrue(stats.getMin() == Double.POSITIVE_INFINITY || stats.getMin() == 0.0);

            assertTrue(stats.getMax() == Double.NEGATIVE_INFINITY || stats.getMax() == 0.0);
        }

        @Test
        @DisplayName("Should handle transaction exactly 60 seconds old")
        void shouldHandleTransactionExactly60SecondsOld() {

            OffsetDateTime now = OffsetDateTime.now();
            Transaction exactlyOldTransaction = new Transaction(BigDecimal.valueOf(100.0), now.minusSeconds(60));
            Transaction recentTransaction = new Transaction(BigDecimal.valueOf(50.0), now.minusSeconds(30));

            transactionalService.addTransaction(exactlyOldTransaction);
            transactionalService.addTransaction(recentTransaction);

            DoubleSummaryStatistics stats = transactionalService.getStatistics();

            assertEquals(1, stats.getCount());
            assertEquals(50.0, stats.getSum(), 0.001);
        }

        @Test
        @DisplayName("Should handle statistics with zero value transactions")
        void shouldHandleStatisticsWithZeroValueTransactions() {

            OffsetDateTime now = OffsetDateTime.now();
            Transaction zeroTransaction = new Transaction(BigDecimal.ZERO, now.minusSeconds(30));
            Transaction normalTransaction = new Transaction(BigDecimal.valueOf(100.0), now.minusSeconds(20));

            transactionalService.addTransaction(zeroTransaction);
            transactionalService.addTransaction(normalTransaction);

            DoubleSummaryStatistics stats = transactionalService.getStatistics();

            assertEquals(2, stats.getCount());
            assertEquals(100.0, stats.getSum(), 0.001);
            assertEquals(50.0, stats.getAverage(), 0.001);
            assertEquals(0.0, stats.getMin(), 0.001);
            assertEquals(100.0, stats.getMax(), 0.001);
        }

        @Test
        @DisplayName("Should handle statistics with single transaction")
        void shouldHandleStatisticsWithSingleTransaction() {

            OffsetDateTime now = OffsetDateTime.now();
            Transaction transaction = new Transaction(BigDecimal.valueOf(123.45), now.minusSeconds(30));

            transactionalService.addTransaction(transaction);

            DoubleSummaryStatistics stats = transactionalService.getStatistics();

            assertEquals(1, stats.getCount());
            assertEquals(123.45, stats.getSum(), 0.001);
            assertEquals(123.45, stats.getAverage(), 0.001);
            assertEquals(123.45, stats.getMin(), 0.001);
            assertEquals(123.45, stats.getMax(), 0.001);
        }

        @Test
        @DisplayName("Should handle very large numbers in statistics")
        void shouldHandleVeryLargeNumbersInStatistics() {

            OffsetDateTime now = OffsetDateTime.now();
            Transaction largeTransaction1 = new Transaction(BigDecimal.valueOf(999999999.99), now.minusSeconds(30));
            Transaction largeTransaction2 = new Transaction(BigDecimal.valueOf(888888888.88), now.minusSeconds(20));

            transactionalService.addTransaction(largeTransaction1);
            transactionalService.addTransaction(largeTransaction2);

            DoubleSummaryStatistics stats = transactionalService.getStatistics();

            assertEquals(2, stats.getCount());
            assertEquals(1888888888.87, stats.getSum(), 0.01);
            assertEquals(944444444.435, stats.getAverage(), 0.01);
            assertEquals(888888888.88, stats.getMin(), 0.01);
            assertEquals(999999999.99, stats.getMax(), 0.01);
        }

        @Test
        @DisplayName("Should handle very small numbers in statistics")
        void shouldHandleVerySmallNumbersInStatistics() {

            OffsetDateTime now = OffsetDateTime.now();
            Transaction smallTransaction1 = new Transaction(BigDecimal.valueOf(0.01), now.minusSeconds(30));
            Transaction smallTransaction2 = new Transaction(BigDecimal.valueOf(0.02), now.minusSeconds(20));

            transactionalService.addTransaction(smallTransaction1);
            transactionalService.addTransaction(smallTransaction2);

            DoubleSummaryStatistics stats = transactionalService.getStatistics();

            assertEquals(2, stats.getCount());
            assertEquals(0.03, stats.getSum(), 0.001);
            assertEquals(0.015, stats.getAverage(), 0.001);
            assertEquals(0.01, stats.getMin(), 0.001);
            assertEquals(0.02, stats.getMax(), 0.001);
        }

        @Test
        @DisplayName("Should handle concurrent statistics requests")
        void shouldHandleConcurrentStatisticsRequests() throws InterruptedException {

            OffsetDateTime now = OffsetDateTime.now();
            for (int i = 0; i < 100; i++) {
                Transaction transaction = new Transaction(BigDecimal.valueOf(1.0), now.minusSeconds(30));
                transactionalService.addTransaction(transaction);
            }

            int numberOfThreads = 10;
            ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch latch = new CountDownLatch(numberOfThreads);


            for (int i = 0; i < numberOfThreads; i++) {
                executorService.submit(() -> {
                    try {
                        DoubleSummaryStatistics stats = transactionalService.getStatistics();

                        assertEquals(100, stats.getCount());
                        assertEquals(100.0, stats.getSum(), 0.001);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(10, TimeUnit.SECONDS));
            executorService.shutdown();
        }

        @Test
        @DisplayName("Should handle mixed old and new transactions correctly")
        void shouldHandleMixedOldAndNewTransactionsCorrectly() {

            OffsetDateTime now = OffsetDateTime.now();

            transactionalService.addTransaction(new Transaction(BigDecimal.valueOf(100.0), now.minusSeconds(30)));
            transactionalService.addTransaction(new Transaction(BigDecimal.valueOf(200.0), now.minusSeconds(70)));
            transactionalService.addTransaction(new Transaction(BigDecimal.valueOf(50.0), now.minusSeconds(45)));
            transactionalService.addTransaction(new Transaction(BigDecimal.valueOf(300.0), now.minusSeconds(80)));
            transactionalService.addTransaction(new Transaction(BigDecimal.valueOf(75.0), now.minusSeconds(15)));

            DoubleSummaryStatistics stats = transactionalService.getStatistics();

            assertEquals(3, stats.getCount());
            assertEquals(225.0, stats.getSum(), 0.001);
            assertEquals(75.0, stats.getAverage(), 0.001);
            assertEquals(50.0, stats.getMin(), 0.001);
            assertEquals(100.0, stats.getMax(), 0.001);
        }
    }
}
