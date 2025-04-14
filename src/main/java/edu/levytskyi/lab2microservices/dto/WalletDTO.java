package edu.levytskyi.lab2microservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {
    private Long id;
    private Long userId;
    private Long currencyId;
    private String currencySymbol; // Useful info
    private Double balance;
}