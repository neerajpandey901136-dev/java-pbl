package com.expense.tracker.repositories;

import com.expense.tracker.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByDateTimeDesc(Long userId);
    
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.rawSms = :rawSms")
    List<Transaction> findDuplicateSmsTransactions(@Param("userId") Long userId, @Param("rawSms") String rawSms);
}
