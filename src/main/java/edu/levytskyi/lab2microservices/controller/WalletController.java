package edu.levytskyi.lab2microservices.controller;
/* @author Sandoplay
 * @project Lab1-miroservices
 * @class aaa
 * @version 1.0.0
 * @since 25.03.2025 - 15.24
 */

import edu.levytskyi.lab2microservices.entity.Wallet;
import edu.levytskyi.lab2microservices.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {
    @Autowired
    private WalletService walletService;

    @GetMapping
    public ResponseEntity<List<Wallet>> getAllWallets() {
        return ResponseEntity.ok(walletService.getAllWallets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getWalletById(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.getWalletById(id));
    }
    //Отримання всіх гаманців юзера
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Wallet>> getWalletsByUserId(@PathVariable Long userId) {
        List<Wallet> wallets = walletService.getWalletsByUserId(userId);
        return new ResponseEntity<>(wallets, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestParam Long userId, @RequestParam Long currencyId, @RequestParam Double initialBalance) {
        Wallet createdWallet = walletService.createWallet(userId, currencyId, initialBalance);
        return new ResponseEntity<>(createdWallet, HttpStatus.CREATED);
    }
}