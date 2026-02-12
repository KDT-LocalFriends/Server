package com.backend.kdt.shop.controller;

import com.backend.kdt.auth.dto.ApiResponse;
import com.backend.kdt.pay.entity.ItemType;
import com.backend.kdt.shop.dto.ItemPurchaseResponseDto;
import com.backend.kdt.shop.dto.ItemTypeDto;
import com.backend.kdt.shop.entity.ShopItemType;
import com.backend.kdt.shop.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
@Tag(name = "[구현완료] 아이템 상점 API", description = "아이템 조회 및 구매")
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items")
    @Operation(summary = "전체 아이템 목록 조회", description = "상점에서 구매 가능한 모든 아이템을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ItemTypeDto>>> getAllItems() {
        List<ItemTypeDto> items = itemService.getAllItems();
        return ResponseEntity.ok(ApiResponse.onSuccess(items));
    }

    @GetMapping("/items/category/{category}")
    @Operation(summary = "카테고리별 아이템 조회", description = "특정 카테고리(꾸미기/성장)의 아이템들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ItemTypeDto>>> getItemsByCategory(
            @Parameter(description = "아이템 카테고리") @PathVariable ItemType category) {
        List<ItemTypeDto> items = itemService.getItemsByCategory(category);
        return ResponseEntity.ok(ApiResponse.onSuccess(items));
    }

    @GetMapping("/items/consumption")
    @Operation(summary = "꾸미기 아이템 조회", description = "꾸미기 카테고리 아이템들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ItemTypeDto>>> getCosmeticItems() {
        List<ItemTypeDto> items = itemService.getCosmeticItems();
        return ResponseEntity.ok(ApiResponse.onSuccess(items));
    }

    @GetMapping("/items/cosmetic")
    @Operation(summary = "성장 아이템 조회", description = "성장 카테고리 아이템들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ItemTypeDto>>> getConsumptionItems() {
        List<ItemTypeDto> items = itemService.getConsumptionItems();
        return ResponseEntity.ok(ApiResponse.onSuccess(items));
    }

    @PostMapping("/{itemType}/purchase")
    @Operation(summary = "아이템 구매", description = "선택한 아이템 1개를 포인트로 구매합니다.")
    public ResponseEntity<ApiResponse<ItemPurchaseResponseDto>> purchaseItem(
            @Parameter(description = "아이템 타입") @PathVariable ShopItemType itemType,
            @Parameter(description = "사용자 ID") @RequestParam Long userId) {
        try {
            ItemPurchaseResponseDto response = itemService.purchaseItem(userId, itemType);
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.onFailure("PURCHASE_FAILED", e.getMessage()));
        }
    }
}