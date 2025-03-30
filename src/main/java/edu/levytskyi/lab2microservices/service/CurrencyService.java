package edu.levytskyi.lab2microservices.service;
/* @author Sandoplay
 * @project Lab1-miroservices
 * @class asdff
 * @version 1.0.0
 * @since 25.03.2025 - 15.23
 */

// CurrencyService.java (Імітація отримання курсу)

import edu.levytskyi.lab2microservices.entity.Currency;
import edu.levytskyi.lab2microservices.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CurrencyService {

    @Autowired
    private CurrencyRepository currencyRepository;

    private static final Map<String, Double> FAKE_PRICES = new HashMap<>();

    //Статичний блок для заповнення
    static {
        FAKE_PRICES.put("BTC", 45000.0);
        FAKE_PRICES.put("ETH", 3000.0);
        FAKE_PRICES.put("LTC", 150.0);
    }
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }
    public Optional<Currency> findBySymbol(String symbol) { //Пошук по символу
        return currencyRepository.findBySymbol(symbol);
    }
    //Метод для створення нової валюти
    public Currency createCurrency(Currency currency) {

        return currencyRepository.save(currency);
    }
    public Currency getCurrencyById(Long id) {
        return currencyRepository.findById(id).orElseThrow(() -> new RuntimeException("Currency not found"));
    }
    //  Імітація отримання поточного курсу.
    //  У реальному застосунку тут був би запит до API біржі.
    public double getCurrentPrice(String symbol) {
        //Перевірка чи є символ серед доступних
        if (!FAKE_PRICES.containsKey(symbol)) {
            throw new RuntimeException("Currency not supported: " + symbol);
        }
        //  Повертаємо "фейкову" ціну.
        return FAKE_PRICES.get(symbol);
    }
    //Оновлення фейкової ціни
    public void updateFakePrice(String symbol, double newPrice) {
        if (!FAKE_PRICES.containsKey(symbol)) {
            throw new RuntimeException("Currency not supported: " + symbol);
        }
        FAKE_PRICES.put(symbol, newPrice);

        //Опціональне оновлення ціни в базі даних
        currencyRepository.findBySymbol(symbol).ifPresent(currency -> {
            currency.setCurrentPrice(newPrice);
            currencyRepository.save(currency);
        });
    }

}