package com.example.auth_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.auth_server.model.ConsentRecord;

@Repository
public interface ConsentRepository extends JpaRepository<ConsentRecord, String> {

    @Query("SELECT c FROM ConsentRecord c WHERE c.userId = :userId AND c.clientId = :clientId " +
            "AND c.status = 'ACTIVE' AND c.expiresAt > CURRENT_TIMESTAMP " +
            "ORDER BY c.createdAt DESC")
    ConsentRecord findActiveConsent(@Param("userId") String userId,
            @Param("clientId") String clientId);
}
