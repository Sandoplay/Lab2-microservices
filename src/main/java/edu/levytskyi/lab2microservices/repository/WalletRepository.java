package edu.levytskyi.lab2microservices.repository;
/* @author Sandoplay
 * @project Lab1-miroservices
 * @class WallerRepository
 * @version 1.0.0
 * @since 25.03.2025 - 15.15
 */

import edu.levytskyi.lab2microservices.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByUserId(Long userId); //Пошук гаманців користувача
}