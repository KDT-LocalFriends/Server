package com.backend.kdt.inventory.controller;

import com.backend.kdt.auth.dto.ApiResponse;
import com.backend.kdt.inventory.dto.ConsumptionInventoryDto;
import com.backend.kdt.inventory.dto.CosmeticInventoryDto;
import com.backend.kdt.inventory.dto.InventoryDto;
import com.backend.kdt.inventory.dto.InventorySummaryDto;
import com.backend.kdt.inventory.service.InventoryService;
import com.backend.kdt.pay.entity.ItemType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
@Tag(name = "[구현완료] 인벤토리 API", description = "사용자 아이템 인벤토리 조회")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @Operation(summary = "전체 인벤토리 조회", description = "사용자의 모든 아이템(소모품, 치장품)을 조회합니다.")
    public ResponseEntity<ApiResponse<InventoryDto>> getUserInventory(
            @Parameter(description = "사용자 ID") @RequestParam Long userId) {
        InventoryDto inventory = inventoryService.getUserInventory(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(inventory));
    }

    @GetMapping("/consumption")
    @Operation(summary = "소모품 인벤토리 조회", description = "사용자의 소모품만 조회합니다.")
    public ResponseEntity<ApiResponse<ConsumptionInventoryDto>> getConsumptionItems(
            @Parameter(description = "사용자 ID") @RequestParam Long userId) {
        ConsumptionInventoryDto items = inventoryService.getConsumptionItems(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(items));
    }

    @GetMapping("/cosmetic")
    @Operation(summary = "치장품 인벤토리 조회", description = "사용자의 치장품만 조회합니다.")
    public ResponseEntity<ApiResponse<CosmeticInventoryDto>> getCosmeticItems(
            @Parameter(description = "사용자 ID") @RequestParam Long userId) {
        CosmeticInventoryDto items = inventoryService.getCosmeticItems(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(items));
    }

    @GetMapping("/by-type")
    @Operation(summary = "아이템 타입별 조회", description = "특정 아이템 타입의 아이템들을 조회합니다.")
    public ResponseEntity<ApiResponse<Object>> getItemsByType(
            @Parameter(description = "사용자 ID") @RequestParam Long userId,
            @Parameter(description = "아이템 타입") @RequestParam ItemType itemType) {
        Object items = inventoryService.getItemsByType(userId, itemType);
        return ResponseEntity.ok(ApiResponse.onSuccess(items));
    }

//    @GetMapping("/summary")
//    @Operation(summary = "인벤토리 요약", description = "사용자의 아이템 보유 현황 요약 정보를 조회합니다.")
//    public ResponseEntity<ApiResponse<InventorySummaryDto>> getInventorySummary(
//            @Parameter(description = "사용자 ID") @RequestParam Long userId) {
//        InventorySummaryDto summary = inventoryService.getInventorySummary(userId);
//        return ResponseEntity.ok(ApiResponse.onSuccess(summary));
//    }
}