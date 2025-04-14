package edu.levytskyi.lab2microservices.service;

import edu.levytskyi.lab2microservices.dto.CurrencyDTO;
import edu.levytskyi.lab2microservices.model.Currency;
import edu.levytskyi.lab2microservices.exception.ResourceNotFoundException;
import edu.levytskyi.lab2microservices.mapper.CurrencyMapper;
import edu.levytskyi.lab2microservices.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;
    private static final Map<String, Double> FAKE_PRICES = new HashMap<>();
    static { // Ініціалізація цін
        FAKE_PRICES.put("BTC", 65000.0); FAKE_PRICES.put("ETH", 3500.0);
        FAKE_PRICES.put("LTC", 180.0); FAKE_PRICES.put("SOL", 150.0);
        FAKE_PRICES.put("RVN", 5.0);
    }

    @Transactional(readOnly = true)
    public List<CurrencyDTO> getAllCurrencies() {
        return currencyRepository.findAll().stream()
                .map(currencyMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CurrencyDTO getCurrencyById(Long id) {
        return currencyRepository.findById(id).map(currencyMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public CurrencyDTO getCurrencyBySymbol(String symbol) {
        return currencyRepository.findBySymbol(symbol.toUpperCase()).map(currencyMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with symbol: " + symbol));
    }

    @Transactional
    public CurrencyDTO createCurrency(CurrencyDTO currencyDTO) {
        currencyRepository.findBySymbol(currencyDTO.getSymbol().toUpperCase()).ifPresent(c -> {
            throw new IllegalArgumentException("Currency symbol already exists: " + currencyDTO.getSymbol()); });
        Currency currency = currencyMapper.toEntity(currencyDTO);
        currency.setSymbol(currency.getSymbol().toUpperCase());
        currency.setCurrentPrice(getCurrentPrice(currency.getSymbol())); // Використання методу
        Currency savedCurrency = currencyRepository.save(currency);
        return currencyMapper.toDto(savedCurrency);
    }

    @Transactional
    public CurrencyDTO updateCurrency(Long id, CurrencyDTO currencyDTO) {
        Currency existingCurrency = currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + id));
        Optional<Currency> conflictingCurrency = currencyRepository.findBySymbol(currencyDTO.getSymbol().toUpperCase());
        if(conflictingCurrency.isPresent() && !conflictingCurrency.get().getId().equals(id)) {
            throw new IllegalArgumentException("Currency symbol already exists: " + currencyDTO.getSymbol()); }

        currencyMapper.updateEntityFromDto(currencyDTO, existingCurrency);
        existingCurrency.setSymbol(existingCurrency.getSymbol().toUpperCase());
        existingCurrency.setCurrentPrice(getCurrentPrice(existingCurrency.getSymbol())); // Оновлення ціни
        Currency updatedCurrency = currencyRepository.save(existingCurrency);
        return currencyMapper.toDto(updatedCurrency);
    }

    @Transactional
    public void deleteCurrency(Long id) {
        if (!currencyRepository.existsById(id)) { throw new ResourceNotFoundException("Currency not found with id: " + id); }
        currencyRepository.deleteById(id);
    }

    // Метод для отримання ціни (змінився)
    public double getCurrentPrice(String symbol) {
        String upperSymbol = symbol.toUpperCase();
        Optional<Currency> dbCurrency = currencyRepository.findBySymbol(upperSymbol);
        if (dbCurrency.isPresent()) { return dbCurrency.get().getCurrentPrice(); }
        if (!FAKE_PRICES.containsKey(upperSymbol)) {
            throw new ResourceNotFoundException("Pricing not available for currency: " + symbol); }
        return FAKE_PRICES.get(upperSymbol);
    }

    // Метод для оновлення ціни (змінився)
    @Transactional
    public void updatePriceInDatabase(String symbol, double newPrice) {
        String upperSymbol = symbol.toUpperCase();
        Currency currency = currencyRepository.findBySymbol(upperSymbol)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with symbol: " + symbol));
        currency.setCurrentPrice(newPrice);
        currencyRepository.save(currency);
        FAKE_PRICES.put(upperSymbol, newPrice); // Оновлення кешу
    }
}