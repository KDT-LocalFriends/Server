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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductPessimisticLockTest {

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
            User user = User.builder()
                    .userName("testUser" + i)
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

            users.add(userRepository.save(user));
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

                    System.out.println("[DEBUG] 구매 요청 시작 - userId = " + user.getId());

                    productService.exchangeProduct(user.getId(), product.getId(), 1);

                    successCount.incrementAndGet();
                    System.out.println("[DEBUG] 구매 성공 - userId = " + user.getId());

                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("[DEBUG] 구매 실패 - " + e.getClass().getSimpleName() + " / " + e.getMessage());
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
        Product resultProduct = productRepository.findById(product.getId())
                .orElseThrow();

        long exchangeCount = exchangeRepository.count();

        System.out.println("[DEBUG] 성공 수 = " + successCount.get());
        System.out.println("[DEBUG] 실패 수 = " + failCount.get());
        System.out.println("[DEBUG] 최종 재고 = " + resultProduct.getStock());
        System.out.println("[DEBUG] 교환 내역 수 = " + exchangeCount);

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(requestCount - 1);
        assertThat(resultProduct.getStock()).isEqualTo(0);
        assertThat(exchangeCount).isEqualTo(1);
    }
}
