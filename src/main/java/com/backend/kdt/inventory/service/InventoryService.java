package com.backend.kdt.inventory.service;

import com.backend.kdt.auth.entity.User;
import com.backend.kdt.auth.repository.UserRepository;
import com.backend.kdt.character.entity.Character;
import com.backend.kdt.character.repository.CharacterRepository;
import com.backend.kdt.inventory.dto.ConsumptionInventoryDto;
import com.backend.kdt.inventory.dto.CosmeticInventoryDto;
import com.backend.kdt.inventory.dto.InventoryDto;
import com.backend.kdt.inventory.dto.InventorySummaryDto;
import com.backend.kdt.inventory.dto.ConsumptionItemDto;
import com.backend.kdt.inventory.dto.CosmeticItemDto;
import com.backend.kdt.pay.entity.ItemType;
import com.backend.kdt.shop.entity.ShopItemType;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;

    /**
     * 사용자 전체 인벤토리 조회
     */
    public InventoryDto getUserInventory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

        return InventoryDto.builder()
                .userId(userId)
                .userName(user.getUserName())
                .consumption(getConsumptionItems(userId))
                .cosmetic(getCosmeticItems(userId))
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * 소모품만 조회 (개수가 1개 이상인 것만 표시)
     */
    public ConsumptionInventoryDto getConsumptionItems(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

        // 소모품 아이템들 중 실제로 소유한 것만 필터링
        List<ConsumptionItemDto> consumptionItems = Arrays.stream(ShopItemType.values())
                .filter(item -> item.getCategory() == ItemType.CONSUMPTION)
                .map(item -> ConsumptionItemDto.builder()
                        .itemName(item.getDisplayName())
                        .emoji(item.getEmoji())
                        .count(getConsumptionItemCount(user, item))
                        .itemType(item.name())
                        .build())
                .filter(item -> item.getCount() > 0)  // 개수가 1개 이상인 것만 필터링
                .collect(Collectors.toList());

        int totalCount = consumptionItems.stream()
                .mapToInt(ConsumptionItemDto::getCount)
                .sum();

        return ConsumptionInventoryDto.builder()
                .categoryName("소모품")
                .totalCount(totalCount)
                .description("시청 완료 시 획득할 수 있는 아이템입니다.")
                .items(consumptionItems)
                .build();
    }

    /**
     * 치장품만 조회 (소유한 것만 표시)
     */
    public CosmeticInventoryDto getCosmeticItems(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

        // 캐릭터 조회 (착용 상태 확인용)
        Character character = characterRepository.findByUserId(userId).orElse(null);

        // 치장품 아이템들 중 실제로 소유한 것만 필터링
        List<CosmeticItemDto> cosmeticItems = Arrays.stream(ShopItemType.values())
                .filter(item -> item.getCategory() == ItemType.COSMETIC)
                .map(item -> CosmeticItemDto.builder()
                        .itemName(item.getDisplayName())
                        .emoji(item.getEmoji())
                        .isOwned(getCosmeticItemCount(user, item) > 0)
                        .isEquipped(character != null && getCosmeticItemEquipped(character, item))
                        .itemType(item.name())
                        .build())
                .filter(item -> item.isOwned())  // 소유한 것만 필터링
                .collect(Collectors.toList());

        int totalOwnedCount = cosmeticItems.size();  // 필터링된 리스트의 크기가 곧 소유한 종류 수

        int totalEquippedCount = (int) cosmeticItems.stream()
                .mapToLong(item -> item.isEquipped() ? 1 : 0)
                .sum();

        return CosmeticInventoryDto.builder()
                .categoryName("치장품")
                .totalOwnedCount(totalOwnedCount)
                .totalEquippedCount(totalEquippedCount)
                .description("구매, 기부, 게임 완료 시 획득할 수 있는 아이템입니다.")
                .items(cosmeticItems)
                .build();
    }

    /**
     * 아이템 타입별 조회
     */
    public Object getItemsByType(Long userId, ItemType itemType) {
        switch (itemType) {
            case CONSUMPTION:
                return getConsumptionItems(userId);
            case COSMETIC:
                return getCosmeticItems(userId);
            default:
                throw new IllegalArgumentException("지원하지 않는 아이템 타입입니다: " + itemType);
        }
    }

    /**
     * 인벤토리 요약 정보
     */
    public InventorySummaryDto getInventorySummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

        // 캐릭터 조회 (착용 상태 확인용)
        Character character = characterRepository.findByUserId(userId).orElse(null);

        // 소유한 치장품 종류 수 계산
        long ownedCosmeticCount = Arrays.stream(ShopItemType.values())
                .filter(item -> item.getCategory() == ItemType.COSMETIC)
                .mapToLong(item -> getCosmeticItemCount(user, item) > 0 ? 1 : 0)
                .sum();

        int totalConsumptionCount = user.getPersimmonCount() + user.getGreenTeaCount();

        return InventorySummaryDto.builder()
                .userId(userId)
                .totalConsumptionCount(totalConsumptionCount)
                .persimmonCount(user.getPersimmonCount())
                .greenTeaCount(user.getGreenTeaCount())
                .totalOwnedCosmeticCount((int) ownedCosmeticCount)
                .totalEquippedCosmeticCount(character != null ? (int) Arrays.stream(ShopItemType.values())
                        .filter(item -> item.getCategory() == ItemType.COSMETIC)
                        .mapToLong(item -> getCosmeticItemEquipped(character, item) ? 1 : 0)
                        .sum() : 0)
                .hasStrawberryHairpin(user.getStrawberryHairpinCount() > 0)
                .hasGongbangAhjima(user.getGongbangAhjimaCount() > 0)
                .hasCarCrown(user.getCarCrownCount() > 0)
                .hasRose(user.getRoseCount() > 0)
                .totalItemCount(totalConsumptionCount + (int) ownedCosmeticCount)
                .build();
    }

    /**
     * 소모품 아이템 개수 조회
     */
    private int getConsumptionItemCount(User user, ShopItemType itemType) {
        switch (itemType) {
            case PERSIMMON:
                return user.getPersimmonCount();
            case GREEN_TEA:
                return user.getGreenTeaCount();
            default:
                return 0;
        }
    }

    /**
     * 치장품 아이템 개수 조회 (소유 여부)
     */
    private int getCosmeticItemCount(User user, ShopItemType itemType) {
        switch (itemType) {
            case STRAWBERRY_HAIRPIN:
                return user.getStrawberryHairpinCount();
            case GONGBANG_AHJIMA:
                return user.getGongbangAhjimaCount();
            case CAR_CROWN:
                return user.getCarCrownCount();
            case ROSE:
                return user.getRoseCount();
            default:
                return 0;
        }
    }

    /**
     * 치장품 아이템 착용 여부 조회 (Character 엔티티에서 직접)
     */
    private boolean getCosmeticItemEquipped(Character character, ShopItemType itemType) {
        switch (itemType) {
            case STRAWBERRY_HAIRPIN:
                return character.getEquippedStrawberryHairpin();
            case GONGBANG_AHJIMA:
                return character.getEquippedGongbangAhjima();
            case CAR_CROWN:
                return character.getEquippedCarCrown();
            case ROSE:
                return character.getEquippedRose();
            default:
                return false;
        }
    }
}