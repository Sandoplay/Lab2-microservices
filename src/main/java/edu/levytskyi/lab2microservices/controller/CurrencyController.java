package edu.levytskyi.lab2microservices.controller;

import edu.levytskyi.lab2microservices.dto.CurrencyDTO;
import edu.levytskyi.lab2microservices.service.CurrencyService;
import jakarta.validation.Valid; // Додано для валідації
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated; // Додано
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
@Validated // Увімкнення валідації
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping // Повертає DTO
    public ResponseEntity<List<CurrencyDTO>> getAllCurrencies() {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }

    @GetMapping("/{id}") // Повертає DTO
    public ResponseEntity<CurrencyDTO> getCurrencyById(@PathVariable Long id) {
        return ResponseEntity.ok(currencyService.getCurrencyById(id));
    }

    @GetMapping("/symbol/{symbol}") // Повертає DTO
    public ResponseEntity<CurrencyDTO> getCurrencyBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(currencyService.getCurrencyBySymbol(symbol));
    }

    @PostMapping // Приймає та повертає DTO, використовує @Valid
    public ResponseEntity<CurrencyDTO> createCurrency(@Valid @RequestBody CurrencyDTO currencyDTO) {
        if (currencyDTO.getId() != null) { return ResponseEntity.badRequest().build(); } // Перевірка ID
        CurrencyDTO createdCurrency = currencyService.createCurrency(currencyDTO);
        return new ResponseEntity<>(createdCurrency, HttpStatus.CREATED);
    }

    @PutMapping("/{id}") // Приймає та повертає DTO, використовує @Valid
    public ResponseEntity<CurrencyDTO> updateCurrency(@PathVariable Long id, @Valid @RequestBody CurrencyDTO currencyDTO) {
        if (currencyDTO.getId() != null && !currencyDTO.getId().equals(id)) { return ResponseEntity.badRequest().build(); } // Перевірка ID
        currencyDTO.setId(id);
        CurrencyDTO updatedCurrency = currencyService.updateCurrency(id, currencyDTO);
        return ResponseEntity.ok(updatedCurrency);
    }

    // Новий ендпоінт видалення
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurrency(@PathVariable Long id) {
        currencyService.deleteCurrency(id);
        return ResponseEntity.noContent().build();
    }

    // Ендпоінт оновлення ціни (змінився виклик сервісу)
    @PutMapping("/update-price/{symbol}")
    public ResponseEntity<String> updateCurrencyPrice(@PathVariable String symbol, @RequestParam double newPrice) {
        if (newPrice < 0) { return ResponseEntity.badRequest().body("Price cannot be negative."); }
        currencyService.updatePriceInDatabase(symbol, newPrice); // Новий метод сервісу
        return ResponseEntity.ok("Price for " + symbol.toUpperCase() + " updated to " + newPrice);
    }
}