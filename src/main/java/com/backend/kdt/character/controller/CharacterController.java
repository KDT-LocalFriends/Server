package com.backend.kdt.character.controller;

import com.backend.kdt.auth.dto.ApiResponse;
import com.backend.kdt.character.dto.CharacterDto;
import com.backend.kdt.character.service.CharacterService;
import com.backend.kdt.pay.dto.GameCompletionResponseDto;
import com.backend.kdt.shop.entity.ShopItemType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
@Tag(name = "[구현완료] 캐릭터 API", description = "캐릭터 관련 기능")
public class CharacterController {

    private final CharacterService characterService;

    @GetMapping("/{userId}")
    @Operation(summary = "캐릭터 정보 조회", description = "사용자의 캐릭터 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<CharacterDto>> getCharacter(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        CharacterDto character = characterService.getCharacterByUserId(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(character));
    }

    @PostMapping("/game")
    @Operation(summary = "게임 완료", description = "게임을 완료하고 경험치를 획득합니다. (하루 3번 제한)")
    public ResponseEntity<ApiResponse<GameCompletionResponseDto>> completeGame(
            @Parameter(description = "사용자 ID") @RequestParam Long userId) {
        try {
            GameCompletionResponseDto response = characterService.completeGame(userId);
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.onFailure("GAME_LIMIT_EXCEEDED", e.getMessage()));
        }
    }

    @PostMapping("/pet")
    @Operation(summary = "캐릭터 쓰다듬기", description = "캐릭터를 쓰다듬어 경험치 20을 지급합니다. (하루 3번 제한)")
    public ResponseEntity<ApiResponse<GameCompletionResponseDto>> petCharacter(
            @Parameter(description = "사용자 ID") @RequestParam Long userId) {
        try {
            GameCompletionResponseDto response = characterService.petCharacter(userId);
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.onFailure("PET_LIMIT_EXCEEDED", e.getMessage()));
        }
    }

    @PostMapping("/feed")
    @Operation(summary = "캐릭터 먹이주기", description = "소모품을 소비하여 캐릭터에게 먹이를 주고 경험치 20을 지급합니다. (하루 3번 제한)")
    public ResponseEntity<ApiResponse<GameCompletionResponseDto>> feedCharacter(
            @Parameter(description = "사용자 ID") @RequestParam Long userId) {
        try {
            GameCompletionResponseDto response = characterService.feedCharacter(userId);
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.onFailure("FEED_ERROR", e.getMessage()));
        }
    }

    @GetMapping("/remaining-pets")
    @Operation(summary = "남은 쓰다듬기 횟수 조회", description = "오늘 남은 쓰다듬기 가능 횟수를 조회합니다.")
    public ResponseEntity<ApiResponse<Integer>> getRemainingPets(
            @Parameter(description = "사용자 ID") @RequestParam Long userId) {
        int remaining = characterService.getRemainingDailyPets(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(remaining));
    }

    @GetMapping("/remaining-feeds")
    @Operation(summary = "남은 먹이주기 횟수 조회", description = "오늘 남은 먹이주기 가능 횟수를 조회합니다.")
    public ResponseEntity<ApiResponse<Integer>> getRemainingFeeds(
            @Parameter(description = "사용자 ID") @RequestParam Long userId) {
        int remaining = characterService.getRemainingDailyFeeds(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(remaining));
    }

    @PostMapping("/equip")
    @Operation(summary = "아이템 착용", description = "치장품 아이템을 착용합니다. (하나씩만 착용 가능)")
    public ResponseEntity<ApiResponse<CharacterDto>> equipItem(
            @Parameter(description = "사용자 ID") @RequestParam Long userId,
            @Parameter(description = "착용할 아이템 타입") @RequestParam ShopItemType itemType) {
        try {
            CharacterDto response = characterService.equipItem(userId, itemType, true);
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.onFailure("EQUIP_FAILED", e.getMessage()));
        }
    }

    @PostMapping("/unequip")
    @Operation(summary = "아이템 해제", description = "착용 중인 치장품 아이템을 해제합니다.")
    public ResponseEntity<ApiResponse<CharacterDto>> unequipItem(
            @Parameter(description = "사용자 ID") @RequestParam Long userId,
            @Parameter(description = "해제할 아이템 타입") @RequestParam ShopItemType itemType) {
        try {
            CharacterDto response = characterService.equipItem(userId, itemType, false);
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.onFailure("UNEQUIP_FAILED", e.getMessage()));
        }
    }
}