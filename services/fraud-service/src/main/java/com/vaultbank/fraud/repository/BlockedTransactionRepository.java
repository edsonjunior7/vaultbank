package com.vaultbank.fraud.repository;

import com.vaultbank.fraud.entity.BlockedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedTransactionRepository extends JpaRepository<BlockedTransaction, Long> {
    boolean existsByTransactionId(Long transactionId);
}
