package edu.levytskyi.lab2microservices.controller;
/* @author Sandoplay
 * @project Lab1-miroservices
 * @class asvasv
 * @version 1.0.0
 * @since 25.03.2025 - 15.25
 */


import edu.levytskyi.lab2microservices.entity.Transaction;
import edu.levytskyi.lab2microservices.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired private TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }
    //Отримання всіх транзакцій по гаманцю
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<Transaction>> getTransactionsByWalletId(@PathVariable Long walletId) {
        List<Transaction> transactions = transactionService.getTransactionsByWalletId(walletId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestParam Long walletId, @RequestParam Transaction.TransactionType type, @RequestParam Double amount) {
        Transaction createdTransaction = transactionService.createTransaction(walletId, type, amount);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

}