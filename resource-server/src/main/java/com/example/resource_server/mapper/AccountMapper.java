package com.example.resource_server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.resource_server.dto.AccountDTO;
import com.example.resource_server.model.Account;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountDTO toDto(Account account);

    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "user", ignore = true)
    Account toEntity(AccountDTO dto);
}