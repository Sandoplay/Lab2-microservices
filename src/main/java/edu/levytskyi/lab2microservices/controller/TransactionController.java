package edu.levytskyi.lab2microservices.controller;

import edu.levytskyi.lab2microservices.dto.TransactionCreateDTO; // Новий DTO
import edu.levytskyi.lab2microservices.dto.TransactionDTO; // Новий DTO
import edu.levytskyi.lab2microservices.service.TransactionService;
import jakarta.validation.Valid; // Додано
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated; // Додано
import org.springframework.web.bind.annotation.*;
// ... other imports ...
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Validated // Увімкнення валідації
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping // Повертає DTO
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{id}") // Повертає DTO
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @GetMapping("/wallet/{walletId}") // Повертає DTO
    public ResponseEntity<List<TransactionDTO>> getTransactionsByWalletId(@PathVariable Long walletId) {
        return ResponseEntity.ok(transactionService.getTransactionsByWalletId(walletId));
    }

    // Новий ендпоінт
    @GetMapping("/user/{userId}") // Повертає DTO
    public ResponseEntity<List<TransactionDTO>> getTransactionsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getTransactionsByUserId(userId));
    }

    @PostMapping // Приймає CreateDTO, повертає DTO, використовує @Valid
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody TransactionCreateDTO transactionCreateDTO) {
        TransactionDTO createdTransaction = transactionService.createTransaction(transactionCreateDTO);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }
}