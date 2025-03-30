package edu.levytskyi.lab2microservices.entity;
/* @author Sandoplay
 * @project Lab1-miroservices
 * @class User
 * @version 1.0.0
 * @since 25.03.2025 - 15.03
 */

// User.java

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; // Паролі потрібно хешувати

    private String email; // Додано для приколу

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wallet> wallets;
}



