package edu.levytskyi.lab2microservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.levytskyi.lab2microservices.dto.UserCreateDTO;
import edu.levytskyi.lab2microservices.dto.UserDTO;
import edu.levytskyi.lab2microservices.model.User;
import edu.levytskyi.lab2microservices.repository.CurrencyRepository;
import edu.levytskyi.lab2microservices.repository.TransactionRepository;
import edu.levytskyi.lab2microservices.repository.UserRepository;
import edu.levytskyi.lab2microservices.repository.WalletRepository; // Import WalletRepository
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rollback transactions after each test
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    // Inject other repositories needed for cleanup
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CurrencyRepository currencyRepository;


    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        // Clean up in the correct order before each test
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
        currencyRepository.deleteAll(); // Also clean currencies if they might interfere

        // Create some initial data
        testUser1 = User.builder().username("testuser1").email("test1@example.com").password("password").build();
        testUser2 = User.builder().username("testuser2").email("test2@example.com").password("password").build();
        testUser1 = userRepository.save(testUser1);
        testUser2 = userRepository.save(testUser2);
    }

    // --- Rest of the tests remain the same ---

    @Test
    void getAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is(testUser1.getUsername())))
                .andExpect(jsonPath("$[1].username", is(testUser2.getUsername())));
    }

    @Test
    void getUserById_Success() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testUser1.getId().intValue())))
                .andExpect(jsonPath("$.username", is(testUser1.getUsername())))
                .andExpect(jsonPath("$.email", is(testUser1.getEmail())));
    }

    @Test
    void getUserById_NotFound() throws Exception {
        long nonExistentId = 999L;
        mockMvc.perform(get("/api/users/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User not found with id: " + nonExistentId)));
    }

    @Test
    void getUserByUsername_Success() throws Exception {
        mockMvc.perform(get("/api/users/byUsername/{username}", testUser1.getUsername()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testUser1.getId().intValue())))
                .andExpect(jsonPath("$.username", is(testUser1.getUsername())));
    }

    @Test
    void getUserByUsername_NotFound() throws Exception {
        String nonExistentUsername = "nosuchuser";
        mockMvc.perform(get("/api/users/byUsername/{username}", nonExistentUsername))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User not found with username: " + nonExistentUsername)));
    }

    @Test
    void createUser_Success() throws Exception {
        UserCreateDTO newUserDTO = new UserCreateDTO("newuser", "newpassword123", "new@example.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is(newUserDTO.getUsername())))
                .andExpect(jsonPath("$.email", is(newUserDTO.getEmail())));
    }

    @Test
    void createUser_ValidationError() throws Exception {
        // Invalid email, short username
        UserCreateDTO invalidUserDTO = new UserCreateDTO("nu", "newpassword123", "invalid-email");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username", containsString("must be between 3 and 50 characters")))
                .andExpect(jsonPath("$.email", containsString("Email should be valid")));
    }

    @Test
    void createUser_UsernameConflict() throws Exception {
        UserCreateDTO conflictingUserDTO = new UserCreateDTO(testUser1.getUsername(), "password123", "conflict@example.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictingUserDTO)))
                .andExpect(status().isBadRequest()) // Or Internal Server Error depending on GlobalExceptionHandler setup for IllegalArgumentException
                .andExpect(jsonPath("$.message", containsString("Username already exists: " + testUser1.getUsername())));
    }


    @Test
    void updateUser_Success() throws Exception {
        UserDTO updateUserDTO = new UserDTO(testUser1.getId(), "updatedUsername", "updated@example.com");

        mockMvc.perform(put("/api/users/{id}", testUser1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testUser1.getId().intValue())))
                .andExpect(jsonPath("$.username", is(updateUserDTO.getUsername())))
                .andExpect(jsonPath("$.email", is(updateUserDTO.getEmail())));
    }

    @Test
    void updateUser_NotFound() throws Exception {
        long nonExistentId = 999L;
        UserDTO updateUserDTO = new UserDTO(nonExistentId, "updatedUsername", "updated@example.com");

        mockMvc.perform(put("/api/users/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User not found with id: " + nonExistentId)));
    }

    @Test
    void updateUser_UsernameConflict() throws Exception {
        // Try to update testUser1's username to testUser2's username
        UserDTO conflictingUpdateDTO = new UserDTO(testUser1.getId(), testUser2.getUsername(), "updateConflict@example.com");

        mockMvc.perform(put("/api/users/{id}", testUser1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictingUpdateDTO)))
                .andExpect(status().isBadRequest()) // Or Internal Server Error depending on GlobalExceptionHandler setup for IllegalArgumentException
                .andExpect(jsonPath("$.message", containsString("Username already exists: " + testUser2.getUsername())));
    }

    @Test
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser1.getId()))
                .andExpect(status().isNoContent());

        // Verify user is actually deleted
        mockMvc.perform(get("/api/users/{id}", testUser1.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_NotFound() throws Exception {
        long nonExistentId = 999L;
        mockMvc.perform(delete("/api/users/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User not found with id: " + nonExistentId)));
    }
}