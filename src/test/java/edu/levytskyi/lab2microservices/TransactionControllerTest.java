package edu.levytskyi.lab2microservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.levytskyi.lab2microservices.dto.TransactionCreateDTO;
import edu.levytskyi.lab2microservices.model.*;
import edu.levytskyi.lab2microservices.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private User testUser;
    private Currency testCurrencyBTC;
    private Wallet testWalletBTC;
    private Transaction testTransactionBuy;
    private Transaction testTransactionSell;

    @BeforeEach
    void setUp() {
        // Clean up in the correct order
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
        currencyRepository.deleteAll();

        // Create necessary entities
        testUser = User.builder().username("txuser").email("tx@example.com").password("password").build();
        testUser = userRepository.save(testUser);

        testCurrencyBTC = Currency.builder().symbol("BTC").name("Bitcoin").currentPrice(65000.0).build();
        testCurrencyBTC = currencyRepository.save(testCurrencyBTC);

        testWalletBTC = Wallet.builder().user(testUser).currency(testCurrencyBTC).balance(1.0).build(); // Start with 1 BTC
        testWalletBTC = walletRepository.save(testWalletBTC);

        // Create some initial transactions
        testTransactionBuy = Transaction.builder()
                .wallet(testWalletBTC)
                .type(Transaction.TransactionType.BUY)
                .amount(0.5)
                .price(64000.0)
                .timestamp(LocalDateTime.now().minusDays(1))
                .build();
        testTransactionSell = Transaction.builder()
                .wallet(testWalletBTC)
                .type(Transaction.TransactionType.SELL)
                .amount(0.2)
                .price(65500.0)
                .timestamp(LocalDateTime.now().minusHours(5))
                .build();
        testTransactionBuy = transactionRepository.save(testTransactionBuy);
        testTransactionSell = transactionRepository.save(testTransactionSell);
    }

    // --- Rest of the tests remain the same ---

    @Test
    void getAllTransactions() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type", is(testTransactionBuy.getType().toString())))
                .andExpect(jsonPath("$[1].type", is(testTransactionSell.getType().toString())));
    }

    @Test
    void getTransactionById_Success() throws Exception {
        mockMvc.perform(get("/api/transactions/{id}", testTransactionBuy.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testTransactionBuy.getId().intValue())))
                .andExpect(jsonPath("$.walletId", is(testWalletBTC.getId().intValue())))
                .andExpect(jsonPath("$.currencySymbol", is(testCurrencyBTC.getSymbol())))
                .andExpect(jsonPath("$.type", is(testTransactionBuy.getType().toString())))
                .andExpect(jsonPath("$.amount", is(testTransactionBuy.getAmount())))
                .andExpect(jsonPath("$.price", is(testTransactionBuy.getPrice())));
    }

    @Test
    void getTransactionById_NotFound() throws Exception {
        long nonExistentId = 9999L;
        mockMvc.perform(get("/api/transactions/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Transaction not found with id: " + nonExistentId)));
    }

    @Test
    void getTransactionsByWalletId_Success() throws Exception {
        mockMvc.perform(get("/api/transactions/wallet/{walletId}", testWalletBTC.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].walletId", is(testWalletBTC.getId().intValue())))
                .andExpect(jsonPath("$[1].walletId", is(testWalletBTC.getId().intValue())));
    }

    @Test
    void getTransactionsByWalletId_WalletNotFound() throws Exception {
        long nonExistentWalletId = 9998L;
        mockMvc.perform(get("/api/transactions/wallet/{walletId}", nonExistentWalletId))
                .andExpect(status().isNotFound()) // Service checks if wallet exists
                .andExpect(jsonPath("$.message", containsString("Wallet not found with id: " + nonExistentWalletId)));
    }

     @Test
    void getTransactionsByUserId_Success() throws Exception {
        // Create another user and wallet/transaction to ensure filtering works
        User otherUser = userRepository.save(User.builder().username("otheruser").email("other@e.com").password("pass").build());
        Wallet otherWallet = walletRepository.save(Wallet.builder().user(otherUser).currency(testCurrencyBTC).balance(0.1).build());
        transactionRepository.save(Transaction.builder().wallet(otherWallet).type(Transaction.TransactionType.BUY).amount(0.1).price(60000.0).timestamp(LocalDateTime.now()).build());

        mockMvc.perform(get("/api/transactions/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2))) // Should only return transactions for testUser
                .andExpect(jsonPath("$[0].walletId", is(testWalletBTC.getId().intValue())))
                .andExpect(jsonPath("$[1].walletId", is(testWalletBTC.getId().intValue())));
    }

    @Test
    void createTransaction_Buy_Success() throws Exception {
        double buyAmount = 0.3;
        TransactionCreateDTO buyDTO = new TransactionCreateDTO(testWalletBTC.getId(), Transaction.TransactionType.BUY, buyAmount);
        double initialBalance = testWalletBTC.getBalance();

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buyDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.walletId", is(testWalletBTC.getId().intValue())))
                .andExpect(jsonPath("$.currencySymbol", is(testCurrencyBTC.getSymbol())))
                .andExpect(jsonPath("$.type", is(Transaction.TransactionType.BUY.toString())))
                .andExpect(jsonPath("$.amount", is(buyAmount)))
                .andExpect(jsonPath("$.price", is(testCurrencyBTC.getCurrentPrice()))) // Price set by service
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        // Verify balance update
        Wallet updatedWallet = walletRepository.findById(testWalletBTC.getId()).orElseThrow();
        assertEquals(initialBalance + buyAmount, updatedWallet.getBalance(), 0.001);
    }

    @Test
    void createTransaction_Sell_Success() throws Exception {
        double sellAmount = 0.1;
        TransactionCreateDTO sellDTO = new TransactionCreateDTO(testWalletBTC.getId(), Transaction.TransactionType.SELL, sellAmount);
        double initialBalance = testWalletBTC.getBalance();

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sellDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.walletId", is(testWalletBTC.getId().intValue())))
                .andExpect(jsonPath("$.currencySymbol", is(testCurrencyBTC.getSymbol())))
                .andExpect(jsonPath("$.type", is(Transaction.TransactionType.SELL.toString())))
                .andExpect(jsonPath("$.amount", is(sellAmount)))
                .andExpect(jsonPath("$.price", is(testCurrencyBTC.getCurrentPrice())));

        // Verify balance update
        Wallet updatedWallet = walletRepository.findById(testWalletBTC.getId()).orElseThrow();
        assertEquals(initialBalance - sellAmount, updatedWallet.getBalance(), 0.001);
    }

    @Test
    void createTransaction_Sell_InsufficientFunds() throws Exception {
        double sellAmount = testWalletBTC.getBalance() + 0.1; // Try to sell more than available
        TransactionCreateDTO sellDTO = new TransactionCreateDTO(testWalletBTC.getId(), Transaction.TransactionType.SELL, sellAmount);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sellDTO)))
                .andExpect(status().isBadRequest()) // Handled by InsufficientFundsException
                .andExpect(jsonPath("$.message", containsString("Insufficient cryptocurrency balance")));
    }

    @Test
    void createTransaction_ValidationError() throws Exception {
        TransactionCreateDTO invalidDTO = new TransactionCreateDTO(null, null, -0.5); // Null walletId, null type, negative amount

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.walletId", containsString("cannot be null")))
                .andExpect(jsonPath("$.type", containsString("cannot be null")))
                .andExpect(jsonPath("$.amount", containsString("must be positive")));
    }

    @Test
    void createTransaction_WalletNotFound() throws Exception {
        long nonExistentWalletId = 9998L;
        TransactionCreateDTO dto = new TransactionCreateDTO(nonExistentWalletId, Transaction.TransactionType.BUY, 0.1);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Wallet not found with id: " + nonExistentWalletId)));
    }
}