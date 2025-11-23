package com.example.auth_server.repository;

import com.example.auth_server.model.Consent;
import com.example.auth_server.enums.ConsentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, String> {
    
    List<Consent> findByClientIdAndStatus(String clientId, ConsentStatus status);
    
    List<Consent> findByClientId(String clientId);
    
    @Query("SELECT c FROM Consent c WHERE c.status = :status AND c.expirationDate > :now")
    List<Consent> findActiveConsents(ConsentStatus status, LocalDateTime now);
    
    @Query("SELECT c FROM Consent c WHERE c.cpf = :cpf AND c.status = 'AUTHORIZED'")
    List<Consent> findActiveConsentsByCpf(String cpf);
    
    Optional<Consent> findByConsentIdAndClientId(String consentId, String clientId);
    
    @Query("SELECT COUNT(c) FROM Consent c WHERE c.status = :status")
    long countByStatus(ConsentStatus status);
}
