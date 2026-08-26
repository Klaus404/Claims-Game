package com.claimsgame.backend.game.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record CreateRoundRequest(@NotEmpty List<@Valid HandScoreRequest> scores, UUID claimerId) {
}
