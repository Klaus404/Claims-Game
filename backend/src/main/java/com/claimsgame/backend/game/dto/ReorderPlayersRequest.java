package com.claimsgame.backend.game.dto;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

public record ReorderPlayersRequest(@Valid List<UUID> playerIds) {
}
