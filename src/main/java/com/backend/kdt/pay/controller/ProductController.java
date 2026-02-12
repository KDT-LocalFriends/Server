package com.backend.kdt.pay.controller;

import com.backend.kdt.auth.dto.ApiResponse;
import com.backend.kdt.pay.dto.DonationRequestDto;
import com.backend.kdt.pay.dto.DonationResponseDto;
import com.backend.kdt.pay.dto.ExchangeResponseDto;
import com.backend.kdt.pay.dto.GameCompletionResponseDto;
import com.backend.kdt.pay.dto.ProductDetailDto;
import com.backend.kdt.pay.dto.ProductDto;
import com.backend.kdt.pay.dto.ProductExchangeRequestDto;
import com.backend.kdt.pay.dto.WatchCompletionResponseDto;
import com.backend.kdt.pay.entity.TransactionType;
import com.backend.kdt.pay.service.ProductService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "[구현완료] 상품 API", description = "기부, 특산품 구매, 게임/시청 보상")
public class ProductController {

    private final ProductService productService;

    // 특산물 구매 + 갯수 + COSMETIC 아이템 지급
    @PostMapping("/{productId}/exchange")
    @Operation(summary = "상품 교환", description = "사용자가 보유한 포인트로 상품을 교환하고 치장품을 받습니다.")
    public ResponseEntity<ApiResponse<ExchangeResponseDto>> exchangeProduct(
            @Parameter(description = "상품 ID") @PathVariable Long productId,
            @RequestBody ProductExchangeRequestDto request) {
        try {
            ExchangeResponseDto response = productService.exchangeProductWithResponse(
                    request.getUserId(), productId, request.getQuantity());
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.onFailure("EXCHANGE_FAILED", e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "카테고리에 따른 상품 리스트를 조회합니다.")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProducts(
            @Parameter(description = "상품 카테고리") @RequestParam TransactionType transactionType) {
        List<ProductDto> products = productService.getProductsByCategory(transactionType);
        return ResponseEntity.ok(ApiResponse.onSuccess(products));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "(구매전) 단일 상품 조회", description = "상품 ID로 특정 상품 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ProductDto>> getProduct(
            @Parameter(description = "상품 ID") @PathVariable Long productId) {
        ProductDto product = productService.getProduct(productId);
        return ResponseEntity.ok(ApiResponse.onSuccess(product));
    }

    // 기부하기 + COSMETIC 아이템 지급
    @PostMapping("/{productId}/donate")
    @Operation(summary = "기부하기", description = "사용자가 보유한 포인트로 기부하고 치장품을 받습니다.")
    public ResponseEntity<ApiResponse<DonationResponseDto>> donateProduct(
            @Parameter(description = "상품 ID") @PathVariable Long productId,
            @RequestBody DonationRequestDto request) {
        try {
            DonationResponseDto response = productService.donateProductWithResponse(
                    request.getUserId(), productId, request.getDonationAmount());
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.onFailure("DONATION_FAILED", e.getMessage()));
        }
    }

    @Operation(summary = "기부 내역 조회", description = "사용자의 기부 내역을 조회합니다.")
    @GetMapping("/my/donations")
    public ResponseEntity<ApiResponse<List<ProductDetailDto>>> getMyDonations(
            @Parameter(description = "사용자 ID") @RequestParam Long userId) {
        List<ProductDetailDto> donations = productService.getUserDonationHistory(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(donations));
    }

    // 영상 시청 + CONSUMPTION 아이템 지급
    @PostMapping("/complete-watching")
    @Operation(summary = "시청 완료", description = "시청 완료 시 소모품 3개를 지급합니다. (1회 제한)")
    public ResponseEntity<ApiResponse<WatchCompletionResponseDto>> completeWatching(
            @Parameter(description = "사용자 ID") @RequestParam Long userId) {
        try {
            WatchCompletionResponseDto response = productService.completeWatchingWithResponse(userId);
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.onFailure("ALREADY_COMPLETED", e.getMessage()));
        }
    }
    @Operation(summary = "시청 상태 조회", description = "유저의 시청 완료 여부를 조회합니다.")
    @GetMapping("/watch-status")
    public ResponseEntity<ApiResponse<Boolean>> getWatchStatus(
            @Parameter(description = "유저 ID") @RequestParam Long userId) {
        boolean isCompleted = productService.isWatchCompleted(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(isCompleted));
    }
}