package edu.levytskyi.lab2microservices.service;
/* @author Sandoplay
 * @project Lab1-miroservices
 * @class asf
 * @version 1.0.0
 * @since 25.03.2025 - 15.22
 */

import edu.levytskyi.lab2microservices.entity.Transaction;
import edu.levytskyi.lab2microservices.entity.Wallet;
import edu.levytskyi.lab2microservices.repository.TransactionRepository;
import edu.levytskyi.lab2microservices.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private CurrencyService currencyService; // Додаємо CurrencyService

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id).orElseThrow(() -> new RuntimeException("Transaction not found"));
    }
    //Отримання всіх транзакцій по гаманцю
    public List<Transaction> getTransactionsByWalletId(Long walletId) {
        return transactionRepository.findByWalletId(walletId);
    }
    @Transactional
    public Transaction createTransaction(Long walletId, Transaction.TransactionType type, Double amount) {
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found"));

        //Отримуємо актуальну ціну
        double currentPrice = currencyService.getCurrentPrice(wallet.getCurrency().getSymbol());


        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setPrice(currentPrice); // Зберігаємо ціну
        transaction.setTimestamp(LocalDateTime.now());

        // Оновлюємо баланс гаманця
        if (type == Transaction.TransactionType.BUY) {
            if (wallet.getBalance() < amount * currentPrice) {
                throw new RuntimeException("Insufficient funds");
            }
            wallet.setBalance(wallet.getBalance() - amount * currentPrice);
        } else if (type == Transaction.TransactionType.SELL) {
            //Перевірка чи є достатня кількість криптовалюти для продажі
            if (amount > wallet.getCurrency().getCurrentPrice()) {
                throw new RuntimeException("Insufficient cryptocurrency amount for sale.");
            }
            wallet.setBalance(wallet.getBalance() + amount * currentPrice);
        }


        walletRepository.save(wallet); // Зберігаємо оновлений гаманець
        return transactionRepository.save(transaction);
    }
}
