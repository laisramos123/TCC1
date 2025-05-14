package com.example.resource_server.mapper;

import org.mapstruct.Mapper;

import com.example.resource_server.dto.UserDTO;
import com.example.resource_server.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto(User user);

    User toEntity(UserDTO dto);
}
