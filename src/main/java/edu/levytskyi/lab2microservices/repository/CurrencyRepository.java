package edu.levytskyi.lab2microservices.repository;
/* @author Sandoplay
 * @project Lab1-miroservices
 * @class CurrencyRepository
 * @version 1.0.0
 * @since 25.03.2025 - 15.19
 */

import edu.levytskyi.lab2microservices.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findBySymbol(String symbol); // Пошук валюти за символом (BTC, ETH)
}