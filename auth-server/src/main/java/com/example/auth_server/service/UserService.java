package com.example.auth_server.service;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.auth_server.model.User;
import com.example.auth_server.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("  TENTATIVA DE LOGIN - Usuario: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error(" USUARIO NÃO ENCONTRADO: {}", username);
                    return new UsernameNotFoundException("Usuario não encontrado: " + username);
                });

        logger.info(" USUARIO ENCONTRADO: {} | Enabled: {} | Password hash: {}",
                user.getUsername(), user.isEnabled(), user.getPassword());
        logger.info("AUTHORITIES do usuario: {}", user.getAuthorities());

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                user.getAuthorities().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet()));

        logger.info(" UserDetails criado - Username: {} | Enabled: {} | Authorities: {}",
                userDetails.getUsername(), userDetails.isEnabled(), userDetails.getAuthorities());

        return userDetails;
    }
}