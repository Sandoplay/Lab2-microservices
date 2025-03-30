package edu.levytskyi.lab2microservices.controller;
/* @author Sandoplay
 * @project Lab1-miroservices
 * @class asf
 * @version 1.0.0
 * @since 25.03.2025 - 15.25
 */


import edu.levytskyi.lab2microservices.entity.Currency;
import edu.levytskyi.lab2microservices.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<List<Currency>> getAllCurrencies() {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Currency> getCurrencyById(@PathVariable Long id) {
        return ResponseEntity.ok(currencyService.getCurrencyById(id));
    }

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<Currency> getCurrencyBySymbol(@PathVariable String symbol) {
        return currencyService.findBySymbol(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping
    public ResponseEntity<Currency> createCurrency(@RequestBody Currency currency) {
        Currency createdCurrency = currencyService.createCurrency(currency);
        return new ResponseEntity<>(createdCurrency, HttpStatus.CREATED);
    }
    //Ендпоінт для оновлення
    @PutMapping("/updateFakePrice/{symbol}")
    public ResponseEntity<String> updateFakePrice(@PathVariable String symbol, @RequestParam double newPrice) {
        try {
            currencyService.updateFakePrice(symbol, newPrice);
            return ResponseEntity.ok("Fake price for " + symbol + " updated to " + newPrice);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}