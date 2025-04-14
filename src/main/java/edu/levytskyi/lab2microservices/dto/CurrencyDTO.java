package edu.levytskyi.lab2microservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDTO {

    private Long id;

    @NotBlank(message = "Symbol cannot be blank")
    @Size(min = 2, max = 10, message = "Symbol must be between 2 and 10 characters")
    private String symbol; // BTC, ETH, etc.

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    private String name;  // Bitcoin, Ethereum, etc.

    @NotNull(message = "Current price cannot be null")
    @PositiveOrZero(message = "Current price must be zero or positive")
    private Double currentPrice;
}