package edu.levytskyi.lab2microservices.entity;
/* @author Sandoplay
 * @project Lab1-miroservices
 * @class Transaction
 * @version 1.0.0
 * @since 25.03.2025 - 15.08
 */

// Transaction.java

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // BUY, SELL

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Double price; // Ціна за одиницю криптовалюти на момент транзакції

    @Column(nullable = false)
    private LocalDateTime timestamp;


    public enum TransactionType {
        BUY, SELL
    }
}