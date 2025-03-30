package edu.levytskyi.lab2microservices.service;
/* @author Sandoplay
 * @project Lab1-miroservices
 * @class serv
 * @version 1.0.0
 * @since 25.03.2025 - 15.21
 */

// WalletService.java

import edu.levytskyi.lab2microservices.entity.User;
import edu.levytskyi.lab2microservices.entity.Currency;
import edu.levytskyi.lab2microservices.entity.Wallet;
import edu.levytskyi.lab2microservices.repository.CurrencyRepository;
import edu.levytskyi.lab2microservices.repository.UserRepository;
import edu.levytskyi.lab2microservices.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WalletService {

    @Autowired private WalletRepository walletRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CurrencyRepository currencyRepository;

    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    public Wallet getWalletById(Long id) {
        return walletRepository.findById(id).orElseThrow(() -> new RuntimeException("Wallet not found"));
    }
    //Отримання всіх гаманців користувача
    public List<Wallet> getWalletsByUserId(Long userId) {
        return walletRepository.findByUserId(userId);
    }
    @Transactional
    public Wallet createWallet(Long userId, Long currencyId, Double initialBalance) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Currency currency = currencyRepository.findById(currencyId).orElseThrow(()-> new RuntimeException("Currency not found"));

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setCurrency(currency);
        wallet.setBalance(initialBalance);
        return walletRepository.save(wallet);
    }
}