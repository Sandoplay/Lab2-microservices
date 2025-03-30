package edu.levytskyi.lab2microservices.repository;
/* @author Sandoplay
 * @project Lab1-microservices
 * @class UserRepository
 * @version 1.0.0
 * @since 25.03.2025 - 15.14
 */

import edu.levytskyi.lab2microservices.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // Додаємо метод пошуку за ім'ям користувача
}