package com.vaultbank.auth.repository;

import com.vaultbank.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    // Busca por hash do CPF (sem precisar descriptografar)
    boolean existsByDocumentHash(String documentHash);
}
