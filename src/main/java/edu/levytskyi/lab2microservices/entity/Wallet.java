package edu.levytskyi.lab2microservices.entity;
/* @author Sandoplay
 * @project Lab1-miroservices
 * @class Wallet
 * @version 1.0.0
 * @since 25.03.2025 - 15.07
 */

// Wallet.java

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wallets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Column(nullable = false)
    private Double balance;
}