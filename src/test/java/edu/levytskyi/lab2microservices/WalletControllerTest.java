package edu.levytskyi.lab2microservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.levytskyi.lab2microservices.dto.WalletCreateDTO;
import edu.levytskyi.lab2microservices.dto.WalletDTO;
import edu.levytskyi.lab2microservices.model.Currency;
import edu.levytskyi.lab2microservices.model.User;
import edu.levytskyi.lab2microservices.model.Wallet;
import edu.levytskyi.lab2microservices.repository.CurrencyRepository;
import edu.levytskyi.lab2microservices.repository.TransactionRepository; // Import TransactionRepository
import edu.levytskyi.lab2microservices.repository.UserRepository;
import edu.levytskyi.lab2microservices.repository.WalletRepository;
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
@Transactional
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    // Inject other repositories needed for cleanup
    @Autowired
    private TransactionRepository transactionRepository;


    private User testUser;
    private Currency testCurrencyBTC;
    private Currency testCurrencyETH;
    private Wallet testWalletBTC;
    private Wallet testWalletETH;

    @BeforeEach
    void setUp() {
        // Clean up in the correct order
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
        currencyRepository.deleteAll();


        // Create necessary entities
        testUser = User.builder().username("walletuser").email("wallet@example.com").password("password").build();
        testUser = userRepository.save(testUser);

        testCurrencyBTC = Currency.builder().symbol("BTC").name("Bitcoin").currentPrice(65000.0).build();
        testCurrencyETH = Currency.builder().symbol("ETH").name("Ethereum").currentPrice(3500.0).build();
        testCurrencyBTC = currencyRepository.save(testCurrencyBTC);
        testCurrencyETH = currencyRepository.save(testCurrencyETH);

        testWalletBTC = Wallet.builder().user(testUser).currency(testCurrencyBTC).balance(1.5).build();
        testWalletETH = Wallet.builder().user(testUser).currency(testCurrencyETH).balance(10.0).build();
        testWalletBTC = walletRepository.save(testWalletBTC);
        testWalletETH = walletRepository.save(testWalletETH);
    }

    // --- Rest of the tests remain the same ---

    @Test
    void getAllWallets() throws Exception {
        // Note: This might return wallets from other users if tests run concurrently without proper isolation,
        // but @Transactional should handle this. Let's assume we only have testUser's wallets.
        mockMvc.perform(get("/api/wallets"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].currencySymbol", is(testCurrencyBTC.getSymbol())))
                .andExpect(jsonPath("$[1].currencySymbol", is(testCurrencyETH.getSymbol())));
    }

    @Test
    void getWalletById_Success() throws Exception {
        mockMvc.perform(get("/api/wallets/{id}", testWalletBTC.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testWalletBTC.getId().intValue())))
                .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.currencyId", is(testCurrencyBTC.getId().intValue())))
                .andExpect(jsonPath("$.currencySymbol", is(testCurrencyBTC.getSymbol())))
                .andExpect(jsonPath("$.balance", is(testWalletBTC.getBalance())));
    }

    @Test
    void getWalletById_NotFound() throws Exception {
        long nonExistentId = 999L;
        mockMvc.perform(get("/api/wallets/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Wallet not found with id: " + nonExistentId)));
    }

    @Test
    void getWalletsByUserId_Success() throws Exception {
        mockMvc.perform(get("/api/wallets/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$[1].userId", is(testUser.getId().intValue())));
    }

    @Test
    void getWalletsByUserId_UserNotFound() throws Exception {
        long nonExistentUserId = 998L;
        mockMvc.perform(get("/api/wallets/user/{userId}", nonExistentUserId))
                .andExpect(status().isNotFound()) // Service throws ResourceNotFoundException for user
                .andExpect(jsonPath("$.message", containsString("User not found with id: " + nonExistentUserId)));
    }

    @Test
    void createWallet_Success() throws Exception {
        // Create a new currency for the new wallet
        Currency testCurrencyLTC = Currency.builder().symbol("LTC").name("Litecoin").currentPrice(180.0).build();
        testCurrencyLTC = currencyRepository.save(testCurrencyLTC);

        WalletCreateDTO newWalletDTO = new WalletCreateDTO(testUser.getId(), testCurrencyLTC.getId(), 5.0);

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newWalletDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.currencyId", is(testCurrencyLTC.getId().intValue())))
                .andExpect(jsonPath("$.currencySymbol", is(testCurrencyLTC.getSymbol())))
                .andExpect(jsonPath("$.balance", is(newWalletDTO.getInitialBalance())));
    }

    @Test
    void createWallet_ValidationError() throws Exception {
        WalletCreateDTO invalidWalletDTO = new WalletCreateDTO(null, null, -1.0); // Null IDs, negative balance

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidWalletDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.userId", containsString("cannot be null")))
                .andExpect(jsonPath("$.currencyId", containsString("cannot be null")))
                .andExpect(jsonPath("$.initialBalance", containsString("must be zero or positive")));
    }

    @Test
    void createWallet_UserNotFound() throws Exception {
        long nonExistentUserId = 998L;
        WalletCreateDTO walletDTO = new WalletCreateDTO(nonExistentUserId, testCurrencyBTC.getId(), 1.0);

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User not found with id: " + nonExistentUserId)));
    }

    @Test
    void createWallet_CurrencyNotFound() throws Exception {
        long nonExistentCurrencyId = 997L;
        WalletCreateDTO walletDTO = new WalletCreateDTO(testUser.getId(), nonExistentCurrencyId, 1.0);

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Currency not found with id: " + nonExistentCurrencyId)));
    }

    @Test
    void createWallet_DuplicateWalletForUserCurrency() throws Exception {
        // Try to create another BTC wallet for the same user
        WalletCreateDTO duplicateWalletDTO = new WalletCreateDTO(testUser.getId(), testCurrencyBTC.getId(), 2.0);

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateWalletDTO)))
                .andExpect(status().isBadRequest()) // Or Internal Server Error depending on GlobalExceptionHandler setup for IllegalArgumentException
                .andExpect(jsonPath("$.message", containsString("User already has a wallet for currency: " + testCurrencyBTC.getSymbol())));
    }

    @Test
    void deleteWallet_Success() throws Exception {
        mockMvc.perform(delete("/api/wallets/{id}", testWalletBTC.getId()))
                .andExpect(status().isNoContent());

        // Verify wallet is actually deleted
        mockMvc.perform(get("/api/wallets/{id}", testWalletBTC.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteWallet_NotFound() throws Exception {
        long nonExistentId = 999L;
        mockMvc.perform(delete("/api/wallets/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Wallet not found with id: " + nonExistentId)));
    }
}