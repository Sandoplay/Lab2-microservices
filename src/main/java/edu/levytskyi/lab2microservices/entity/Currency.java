package edu.levytskyi.lab2microservices.entity;
/* @author Sandoplay
 * @project Lab1-miroservices
 * @class Currency
 * @version 1.0.0
 * @since 25.03.2025 - 15.10
 */

// Currency.java

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "currencies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol; // BTC, ETH, etc.

    @Column(nullable = false)
    private String name;  // Bitcoin, Ethereum, etc.

    //  Для спрощення, додамо поточний курс прямо тут.
    //  В реальному застосунку курс потрібно отримувати з API біржі.
    @Column(nullable = false)
    private Double currentPrice;  //  Приклад:  USD
}