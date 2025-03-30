package edu.levytskyi.lab2microservices.service;
/* @author Sandoplay
 * @project Lab1-miroservices
 * @class as
 * @version 1.0.0
 * @since 25.03.2025 - 15.19
 */

import edu.levytskyi.lab2microservices.entity.User;
import edu.levytskyi.lab2microservices.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User createUser(User user) {
        //  Додати валідацію (наприклад, перевірку унікальності username)
        return userRepository.save(user);
    }

    public User updateUser(Long id, User updatedUser) {
        User user = getUserById(id);
        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        // Оновлення паролю потрібно робити окремим методом з хешуванням!
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    public Optional<User> findByUsername(String username) { //Метод для пошуку юзера
        return userRepository.findByUsername(username);
    }
}