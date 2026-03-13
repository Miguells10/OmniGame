package com.omnigame.infrastructure.persistence;

import com.omnigame.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByAuthUid(UUID authUid);
    Optional<User> findByEmail(String email);
}
