package edu.levytskyi.lab2microservices.dto;

import edu.levytskyi.lab2microservices.model.Transaction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreateDTO {

    @NotNull(message = "Wallet ID cannot be null")
    private Long walletId;

    @NotNull(message = "Transaction type cannot be null")
    private Transaction.TransactionType type; // BUY, SELL

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private Double amount; // Amount of cryptocurrency
}