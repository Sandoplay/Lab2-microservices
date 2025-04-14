package edu.levytskyi.lab2microservices.mapper;

import edu.levytskyi.lab2microservices.dto.UserCreateDTO;
import edu.levytskyi.lab2microservices.dto.UserDTO;
import edu.levytskyi.lab2microservices.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "wallets", ignore = true)
    User toEntity(UserCreateDTO userCreateDTO);

    UserDTO toDto(User user);
    List<UserDTO> toDtoList(List<User> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "wallets", ignore = true)
    void updateEntityFromDto(UserDTO dto, @MappingTarget User entity);
}