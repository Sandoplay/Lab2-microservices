package edu.levytskyi.lab2microservices.service;

import edu.levytskyi.lab2microservices.dto.WalletCreateDTO;
import edu.levytskyi.lab2microservices.dto.WalletDTO;
import edu.levytskyi.lab2microservices.model.Currency;
import edu.levytskyi.lab2microservices.model.User;
import edu.levytskyi.lab2microservices.model.Wallet;
import edu.levytskyi.lab2microservices.exception.ResourceNotFoundException;
import edu.levytskyi.lab2microservices.mapper.WalletMapper;
import edu.levytskyi.lab2microservices.repository.CurrencyRepository;
import edu.levytskyi.lab2microservices.repository.UserRepository;
import edu.levytskyi.lab2microservices.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// ... other imports ...
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository; // Додано
    private final CurrencyRepository currencyRepository; // Додано
    private final WalletMapper walletMapper; // Додано Mapper

    @Transactional(readOnly = true)
    public List<WalletDTO> getAllWallets() {
        return walletRepository.findAll().stream()
                .map(walletMapper::toDto).collect(Collectors.toList()); // Повертає DTO
    }

    @Transactional(readOnly = true)
    public WalletDTO getWalletById(Long id) {
        return walletRepository.findById(id).map(walletMapper::toDto) // Повертає DTO
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id)); // Використовує новий Exception
    }

    // Метод отримання гаманців користувача (змінився тип повернення + перевірка)
    @Transactional(readOnly = true)
    public List<WalletDTO> getWalletsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) { // Перевірка існування користувача
            throw new ResourceNotFoundException("User not found with id: " + userId); }
        return walletRepository.findByUserId(userId).stream()
                .map(walletMapper::toDto).collect(Collectors.toList()); // Повертає DTO
    }

    @Transactional
    public WalletDTO createWallet(WalletCreateDTO walletCreateDTO) { // Приймає DTO
        User user = userRepository.findById(walletCreateDTO.getUserId()) // Знаходить User
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + walletCreateDTO.getUserId()));
        Currency currency = currencyRepository.findById(walletCreateDTO.getCurrencyId()) // Знаходить Currency
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + walletCreateDTO.getCurrencyId()));

        // Перевірка на дублікат гаманця для користувача/валюти
        walletRepository.findByUserIdAndCurrencyId(user.getId(), currency.getId()).ifPresent(w -> {
            throw new IllegalArgumentException("User already has a wallet for currency: " + currency.getSymbol()); });

        Wallet wallet = walletMapper.toEntity(walletCreateDTO); // Маппінг з DTO
        wallet.setUser(user); // Встановлення зв'язків
        wallet.setCurrency(currency);

        Wallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toDto(savedWallet); // Повертає DTO
    }

    // Новий метод видалення
    @Transactional
    public void deleteWallet(Long id) {
        Wallet wallet = walletRepository.findById(id) // Перевірка перед видаленням
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));
        walletRepository.delete(wallet); // Каскадне видалення спрацює для транзакцій
    }
}