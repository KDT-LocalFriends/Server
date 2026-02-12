package com.backend.kdt.charge.controller;

import com.backend.kdt.auth.dto.ApiResponse;
import com.backend.kdt.charge.dto.ChargeResponseDto;
import com.backend.kdt.charge.dto.ChargeTypeDto;
import com.backend.kdt.charge.entity.ChargeType;
import com.backend.kdt.charge.service.ChargeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/charge")
@Tag(name = "[구현 완료] 충전 API", description = "4가지 코인 충전")
public class ChargeController {

    private final ChargeService chargeService;

    @GetMapping("/types")
    @Operation(summary = "충전 타입 목록 조회", description = "충전 가능한 코인 타입들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ChargeTypeDto>>> getChargeTypes() {
        List<ChargeTypeDto> chargeTypes = chargeService.getChargeTypes();
        return ResponseEntity.ok(ApiResponse.onSuccess(chargeTypes));
    }

    @PostMapping("/{chargeType}")
    @Operation(summary = "코인 충전", description = "선택한 타입의 코인을 즉시 충전합니다.")
    public ResponseEntity<ApiResponse<ChargeResponseDto>> chargePoints(
            @Parameter(description = "충전 타입") @PathVariable ChargeType chargeType,
            @Parameter(description = "사용자 ID") @RequestParam Long userId) {
        ChargeResponseDto response = chargeService.chargePoints(userId, chargeType);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}
