package com.example.resource_server.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UserDTO {
    private String id;
    private String name;
    private String email;
    private String cpf; // Ou outro documento de identificação
    private String phone;
    private String address;
    private LocalDate birthDate;
}
