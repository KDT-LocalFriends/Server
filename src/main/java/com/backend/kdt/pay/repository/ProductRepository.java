package com.backend.kdt.pay.repository;

import com.backend.kdt.pay.entity.Product;
import com.backend.kdt.pay.entity.TransactionType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByTransactionType(TransactionType transactionType);

}