package com.backend.kdt.pay.service;

import com.backend.kdt.auth.entity.User;
import com.backend.kdt.auth.repository.UserRepository;
import com.backend.kdt.character.service.CharacterService;
import com.backend.kdt.pay.dto.DonationResponseDto;
import com.backend.kdt.pay.dto.ExchangeResponseDto;
import com.backend.kdt.pay.dto.GameCompletionResponseDto;
import com.backend.kdt.pay.dto.ProductDetailDto;
import com.backend.kdt.pay.dto.ProductDto;
import com.backend.kdt.pay.dto.WatchCompletionResponseDto;
import com.backend.kdt.pay.entity.Product;
import com.backend.kdt.pay.entity.ProductExchange;
import com.backend.kdt.pay.entity.TransactionType;
import com.backend.kdt.pay.repository.ProductExchangeRepository;
import com.backend.kdt.pay.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    // 시청 완료 시 지급할 소모품 개수 (상수)
    private static final int WATCH_COMPLETION_CONSUMPTION = 3;
    // 구매/기부 시 지급할 치장품 개수 (상수)
    private static final int PURCHASE_COSMETIC_REWARD = 1;
    private static final int DONATION_COSMETIC_REWARD = 1;

    // 게임 완료 시 경험치 및 제한 관련 상수
    private static final int GAME_COMPLETION_EXPERIENCE = 50;  // 기본 경험치
    private static final int BONUS_EXPERIENCE_3RD_GAME = 20;   // 3번째 게임 완료 시 추가 경험치
    private static final int DAILY_GAME_LIMIT = 3;             // 하루 최대 게임 횟수

    // 쓰다듬기 관련 상수
    private static final long PET_EXPERIENCE = 20;              // 쓰다듬기 경험치
    private static final int DAILY_PET_LIMIT = 3;              // 하루 최대 쓰다듬기 횟수

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductExchangeRepository exchangeRepository;
    private final CharacterService characterService;

    /**
     * 상품 교환 - PURCHASE 타입으로 자동 처리 + 딸기 헤어핀 지급
     */
    @Transactional
    public void exchangeProduct(Long userId, Long productId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품 없음"));

        if (product.getTransactionType() == TransactionType.DONATION) {
            throw new IllegalArgumentException("기부 상품은 구매할 수 없습니다. 기부 API를 이용해주세요.");
        }

        int totalCost = product.getPointCost() * quantity;

        if (user.getPoint() < totalCost) {
            throw new IllegalArgumentException("포인트 부족");
        }

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("상품 재고 부족");
        }

        // 포인트 차감 & 재고 감소
        user.setPoint(user.getPoint() - totalCost);
        product.setStock(product.getStock() - quantity);

        // 구매 시 딸기 헤어핀 지급 (STRAWBERRY_HAIRPIN)
        user.setStrawberryHairpinCount(user.getStrawberryHairpinCount() + PURCHASE_COSMETIC_REWARD);
        user.setCosmeticCount(user.getCosmeticCount() + PURCHASE_COSMETIC_REWARD);

        // PURCHASE 타입으로 자동 설정
        ProductExchange exchange = ProductExchange.builder()
                .user(user)
                .product(product)
                .quantity(quantity)
                .totalCost(totalCost)
                .transactionType(TransactionType.PURCHASE)
                .exchangedAt(LocalDateTime.now())
                .build();

        try {
            exchangeRepository.save(exchange);
        } catch (OptimisticLockingFailureException e) {
            throw new RuntimeException("상품 교환 중 충돌이 발생했습니다. 다시 시도해주세요.");
        }
    }

    /**
     * 기부하기 - DONATION 타입으로 자동 처리 + 장미 지급
     */
    @Transactional
    public void donateProduct(Long userId, Long productId, int donationAmount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품 없음"));

        if (product.getTransactionType() != TransactionType.DONATION) {
            throw new IllegalArgumentException("일반 상품은 기부할 수 없습니다. 구매 API를 이용해주세요.");
        }

        if (user.getPoint() < donationAmount) {
            throw new IllegalArgumentException("포인트 부족");
        }

        // 포인트 차감
        user.setPoint(user.getPoint() - donationAmount);

        // 기부 시 장미 지급 (ROSE)
        user.setRoseCount(user.getRoseCount() + DONATION_COSMETIC_REWARD);
        user.setCosmeticCount(user.getCosmeticCount() + DONATION_COSMETIC_REWARD);

        // DONATION 타입으로 자동 설정
        ProductExchange exchange = ProductExchange.builder()
                .user(user)
                .product(product)
                .quantity(1) // 기부는 수량 개념이 없으므로 1로 고정
                .totalCost(donationAmount)
                .transactionType(TransactionType.DONATION)
                .exchangedAt(LocalDateTime.now())
                .accepted(true) // 기부는 바로 완료 처리
                .build();

        exchangeRepository.save(exchange);
    }

    /**
     * 상품 교환 응답 DTO 포함
     */
    @Transactional
    public ExchangeResponseDto exchangeProductWithResponse(Long userId, Long productId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품 없음"));

        if (product.getTransactionType() == TransactionType.DONATION) {
            throw new IllegalArgumentException("기부 상품은 구매할 수 없습니다. 기부 API를 이용해주세요.");
        }

        int totalCost = product.getPointCost() * quantity;

        if (user.getPoint() < totalCost) {
            throw new IllegalArgumentException("포인트 부족");
        }

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("상품 재고 부족");
        }

        // 포인트 차감 & 재고 감소 & 딸기 헤어핀 지급
        user.setPoint(user.getPoint() - totalCost);
        product.setStock(product.getStock() - quantity);
        user.setStrawberryHairpinCount(user.getStrawberryHairpinCount() + PURCHASE_COSMETIC_REWARD);
        user.setCosmeticCount(user.getCosmeticCount() + PURCHASE_COSMETIC_REWARD);

        ProductExchange exchange = ProductExchange.builder()
                .user(user)
                .product(product)
                .quantity(quantity)
                .totalCost(totalCost)
                .transactionType(TransactionType.PURCHASE)
                .exchangedAt(LocalDateTime.now())
                .build();

        try {
            exchangeRepository.save(exchange);
        } catch (OptimisticLockingFailureException e) {
            throw new RuntimeException("상품 교환 중 충돌이 발생했습니다. 다시 시도해주세요.");
        }

        return ExchangeResponseDto.builder()
                .userId(userId)
                .productName(product.getName())
                .quantity(quantity)
                .totalCost(totalCost)
                .remainingPoints(user.getPoint())
                .rewardCosmetic(PURCHASE_COSMETIC_REWARD)
                .totalCosmeticItems(user.getCosmeticCount())
                .message(String.format("%s %d개를 구매했습니다! 딸기 헤어핀 %d개를 받았어요. 🍓",
                        product.getName(), quantity, PURCHASE_COSMETIC_REWARD))
                .exchangedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 기부 응답 DTO 포함
     */
    @Transactional
    public DonationResponseDto donateProductWithResponse(Long userId, Long productId, int donationAmount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품 없음"));

        if (product.getTransactionType() != TransactionType.DONATION) {
            throw new IllegalArgumentException("일반 상품은 기부할 수 없습니다. 구매 API를 이용해주세요.");
        }

        if (user.getPoint() < donationAmount) {
            throw new IllegalArgumentException("포인트 부족");
        }

        // 포인트 차감 & 장미 지급
        user.setPoint(user.getPoint() - donationAmount);
        user.setRoseCount(user.getRoseCount() + DONATION_COSMETIC_REWARD);
        user.setCosmeticCount(user.getCosmeticCount() + DONATION_COSMETIC_REWARD);

        ProductExchange exchange = ProductExchange.builder()
                .user(user)
                .product(product)
                .quantity(1)
                .totalCost(donationAmount)
                .transactionType(TransactionType.DONATION)
                .exchangedAt(LocalDateTime.now())
                .accepted(true)
                .build();

        exchangeRepository.save(exchange);

        return DonationResponseDto.builder()
                .userId(userId)
                .donationTarget(product.getName())
                .donationAmount(donationAmount)
                .remainingPoints(user.getPoint())
                .rewardCosmetic(DONATION_COSMETIC_REWARD)
                .totalCosmeticItems(user.getCosmeticCount())
                .message(String.format("%s에 %d 포인트를 기부했습니다! 장미 %d개를 받았어요. 🌹",
                        product.getName(), donationAmount, DONATION_COSMETIC_REWARD))
                .donatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 카테고리별 상품 조회 (TransactionType별)
     */
    public List<ProductDto> getProductsByCategory(TransactionType transactionType) {
        return productRepository.findByTransactionType(transactionType).stream()
                .map(ProductDto::from)
                .toList();
    }

    /**
     * 시청 완료 처리 및 단감 지급
     */
    @Transactional
    public WatchCompletionResponseDto completeWatchingWithResponse(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));

        if (user.getWatched()) {
            throw new IllegalStateException("이미 시청을 완료하였습니다.");
        }

        // 시청 완료 처리 및 단감 3개 지급 (PERSIMMON)
        user.setWatched(true);
        user.setPersimmonCount(user.getPersimmonCount() + WATCH_COMPLETION_CONSUMPTION);
        user.setConsumptionCount(user.getConsumptionCount() + WATCH_COMPLETION_CONSUMPTION);
        userRepository.save(user);

        return WatchCompletionResponseDto.builder()
                .userId(userId)
                .rewardItems(WATCH_COMPLETION_CONSUMPTION)
                .totalConsumptionItems(user.getConsumptionCount())
                .message("시청 완료! 단감 3개가 지급되었습니다. 🍊")
                .completedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 개별 상품 조회
     */
    @Cacheable(value = "product", key = "#productId")
    public ProductDto getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품 없음"));
        return ProductDto.from(product);
    }

    /**
     * 사용자 기부 내역 조회
     */
    public List<ProductDetailDto> getUserDonationHistory(Long userId) {
        return exchangeRepository.findByUserIdAndTransactionTypeOrderByExchangedAtDesc(
                        userId, TransactionType.DONATION).stream()
                .map(ProductDetailDto::from)
                .toList();
    }

    /**
     * 시청 상태 조회
     */
    public boolean isWatchCompleted(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));
        return user.getWatched();
    }
}
