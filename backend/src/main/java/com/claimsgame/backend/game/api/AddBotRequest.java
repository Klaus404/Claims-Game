package com.claimsgame.backend.game.api;

import jakarta.validation.constraints.NotBlank;

public record AddBotRequest(@NotBlank String name) {
}
