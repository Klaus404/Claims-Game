package com.claimsgame.backend.game.dao;

import com.claimsgame.backend.game.model.Game;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID> {
    Optional<Game> findByJoinCode(String joinCode);
    boolean existsByJoinCode(String joinCode);
}
