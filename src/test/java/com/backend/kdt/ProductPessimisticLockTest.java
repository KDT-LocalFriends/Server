package com.backend.kdt;

import com.backend.kdt.auth.entity.Age;
import com.backend.kdt.auth.entity.Gender;
import com.backend.kdt.auth.entity.User;
import com.backend.kdt.auth.repository.UserRepository;
import com.backend.kdt.pay.entity.Product;
import com.backend.kdt.pay.entity.TransactionType;
import com.backend.kdt.pay.repository.ProductExchangeRepository;
import com.backend.kdt.pay.repository.ProductRepository;
import com.backend.kdt.pay.service.ProductService;
import com.backend.kdt.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PessimisticLockingFailureException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ProductPessimisticLockTest extends AbstractIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductExchangeRepository exchangeRepository;

    @AfterEach
    void tearDown() {
        exchangeRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("비관적 락 적용 시 재고 1개 상품에 동시 구매 요청이 들어와도 1명만 성공한다")
    void pessimisticLockTest() throws InterruptedException {
        // given
        Product product = Product.builder()
                .name("딸기 헤어핀")
                .pointCost(100)
                .stock(1)
                .transactionType(TransactionType.PURCHASE)
                .imageUrl("test-image-url")
                .build();

        productRepository.save(product);

        int requestCount = 10;

        List<User> users = new ArrayList<>();
        for (int i = 0; i < requestCount; i++) {
            users.add(userRepository.save(newUser("testUser" + i)));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);

        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(requestCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (User user : users) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    productService.exchangeProduct(user.getId(), product.getId(), 1);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // then
        Product resultProduct = productRepository.findById(product.getId()).orElseThrow();
        long exchangeCount = exchangeRepository.count();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(requestCount - 1);
        assertThat(resultProduct.getStock()).isEqualTo(0);
        assertThat(exchangeCount).isEqualTo(1);
    }

    @Test
    @DisplayName("재고 100개에 동시 1,000건 요청 — 정확히 100건 성공, 락 충돌로 인한 재시도 0건, 전 요청 처리 완료")
    void 비관적_락_동시성_검증() throws InterruptedException {
        // given
        int stock = 100;
        int requestCount = 1_000;

        Product product = productRepository.save(Product.builder()
                .name("딸기 헤어핀")
                .pointCost(100)
                .stock(stock)
                .transactionType(TransactionType.PURCHASE)
                .imageUrl("test-image-url")
                .build());

        List<User> users = new ArrayList<>();
        for (int i = 0; i < requestCount; i++) {
            users.add(userRepository.save(newUser("lockUser" + i)));
        }

        // 스레드 1,000개를 그대로 띄우되, 실제 락 경합은 DB 커넥션 풀(20)과 상품 row 락에서 발생한다
        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);

        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(requestCount);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger outOfStock = new AtomicInteger();
        AtomicInteger lockConflict = new AtomicInteger();
        AtomicInteger unexpected = new AtomicInteger();

        // when
        long startTime = System.currentTimeMillis();

        for (User user : users) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    productService.exchangeProduct(user.getId(), product.getId(), 1);
                    success.incrementAndGet();
                } catch (PessimisticLockingFailureException e) {
                    // 낙관적 락이었다면 여기(충돌/재시도)에 쌓였을 자리 - 비관적 락은 대기 후 순차 처리하므로 0이어야 한다
                    lockConflict.incrementAndGet();
                } catch (IllegalArgumentException e) {
                    // "상품 재고 부족" - 정상적인 재고 소진 실패
                    outOfStock.incrementAndGet();
                } catch (Exception e) {
                    unexpected.incrementAndGet();
                    System.out.println("[UNEXPECTED] " + e.getClass().getSimpleName() + " / " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        long elapsedMs = System.currentTimeMillis() - startTime;
        System.out.println("[DEBUG] 동시 " + requestCount + "건 처리 소요 시간 = " + elapsedMs + "ms");

        // then
        Product resultProduct = productRepository.findById(product.getId()).orElseThrow();
        long exchangeCount = exchangeRepository.count();

        assertThat(success.get()).isEqualTo(stock);
        assertThat(lockConflict.get()).isZero();
        assertThat(unexpected.get()).isZero();
        assertThat(success.get() + outOfStock.get()).isEqualTo(requestCount);
        assertThat(resultProduct.getStock()).isZero();
        assertThat(exchangeCount).isEqualTo(stock);
    }

    private User newUser(String userName) {
        return User.builder()
                .userName(userName)
                .password("1234")
                .point(1000L)
                .age(Age.TEENS_20S)
                .gender(Gender.MALE)
                .watched(false)
                .dailyFeedCount(0)
                .dailyGameCount(0)
                .dailyPetCount(0)
                .consumptionCount(0)
                .cosmeticCount(0)
                .strawberryHairpinCount(0)
                .roseCount(0)
                .persimmonCount(0)
                .greenTeaCount(0)
                .gongbangAhjimaCount(0)
                .carCrownCount(0)
                .build();
    }
}
