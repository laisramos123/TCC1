package com.example.resource_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.resource_server.model.CreditCard;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, String> {

    List<CreditCard> findByUserId(String userId);

    Optional<CreditCard> findByCardIdAndUserId(String cardId, String userId);
}