package com.expense.tracker.services;

import com.expense.tracker.models.Transaction;
import com.expense.tracker.models.TransactionType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SMSParserService {

    private static final Pattern DEBIT_PATTERN_1 = Pattern
            .compile("(?i)(Rs\\.?|INR)\\s*([0-9,.]+)\\s+debited.*at\\s+([a-zA-Z0-9\\s]+)\\s+on");
    private static final Pattern DEBIT_PATTERN_2 = Pattern
            .compile("(?i)debited.*(?:Rs\\.?|INR)\\s*([0-9,.]+).*info[:\\-]\\s*([a-zA-Z0-9\\s]+)");

    private static final Pattern CREDIT_PATTERN_1 = Pattern
            .compile("(?i)(Rs\\.?|INR)\\s*([0-9,.]+)\\s+credited.*from\\s+([a-zA-Z0-9\\s]+)\\s+on");
    private static final Pattern CREDIT_PATTERN_2 = Pattern
            .compile("(?i)credited.*(?:Rs\\.?|INR)\\s*([0-9,.]+).*from\\s+([a-zA-Z0-9\\s]+)");

    private static final Pattern GENERIC_AMOUNT = Pattern.compile("(?i)(?:Rs\\.?|INR)\\s*([0-9,.]+)");

    public Transaction parseSMS(String smsBody) {
        Transaction tx = new Transaction();
        tx.setDateTime(LocalDateTime.now()); // For simplicity, setting current time. In reality, parse date from SMS or
                                             // use SMS received time.
        tx.setRawSms(smsBody);
        tx.setFromSms(true);

        boolean isParsed = false;

        Matcher d1 = DEBIT_PATTERN_1.matcher(smsBody);
        if (d1.find()) {
            tx.setAmount(parseAmount(d1.group(2)));
            tx.setMerchant(d1.group(3).trim());
            tx.setType(TransactionType.DEBIT);
            tx.setCategory(autoCategorize(tx.getMerchant()));
            isParsed = true;
        }

        if (!isParsed) {
            Matcher c1 = CREDIT_PATTERN_1.matcher(smsBody);
            if (c1.find()) {
                tx.setAmount(parseAmount(c1.group(2)));
                tx.setMerchant(c1.group(3).trim());
                tx.setType(TransactionType.CREDIT);
                tx.setCategory("Income");
                isParsed = true;
            }
        }

        if (!isParsed) {
            Matcher d2 = DEBIT_PATTERN_2.matcher(smsBody);
            if (d2.find()) {
                tx.setAmount(parseAmount(d2.group(1)));
                tx.setMerchant(d2.group(2).trim());
                tx.setType(TransactionType.DEBIT);
                tx.setCategory(autoCategorize(tx.getMerchant()));
                isParsed = true;
            }
        }

        if (!isParsed) {
            Matcher c2 = CREDIT_PATTERN_2.matcher(smsBody);
            if (c2.find()) {
                tx.setAmount(parseAmount(c2.group(1)));
                tx.setMerchant(c2.group(2).trim());
                tx.setType(TransactionType.CREDIT);
                tx.setCategory("Income");
                isParsed = true;
            }
        }

        if (!isParsed) {
            if (smsBody.toLowerCase().contains("debited") || smsBody.toLowerCase().contains("spent")) {
                tx.setType(TransactionType.DEBIT);
                tx.setCategory("Others");
            } else if (smsBody.toLowerCase().contains("credited") || smsBody.toLowerCase().contains("received")) {
                tx.setType(TransactionType.CREDIT);
                tx.setCategory("Income");
            } else {
                return null; // Could not determine type
            }

            Matcher amtMatcher = GENERIC_AMOUNT.matcher(smsBody);
            if (amtMatcher.find()) {
                tx.setAmount(parseAmount(amtMatcher.group(1)));
            } else {
                return null; // Could not find amount
            }
            tx.setMerchant("Unknown");
        }

        tx.setDescription("Auto-detected via SMS");
        return tx;
    }

    private Double parseAmount(String amountStr) {
        try {
            return Double.parseDouble(amountStr.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String autoCategorize(String merchant) {
        String m = merchant.toLowerCase();
        if (m.contains("amazon") || m.contains("flipkart") || m.contains("myntra")) {
            return "Shopping";
        } else if (m.contains("zomato") || m.contains("swiggy") || m.contains("starbucks")
                || m.contains("restaurant")) {
            return "Food";
        } else if (m.contains("jio") || m.contains("airtel") || m.contains("bill") || m.contains("electricity")) {
            return "Bills";
        } else if (m.contains("uber") || m.contains("ola") || m.contains("irctc")) {
            return "Travel";
        } else {
            return "Others";
        }
    }
}
