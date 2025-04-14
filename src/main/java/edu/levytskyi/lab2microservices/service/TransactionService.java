package edu.levytskyi.lab2microservices.service;

import edu.levytskyi.lab2microservices.dto.TransactionCreateDTO;
import edu.levytskyi.lab2microservices.dto.TransactionDTO;
import edu.levytskyi.lab2microservices.model.Transaction;
import edu.levytskyi.lab2microservices.model.Wallet;
import edu.levytskyi.lab2microservices.exception.InsufficientFundsException;
import edu.levytskyi.lab2microservices.exception.ResourceNotFoundException;
import edu.levytskyi.lab2microservices.mapper.TransactionMapper;
import edu.levytskyi.lab2microservices.repository.TransactionRepository;
import edu.levytskyi.lab2microservices.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// ... other imports ...
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final CurrencyService currencyService; // Використання CurrencyService
    private final TransactionMapper transactionMapper; // Додано Mapper

    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(transactionMapper::toDto).collect(Collectors.toList()); // Використання DTO
    }

    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(Long id) {
        return transactionRepository.findById(id).map(transactionMapper::toDto) // Використання DTO
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByWalletId(Long walletId) {
        if (!walletRepository.existsById(walletId)) { // Перевірка існування гаманця
            throw new ResourceNotFoundException("Wallet not found with id: " + walletId); }
        return transactionRepository.findByWalletId(walletId).stream()
                .map(transactionMapper::toDto).collect(Collectors.toList()); // Використання DTO
    }

    // Новий метод
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByWalletUserId(userId).stream()
                .map(transactionMapper::toDto).collect(Collectors.toList()); // Використання DTO
    }

    @Transactional
    public TransactionDTO createTransaction(TransactionCreateDTO transactionCreateDTO) { // Приймає DTO
        Wallet wallet = walletRepository.findById(transactionCreateDTO.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + transactionCreateDTO.getWalletId()));

        double currentPrice = currencyService.getCurrentPrice(wallet.getCurrency().getSymbol()); // Отримання ціни з сервісу
        double amount = transactionCreateDTO.getAmount();
        Transaction.TransactionType type = transactionCreateDTO.getType();

        Transaction transaction = transactionMapper.toEntity(transactionCreateDTO); // Маппінг з DTO
        transaction.setWallet(wallet);
        transaction.setPrice(currentPrice); // Встановлення ціни
        transaction.setTimestamp(LocalDateTime.now()); // Встановлення часу

        // Оновлена логіка балансу та перевірки
        if (type == Transaction.TransactionType.BUY) {
            wallet.setBalance(wallet.getBalance() + amount);
        } else if (type == Transaction.TransactionType.SELL) {
            if (wallet.getBalance() < amount) {
                throw new InsufficientFundsException("Insufficient cryptocurrency balance in wallet " + wallet.getId() + " to sell " + amount + " " + wallet.getCurrency().getSymbol()); // Новий Exception
            }
            wallet.setBalance(wallet.getBalance() - amount);
        }

        walletRepository.save(wallet);
        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toDto(savedTransaction); // Повертає DTO
    }
}