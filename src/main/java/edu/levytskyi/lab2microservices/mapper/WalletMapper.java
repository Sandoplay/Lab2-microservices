package edu.levytskyi.lab2microservices.mapper;

import edu.levytskyi.lab2microservices.dto.WalletCreateDTO;
import edu.levytskyi.lab2microservices.dto.WalletDTO;
import edu.levytskyi.lab2microservices.model.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    WalletMapper INSTANCE = Mappers.getMapper(WalletMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(source = "initialBalance", target = "balance")
    Wallet toEntity(WalletCreateDTO walletCreateDTO);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "currency.id", target = "currencyId")
    @Mapping(source = "currency.symbol", target = "currencySymbol")
    WalletDTO toDto(Wallet wallet);

    List<WalletDTO> toDtoList(List<Wallet> wallets);
}