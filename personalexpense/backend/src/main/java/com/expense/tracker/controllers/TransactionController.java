package com.expense.tracker.controllers;

import com.expense.tracker.models.Transaction;
import com.expense.tracker.models.User;
import com.expense.tracker.repositories.TransactionRepository;
import com.expense.tracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserTransactions(@PathVariable Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByDateTimeDesc(userId);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<?> addTransaction(@PathVariable Long userId, @RequestBody Transaction transaction) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }
        transaction.setUser(userOpt.get());
        if (transaction.getDateTime() == null) {
            transaction.setDateTime(LocalDateTime.now());
        }
        Transaction saved = transactionRepository.save(transaction);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        if (!transactionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        transactionRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Transaction deleted"));
    }
}
