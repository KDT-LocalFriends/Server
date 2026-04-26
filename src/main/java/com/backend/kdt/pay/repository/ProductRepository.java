package com.backend.kdt.pay.repository;

import com.backend.kdt.pay.entity.Product;
import com.backend.kdt.pay.entity.TransactionType;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByTransactionType(TransactionType transactionType);

    // 비관적 락: 재고 차감 시 해당 row 선점
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);
}
