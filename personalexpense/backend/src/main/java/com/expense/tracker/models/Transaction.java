package com.expense.tracker.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import com.expense.tracker.models.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private String category;
    private String merchant;
    private String description;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "is_from_sms")
    private boolean isFromSms = false;

    @Column(name = "raw_sms", columnDefinition = "TEXT")
    private String rawSms;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public boolean isFromSms() { return isFromSms; }
    public void setFromSms(boolean fromSms) { this.isFromSms = fromSms; }
    public String getRawSms() { return rawSms; }
    public void setRawSms(String rawSms) { this.rawSms = rawSms; }
}
