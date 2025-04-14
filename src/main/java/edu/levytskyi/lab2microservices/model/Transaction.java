package edu.levytskyi.lab2microservices.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
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
    private Double amount; // Amount of cryptocurrency

    @Column(nullable = false)
    private Double price; // Price per unit of cryptocurrency at the time of transaction

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public enum TransactionType {
        BUY, SELL
    }
}