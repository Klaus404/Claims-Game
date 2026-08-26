package com.claimsgame.backend.game.dto;

import com.claimsgame.backend.game.model.RoundStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoundResponse(UUID id, int roundNumber, Instant createdAt, RoundStatus status,
                            UUID claimerId, List<ScoreResponse> scores) {
    public record ScoreResponse(UUID playerId, String playerName, int handPoints,
                                int awardedPoints, boolean receivedStar, int starPenalty) {
    }
}
