package com.backend.kdt.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    // application.yml의 maximum-pool-size: 1은 그 자체로 모든 요청을 직렬화한다.
    // 그 값을 그대로 두면 락 유무와 무관하게 "커넥션 하나뿐이라서" 순차 처리된 것이 되어
    // 비관적 락이 검증 대상에서 빠져버린다. 실제 락 경합을 재현하려면 풀을 여러 개로 늘려야 한다.
    @DynamicPropertySource
    static void overrideConnectionPool(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> 20);
        registry.add("spring.datasource.hikari.minimum-idle", () -> 10);
        // 동시 요청이 많을 때 커넥션 풀 대기가 락 대기와 겹쳐 타임아웃으로 오탐되는 것을 방지
        registry.add("spring.datasource.hikari.connection-timeout", () -> 60000);
    }
}
