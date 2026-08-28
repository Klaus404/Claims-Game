package com.claimsgame.backend.game.dao;

import com.claimsgame.backend.game.model.Game;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID> {
    Optional<Game> findByJoinCode(String joinCode);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Game> findLockedByJoinCode(String joinCode);
    boolean existsByJoinCode(String joinCode);
}
