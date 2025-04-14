package edu.levytskyi.lab2microservices.controller;

import edu.levytskyi.lab2microservices.dto.WalletCreateDTO; // Новий DTO
import edu.levytskyi.lab2microservices.dto.WalletDTO; // Новий DTO
import edu.levytskyi.lab2microservices.service.WalletService;
import jakarta.validation.Valid; // Додано
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated; // Додано
import org.springframework.web.bind.annotation.*;
// ... other imports ...
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Validated // Увімкнення валідації
public class WalletController {

    private final WalletService walletService;

    @GetMapping // Повертає DTO
    public ResponseEntity<List<WalletDTO>> getAllWallets() {
        return ResponseEntity.ok(walletService.getAllWallets());
    }

    @GetMapping("/{id}") // Повертає DTO
    public ResponseEntity<WalletDTO> getWalletById(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.getWalletById(id));
    }

    @GetMapping("/user/{userId}") // Повертає DTO
    public ResponseEntity<List<WalletDTO>> getWalletsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getWalletsByUserId(userId));
    }

    @PostMapping // Приймає CreateDTO, повертає DTO, використовує @Valid
    public ResponseEntity<WalletDTO> createWallet(@Valid @RequestBody WalletCreateDTO walletCreateDTO) {
        WalletDTO createdWallet = walletService.createWallet(walletCreateDTO);
        return new ResponseEntity<>(createdWallet, HttpStatus.CREATED);
    }

    // Новий ендпоінт видалення
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWallet(@PathVariable Long id) {
        walletService.deleteWallet(id);
        return ResponseEntity.noContent().build();
    }
}