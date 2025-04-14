package edu.levytskyi.lab2microservices.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletCreateDTO {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Currency ID cannot be null")
    private Long currencyId;

    @NotNull(message = "Initial balance cannot be null")
    @PositiveOrZero(message = "Initial balance must be zero or positive")
    private Double initialBalance; // This usually represents the amount of crypto
}