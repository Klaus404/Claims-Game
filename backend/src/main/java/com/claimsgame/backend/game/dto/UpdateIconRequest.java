package com.claimsgame.backend.game.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateIconRequest(@NotBlank String icon) {
}
