package com.backend.kdt.shop.service;

import com.backend.kdt.auth.entity.User;
import com.backend.kdt.auth.repository.UserRepository;
import com.backend.kdt.pay.entity.ItemType;
import com.backend.kdt.shop.dto.ItemPurchaseResponseDto;
import com.backend.kdt.shop.dto.ItemTypeDto;
import com.backend.kdt.shop.entity.ShopItemType;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final UserRepository userRepository;

    /**
     * 아이템 구매 (수량 고정 1개)
     */
    @Transactional
    public ItemPurchaseResponseDto purchaseItem(Long userId, ShopItemType itemType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

        int itemPrice = itemType.getPrice();
        int quantity = 1; // 무조건 1개 고정

        // 포인트 부족 체크
        if (user.getPoint() < itemPrice) {
            throw new IllegalArgumentException("포인트가 부족합니다. 필요 포인트: " + itemPrice);
        }

        // 포인트 차감
        user.setPoint(user.getPoint() - itemPrice);

        // 아이템 지급 (각 아이템별로 분기)
        switch (itemType) {
            case PERSIMMON:
                user.setPersimmonCount(user.getPersimmonCount() + 1);
                break;
            case GREEN_TEA:
                user.setGreenTeaCount(user.getGreenTeaCount() + 1);
                break;
            case STRAWBERRY_HAIRPIN:
                user.setStrawberryHairpinCount(user.getStrawberryHairpinCount() + 1);
                break;
            case GONGBANG_AHJIMA:
                user.setGongbangAhjimaCount(user.getGongbangAhjimaCount() + 1);
                break;
            case CAR_CROWN:
                user.setCarCrownCount(user.getCarCrownCount() + 1);
                break;
            case ROSE:
                user.setRoseCount(user.getRoseCount() + 1);
                break;
        }

        userRepository.save(user);

        return ItemPurchaseResponseDto.builder()
                .userId(userId)
                .itemName(itemType.getDisplayName())
                .emoji(itemType.getEmoji())
                .itemPrice(itemPrice)
                .quantity(quantity)
                .totalCost(itemPrice)
                .remainingPoints(user.getPoint().intValue())
                .message(String.format("%s %s 1개를 구매했습니다!",
                        itemType.getEmoji(), itemType.getDisplayName()))
                .purchasedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 전체 아이템 목록 조회
     */
    public List<ItemTypeDto> getAllItems() {
        return Arrays.stream(ShopItemType.values())
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 아이템 목록 조회
     */
    public List<ItemTypeDto> getItemsByCategory(ItemType category) {
        return Arrays.stream(ShopItemType.values())
                .filter(item -> item.getCategory() == category)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 꾸미기 아이템만 조회
     */
    public List<ItemTypeDto> getConsumptionItems() {
        return getItemsByCategory(ItemType.CONSUMPTION);
    }

    /**
     * 성장 아이템만 조회
     */
    public List<ItemTypeDto> getCosmeticItems() {
        return getItemsByCategory(ItemType.COSMETIC);
    }

    private ItemTypeDto convertToDto(ShopItemType itemType) {
        return ItemTypeDto.builder()
                .type(itemType)
                .displayName(itemType.getDisplayName())
                .emoji(itemType.getEmoji())
                .price(itemType.getPrice())
                .category(itemType.getCategory())
                .categoryName(itemType.getCategory().getDisplayName())
                .build();
    }
}
