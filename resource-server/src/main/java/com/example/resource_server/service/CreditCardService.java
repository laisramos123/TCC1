package com.example.resource_server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.resource_server.model.CreditCard;
import com.example.resource_server.repository.CreditCardRepository;

import java.util.List;
import java.util.Map;

@Service
public class CreditCardService {

    @Autowired
    private CreditCardRepository creditCardRepository;

    public List<CreditCard> getCreditCards(String cpf) {
        return creditCardRepository.findByUserId(cpf);
    }

    public CreditCard getCreditCardById(String cardId, String cpf) {
        return creditCardRepository.findByCardIdAndUserId(cardId, cpf)
                .orElseThrow(() -> new RuntimeException("Cartão não encontrado"));
    }

    public Map<String, Object> getCreditCardDetails(String cardId, String cpf) {
        CreditCard card = getCreditCardById(cardId, cpf);

        return Map.of(
                "cardId", card.getCardId(),
                "cardNumber", card.getCardNumber(),
                "cardHolderName", card.getCardHolderName(),
                "brand", card.getBrand(),
                "creditLimit", card.getCreditLimit(),
                "availableLimit", card.getAvailableLimit(),
                "status", card.getStatus());
    }
}