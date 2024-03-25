package org.example.account.repository;

import org.example.account.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,
        Long> {
    Optional<Transaction> findByTransactionId(String tansactionId);
}