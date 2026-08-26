package com.claimsgame.backend.game.dto;

import jakarta.validation.constraints.NotBlank;

public record AddBotRequest(@NotBlank String name) {
}
