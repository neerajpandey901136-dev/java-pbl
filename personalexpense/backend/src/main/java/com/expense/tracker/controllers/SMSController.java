package com.expense.tracker.controllers;

import com.expense.tracker.models.Transaction;
import com.expense.tracker.models.User;
import com.expense.tracker.repositories.TransactionRepository;
import com.expense.tracker.repositories.UserRepository;
import com.expense.tracker.services.SMSParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/sms")
public class SMSController {

    @Autowired
    private SMSParserService smsParserService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/parse/{userId}")
    public ResponseEntity<?> parseAndSaveSMS(@PathVariable Long userId, @RequestBody Map<String, String> payload) {
        String smsBody = payload.get("sms");
        if (smsBody == null || smsBody.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "SMS content is required"));
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }

        // Duplicate Check
        List<Transaction> duplicates = transactionRepository.findDuplicateSmsTransactions(userId, smsBody);
        if (!duplicates.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Duplicate SMS transaction"));
        }

        Transaction transaction = smsParserService.parseSMS(smsBody);
        if (transaction == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Could not extract transaction details from SMS"));
        }

        transaction.setUser(userOpt.get());
        Transaction saved = transactionRepository.save(transaction);
        
        return ResponseEntity.ok(saved);
    }
}
