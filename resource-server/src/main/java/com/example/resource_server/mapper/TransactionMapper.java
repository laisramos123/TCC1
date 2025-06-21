package com.example.resource_server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.resource_server.dto.TransactionDTO;
import com.example.resource_server.model.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(source = "account.id", target = "accountId")
    TransactionDTO toDto(Transaction transaction);

    @Mapping(target = "account", ignore = true)
    Transaction toEntity(TransactionDTO dto);
}
