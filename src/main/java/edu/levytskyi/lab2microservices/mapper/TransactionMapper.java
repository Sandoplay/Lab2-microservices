package edu.levytskyi.lab2microservices.mapper;

import edu.levytskyi.lab2microservices.dto.TransactionCreateDTO;
import edu.levytskyi.lab2microservices.dto.TransactionDTO;
import edu.levytskyi.lab2microservices.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "wallet", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    Transaction toEntity(TransactionCreateDTO transactionCreateDTO);

    @Mapping(source = "wallet.id", target = "walletId")
    @Mapping(source = "wallet.currency.symbol", target = "currencySymbol")
    TransactionDTO toDto(Transaction transaction);

    List<TransactionDTO> toDtoList(List<Transaction> transactions);
}