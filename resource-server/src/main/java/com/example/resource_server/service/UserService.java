package com.example.resource_server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.resource_server.dto.UserDTO;
import com.example.resource_server.exceptions.ResourceNotFoundException;
import com.example.resource_server.mapper.UserMapper;
import com.example.resource_server.model.User;
import com.example.resource_server.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserDTO findUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return userMapper.toDto(user);
    }
}
