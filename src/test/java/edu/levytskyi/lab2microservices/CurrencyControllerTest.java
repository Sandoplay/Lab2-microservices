package edu.levytskyi.lab2microservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.levytskyi.lab2microservices.dto.CurrencyDTO;
import edu.levytskyi.lab2microservices.model.Currency;
import edu.levytskyi.lab2microservices.repository.CurrencyRepository;
import edu.levytskyi.lab2microservices.repository.TransactionRepository; // Import other repos
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
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CurrencyRepository currencyRepository;

    // Inject other repositories needed for cleanup
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserRepository userRepository;


    private Currency testCurrencyBTC;
    private Currency testCurrencyETH;

    @BeforeEach
    void setUp() {
        // Clean up in the correct order
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
        currencyRepository.deleteAll();

        testCurrencyBTC = Currency.builder().symbol("BTC").name("Bitcoin").currentPrice(65000.0).build();
        testCurrencyETH = Currency.builder().symbol("ETH").name("Ethereum").currentPrice(3500.0).build();
        testCurrencyBTC = currencyRepository.save(testCurrencyBTC);
        testCurrencyETH = currencyRepository.save(testCurrencyETH);
    }

    // --- Rest of the tests remain the same ---

    @Test
    void getAllCurrencies() throws Exception {
        mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].symbol", is(testCurrencyBTC.getSymbol())))
                .andExpect(jsonPath("$[1].symbol", is(testCurrencyETH.getSymbol())));
    }

    @Test
    void getCurrencyById_Success() throws Exception {
        mockMvc.perform(get("/api/currencies/{id}", testCurrencyBTC.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testCurrencyBTC.getId().intValue())))
                .andExpect(jsonPath("$.symbol", is(testCurrencyBTC.getSymbol())))
                .andExpect(jsonPath("$.name", is(testCurrencyBTC.getName())));
    }

    @Test
    void getCurrencyById_NotFound() throws Exception {
        long nonExistentId = 999L;
        mockMvc.perform(get("/api/currencies/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Currency not found with id: " + nonExistentId)));
    }

    @Test
    void getCurrencyBySymbol_Success() throws Exception {
        mockMvc.perform(get("/api/currencies/symbol/{symbol}", testCurrencyBTC.getSymbol()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testCurrencyBTC.getId().intValue())))
                .andExpect(jsonPath("$.symbol", is(testCurrencyBTC.getSymbol())));
    }

    @Test
    void getCurrencyBySymbol_NotFound() throws Exception {
        String nonExistentSymbol = "XYZ";
        mockMvc.perform(get("/api/currencies/symbol/{symbol}", nonExistentSymbol))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Currency not found with symbol: " + nonExistentSymbol)));
    }

    @Test
    void createCurrency_Success() throws Exception {
        CurrencyDTO newCurrencyDTO = new CurrencyDTO(null, "LTC", "Litecoin", 180.0);

        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCurrencyDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.symbol", is(newCurrencyDTO.getSymbol().toUpperCase()))) // Service converts to uppercase
                .andExpect(jsonPath("$.name", is(newCurrencyDTO.getName())))
                .andExpect(jsonPath("$.currentPrice", is(newCurrencyDTO.getCurrentPrice()))); // Price might be overwritten by service logic if symbol exists in fake map
    }

    @Test
    void createCurrency_ValidationError() throws Exception {
        CurrencyDTO invalidCurrencyDTO = new CurrencyDTO(null, "S", "", -10.0); // Short symbol, blank name, negative price

        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCurrencyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.symbol", containsString("must be between 2 and 10 characters")))
                .andExpect(jsonPath("$.name", containsString("cannot be blank")))
                .andExpect(jsonPath("$.currentPrice", containsString("must be zero or positive")));
    }

     @Test
    void createCurrency_SymbolConflict() throws Exception {
        CurrencyDTO conflictingCurrencyDTO = new CurrencyDTO(null, testCurrencyBTC.getSymbol(), "Bitcoin Clone", 100.0);

        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictingCurrencyDTO)))
                .andExpect(status().isBadRequest()) // Or Internal Server Error depending on GlobalExceptionHandler setup for IllegalArgumentException
                .andExpect(jsonPath("$.message", containsString("Currency symbol already exists: " + testCurrencyBTC.getSymbol())));
    }


    @Test
    void updateCurrency_Success() throws Exception {
        CurrencyDTO updateCurrencyDTO = new CurrencyDTO(testCurrencyBTC.getId(), "BTC", "Bitcoin Updated", 66000.0);

        mockMvc.perform(put("/api/currencies/{id}", testCurrencyBTC.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCurrencyDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testCurrencyBTC.getId().intValue())))
                .andExpect(jsonPath("$.name", is(updateCurrencyDTO.getName())))
                .andExpect(jsonPath("$.currentPrice", is(updateCurrencyDTO.getCurrentPrice()))); // Price might be overwritten by service logic
    }

    @Test
    void updateCurrency_NotFound() throws Exception {
        long nonExistentId = 999L;
        CurrencyDTO updateCurrencyDTO = new CurrencyDTO(nonExistentId, "XYZ", "NonExistent", 1.0);

        mockMvc.perform(put("/api/currencies/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCurrencyDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Currency not found with id: " + nonExistentId)));
    }

    @Test
    void updateCurrency_SymbolConflict() throws Exception {
        // Try to update BTC's symbol to ETH's symbol
        CurrencyDTO conflictingUpdateDTO = new CurrencyDTO(testCurrencyBTC.getId(), testCurrencyETH.getSymbol(), "BTC as ETH?", 1.0);

        mockMvc.perform(put("/api/currencies/{id}", testCurrencyBTC.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictingUpdateDTO)))
                .andExpect(status().isBadRequest()) // Or Internal Server Error depending on GlobalExceptionHandler setup for IllegalArgumentException
                .andExpect(jsonPath("$.message", containsString("Currency symbol already exists: " + testCurrencyETH.getSymbol())));
    }


    @Test
    void deleteCurrency_Success() throws Exception {
        mockMvc.perform(delete("/api/currencies/{id}", testCurrencyBTC.getId()))
                .andExpect(status().isNoContent());

        // Verify currency is actually deleted
        mockMvc.perform(get("/api/currencies/{id}", testCurrencyBTC.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCurrency_NotFound() throws Exception {
        long nonExistentId = 999L;
        mockMvc.perform(delete("/api/currencies/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Currency not found with id: " + nonExistentId)));
    }

    @Test
    void updateCurrencyPrice_Success() throws Exception {
        double newPrice = 70000.0;
        mockMvc.perform(put("/api/currencies/update-price/{symbol}", testCurrencyBTC.getSymbol())
                        .param("newPrice", String.valueOf(newPrice)))
                .andExpect(status().isOk())
                .andExpect(content().string("Price for " + testCurrencyBTC.getSymbol() + " updated to " + newPrice));

        // Verify price updated in DB
        mockMvc.perform(get("/api/currencies/{id}", testCurrencyBTC.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPrice", is(newPrice)));
    }

    @Test
    void updateCurrencyPrice_NotFound() throws Exception {
        String nonExistentSymbol = "XYZ";
        double newPrice = 10.0;
        mockMvc.perform(put("/api/currencies/update-price/{symbol}", nonExistentSymbol)
                        .param("newPrice", String.valueOf(newPrice)))
                .andExpect(status().isNotFound())
                 .andExpect(jsonPath("$.message", containsString("Currency not found with symbol: " + nonExistentSymbol)));
    }

    @Test
    void updateCurrencyPrice_NegativePrice() throws Exception {
        double negativePrice = -50.0;
        mockMvc.perform(put("/api/currencies/update-price/{symbol}", testCurrencyBTC.getSymbol())
                        .param("newPrice", String.valueOf(negativePrice)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Price cannot be negative.")); // Assuming controller handles this directly
    }
}