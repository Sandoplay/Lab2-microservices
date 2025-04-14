package edu.levytskyi.lab2microservices.controller;

import edu.levytskyi.lab2microservices.dto.UserCreateDTO; // Новий DTO
import edu.levytskyi.lab2microservices.dto.UserDTO; // Новий DTO
import edu.levytskyi.lab2microservices.service.UserService;
import jakarta.validation.Valid; // Додано
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated; // Додано
import org.springframework.web.bind.annotation.*;
// ... other imports ...
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated // Увімкнення валідації
public class UserController {

    private final UserService userService;

    @GetMapping // Повертає DTO
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}") // Повертає DTO
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/byUsername/{username}") // Повертає DTO
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PostMapping // Приймає CreateDTO, повертає DTO, використовує @Valid
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserDTO createdUser = userService.createUser(userCreateDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{id}") // Приймає та повертає DTO, використовує @Valid
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        if (userDTO.getId() != null && !userDTO.getId().equals(id)) { return ResponseEntity.badRequest().build(); } // Перевірка ID
        userDTO.setId(id);
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}") // Логіка не змінилась, але залишаємо для повноти
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}