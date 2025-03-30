package edu.levytskyi.lab2microservices.repository;
/* @author Sandoplay
 * @project Lab1-miroservices
 * @class balls
 * @version 1.0.0
 * @since 25.03.2025 - 15.18
 */
import edu.levytskyi.lab2microservices.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByWalletId(Long walletId); // Пошук транзакцій по гаманцю
}