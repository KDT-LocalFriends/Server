package com.backend.kdt.pay.dto;

import com.backend.kdt.pay.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private String imageUrl;
    private int pointCost;
    private int stock;

    public static ProductDto from(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .imageUrl(product.getImageUrl())
                .pointCost(product.getPointCost())
                .stock(product.getStock())
                .build();
    }
}

