package edu.levytskyi.lab2microservices.repository;
/* @author Sandoplay
 * @project Lab1-miroservices
 * @class WallerRepository
 * @version 1.0.0
 * @since 25.03.2025 - 15.15
 */

import edu.levytskyi.lab2microservices.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByUserId(Long userId); //Пошук гаманців користувача
    Optional<Wallet> findByUserIdAndCurrencyId(Long userId, Long currencyId); // Check if user already has a wallet for this currency
}