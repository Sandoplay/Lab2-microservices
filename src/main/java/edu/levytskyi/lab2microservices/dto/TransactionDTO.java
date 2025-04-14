package edu.levytskyi.lab2microservices.dto;

import edu.levytskyi.lab2microservices.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private Long walletId;
    private String currencySymbol; // Added for better context
    private Transaction.TransactionType type;
    private Double amount;
    private Double price;
    private LocalDateTime timestamp;
}