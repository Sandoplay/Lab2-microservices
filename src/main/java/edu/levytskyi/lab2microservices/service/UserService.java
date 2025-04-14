package edu.levytskyi.lab2microservices.service;

import edu.levytskyi.lab2microservices.dto.UserCreateDTO;
import edu.levytskyi.lab2microservices.dto.UserDTO;
import edu.levytskyi.lab2microservices.model.User;
import edu.levytskyi.lab2microservices.exception.ResourceNotFoundException;
import edu.levytskyi.lab2microservices.mapper.UserMapper;
import edu.levytskyi.lab2microservices.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// ... other imports ...
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper; // Додано Mapper
    // private final PasswordEncoder passwordEncoder; // Для хешування паролів

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto).collect(Collectors.toList()); // Повертає DTO
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id).map(userMapper::toDto) // Повертає DTO
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)); // Використовує новий Exception
    }

    // Метод пошуку за ім'ям користувача (змінився тип повернення)
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::toDto) // Повертає DTO
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username)); // Використовує новий Exception
    }

    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) { // Приймає DTO
        userRepository.findByUsername(userCreateDTO.getUsername()).ifPresent(u -> { // Перевірка на дублікат
            throw new IllegalArgumentException("Username already exists: " + userCreateDTO.getUsername()); });

        User user = userMapper.toEntity(userCreateDTO); // Маппінг з DTO
        // user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword())); // Важливо хешувати!
        user.setPassword(userCreateDTO.getPassword()); // Тимчасово без хешування

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser); // Повертає DTO
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) { // Приймає DTO для оновлення
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Перевірка на конфлікт username при зміні
        if (!existingUser.getUsername().equals(userDTO.getUsername())) {
            userRepository.findByUsername(userDTO.getUsername()).ifPresent(u -> {
                throw new IllegalArgumentException("Username already exists: " + userDTO.getUsername()); });
        }
        // Оновлення тільки дозволених полів (пароль окремо!)
        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser); // Повертає DTO
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id) // Перевірка перед видаленням
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user); // Каскадне видалення спрацює для гаманців
    }
}