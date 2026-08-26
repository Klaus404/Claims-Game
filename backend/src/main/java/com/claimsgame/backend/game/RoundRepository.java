package com.claimsgame.backend.game;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RoundRepository extends JpaRepository<Round, UUID> {
    Optional<Round> findByIdAndGameId(UUID id, UUID gameId);
}
