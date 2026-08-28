package com.claimsgame.backend.game.controller;

import com.claimsgame.backend.game.dto.CreateGameRequest;
import com.claimsgame.backend.game.dto.CreateRoundRequest;
import com.claimsgame.backend.game.dto.AddBotRequest;
import com.claimsgame.backend.game.dto.GameResponse;
import com.claimsgame.backend.game.dto.JoinGameRequest;
import com.claimsgame.backend.game.dto.RoundResponse;
import com.claimsgame.backend.game.service.GameService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService service;

    public GameController(GameService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameResponse create(@Valid @RequestBody CreateGameRequest request) { return service.create(request); }

    @PostMapping("/{code}/players")
    public GameResponse join(@PathVariable String code, @Valid @RequestBody JoinGameRequest request) { return service.join(code, request); }

    @GetMapping("/{code}")
    public GameResponse get(@PathVariable String code) { return service.get(code); }

    @PostMapping("/{code}/start")
    public GameResponse start(@PathVariable String code, @RequestHeader("X-Player-Id") UUID ownerId) { return service.start(code, ownerId); }

    @PostMapping("/{code}/end")
    public GameResponse end(@PathVariable String code, @RequestHeader("X-Player-Id") UUID ownerId) { return service.end(code, ownerId); }

    @PostMapping("/{code}/leave")
    public GameResponse leave(@PathVariable String code, @RequestHeader("X-Player-Id") UUID playerId) { return service.leave(code, playerId); }

    @PostMapping("/{code}/bots")
    public GameResponse addBot(@PathVariable String code, @RequestHeader("X-Player-Id") UUID ownerId, @Valid @RequestBody AddBotRequest request) { return service.addBot(code, ownerId, request); }

    @PostMapping("/{code}/rounds")
    @ResponseStatus(HttpStatus.CREATED)
    public RoundResponse createRound(@PathVariable String code, @RequestHeader("X-Player-Id") UUID ownerId, @Valid @RequestBody CreateRoundRequest request) { return service.createRound(code, ownerId, request); }

    @PostMapping("/{code}/rounds/{roundId}/resolve")
    public RoundResponse resolve(@PathVariable String code, @PathVariable UUID roundId, @RequestHeader("X-Player-Id") UUID ownerId) { return service.resolve(code, ownerId, roundId); }

    @GetMapping("/{code}/rounds")
    public List<RoundResponse> history(@PathVariable String code) { return service.history(code); }

    @DeleteMapping("/{code}/rounds/last")
    public GameResponse clearLastRound(@PathVariable String code, @RequestHeader("X-Player-Id") UUID ownerId) { return service.clearLastRound(code, ownerId); }

    @PutMapping("/{code}/rounds/last")
    public RoundResponse editLastRound(@PathVariable String code, @RequestHeader("X-Player-Id") UUID ownerId, @Valid @RequestBody CreateRoundRequest request) { return service.editLastRound(code, ownerId, request); }
}
