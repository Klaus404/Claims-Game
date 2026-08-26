package com.claimsgame.backend.game.api;

import jakarta.validation.constraints.NotBlank;

public record CreateGameRequest(@NotBlank String playerName) {
}
