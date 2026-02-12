package com.backend.kdt.pay.repository;

import com.backend.kdt.pay.entity.ProductExchange;
import com.backend.kdt.pay.entity.TransactionType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductExchangeRepository extends JpaRepository<ProductExchange, Long> {
    List<ProductExchange> findByUserIdOrderByExchangedAtDesc(Long userId);

    List<ProductExchange> findByUserIdAndAcceptedFalseAndTransactionTypeOrderByExchangedAtDesc(
            Long userId, TransactionType transactionType);

    List<ProductExchange> findByUserIdAndTransactionTypeOrderByExchangedAtDesc(
            Long userId, TransactionType transactionType);
}