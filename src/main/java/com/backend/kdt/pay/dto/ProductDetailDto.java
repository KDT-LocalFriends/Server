package com.backend.kdt.pay.dto;

import com.backend.kdt.pay.entity.Product;
import com.backend.kdt.pay.entity.ProductExchange;
import com.backend.kdt.pay.entity.TransactionType;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailDto {
    private Long exchangeId;
    private Long productId;
    private String name;
    private String imageUrl;
    private int quantity;
    private int totalCost;
    private TransactionType transactionType;
    private boolean accepted;
    private LocalDateTime exchangedAt;

    public static ProductDetailDto from(ProductExchange exchange) {
        Product product = exchange.getProduct();
        return ProductDetailDto.builder()
                .exchangeId(exchange.getId())
                .productId(product.getId())
                .name(product.getName())
                .imageUrl(product.getImageUrl())
                .quantity(exchange.getQuantity())
                .totalCost(exchange.getTotalCost())
                .transactionType(exchange.getTransactionType())
                .accepted(exchange.isAccepted())
                .exchangedAt(exchange.getExchangedAt())
                .build();
    }
}

