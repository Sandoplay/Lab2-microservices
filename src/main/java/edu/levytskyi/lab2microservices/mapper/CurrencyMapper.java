package edu.levytskyi.lab2microservices.mapper;

import edu.levytskyi.lab2microservices.dto.CurrencyDTO;
import edu.levytskyi.lab2microservices.model.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import java.util.List;


@Mapper(componentModel = "spring")
public interface CurrencyMapper {

    CurrencyMapper INSTANCE = Mappers.getMapper(CurrencyMapper.class);

    Currency toEntity(CurrencyDTO currencyDTO);
    CurrencyDTO toDto(Currency currency);
    List<CurrencyDTO> toDtoList(List<Currency> currencies);
    void updateEntityFromDto(CurrencyDTO dto, @MappingTarget Currency entity);
}