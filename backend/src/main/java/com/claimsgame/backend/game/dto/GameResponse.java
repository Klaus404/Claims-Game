package com.claimsgame.backend.game.dto;

import com.claimsgame.backend.game.model.GameStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GameResponse(UUID id, String joinCode, Instant createdAt, GameStatus status, UUID ownerId,
                           List<PlayerResponse> players, UUID winnerId) {
    public record PlayerResponse(UUID id, String name, int playingOrder, int totalScore,
                                 int starStreak, boolean eliminated, boolean bot) {
    }
}
