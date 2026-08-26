package com.claimsgame.backend.game.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record HandScoreRequest(@NotNull UUID playerId, @Min(0) int handPoints) {
}
