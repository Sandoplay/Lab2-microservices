package edu.levytskyi.lab2microservices.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "currencies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol; // BTC, ETH, etc.

    @Column(nullable = false)
    private String name;  // Bitcoin, Ethereum, etc.

    @Column(nullable = false)
    private Double currentPrice;  // Example: USD price

    @OneToMany(mappedBy = "currency") // Relationship needed for mapping back if necessary, but not strictly required by Wallet
    private List<Wallet> wallets;
}