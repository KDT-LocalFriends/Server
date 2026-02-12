package com.backend.kdt.pay.service;

import com.backend.kdt.pay.entity.Product;
import com.backend.kdt.pay.entity.TransactionType;
import com.backend.kdt.pay.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductDataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) return; // 이미 데이터가 있다면 실행하지 않음

        List<Product> products = List.of(
                // 김해 특산물 - 구매 상품 (PURCHASE)
                Product.builder()
                        .name("김해 대저토마토 선물세트")
                        .transactionType(TransactionType.PURCHASE)
                        .pointCost(15000)
                        .stock(30)
                        .imageUrl("https://cdn.example.com/gimhae_tomato_set.jpg")
                        .build(),

                Product.builder()
                        .name("김해 봉하마을 딸기잼")
                        .transactionType(TransactionType.PURCHASE)
                        .pointCost(8000)
                        .stock(50)
                        .imageUrl("https://cdn.example.com/gimhae_strawberry_jam.jpg")
                        .build(),

                Product.builder()
                        .name("김해 분성산 꿀 세트")
                        .transactionType(TransactionType.PURCHASE)
                        .pointCost(25000)
                        .stock(20)
                        .imageUrl("https://cdn.example.com/gimhae_honey_set.jpg")
                        .build(),

                Product.builder()
                        .name("김해 전통 누룩 막걸리")
                        .transactionType(TransactionType.PURCHASE)
                        .pointCost(12000)
                        .stock(40)
                        .imageUrl("https://cdn.example.com/gimhae_makgeolli.jpg")
                        .build(),

                Product.builder()
                        .name("김해 봉하마을 쌀 5kg")
                        .transactionType(TransactionType.PURCHASE)
                        .pointCost(18000)
                        .stock(25)
                        .imageUrl("https://cdn.example.com/gimhae_rice_5kg.jpg")
                        .build(),

                // 김해 지역 기부처 - 기부 상품 (DONATION)
                Product.builder()
                        .name("김해시 독거노인 급식 지원")
                        .transactionType(TransactionType.DONATION)
                        .pointCost(0) // 기부는 사용자가 직접 금액 입력
                        .stock(999) // 기부는 재고 제한 없음
                        .imageUrl("https://cdn.example.com/gimhae_elderly_support.jpg")
                        .build(),

                Product.builder()
                        .name("김해 유기동물 보호센터 후원")
                        .transactionType(TransactionType.DONATION)
                        .pointCost(0) // 기부는 사용자가 직접 금액 입력
                        .stock(999)
                        .imageUrl("https://cdn.example.com/gimhae_animal_shelter.jpg")
                        .build(),

                Product.builder()
                        .name("김해시 아동복지시설 지원")
                        .transactionType(TransactionType.DONATION)
                        .pointCost(0) // 기부는 사용자가 직접 금액 입력
                        .stock(999)
                        .imageUrl("https://cdn.example.com/gimhae_children_welfare.jpg")
                        .build(),

                Product.builder()
                        .name("김해 환경보호 활동 후원")
                        .transactionType(TransactionType.DONATION)
                        .pointCost(0) // 기부는 사용자가 직접 금액 입력
                        .stock(999)
                        .imageUrl("https://cdn.example.com/gimhae_environment.jpg")
                        .build(),

                Product.builder()
                        .name("김해시 저소득층 학습지원 기금")
                        .transactionType(TransactionType.DONATION)
                        .pointCost(0) // 기부는 사용자가 직접 금액 입력
                        .stock(999)
                        .imageUrl("https://cdn.example.com/gimhae_education_support.jpg")
                        .build()
        );

        productRepository.saveAll(products);
    }
}