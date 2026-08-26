package com.claimsgame.backend.game;

import com.claimsgame.backend.game.api.CreateGameRequest;
import com.claimsgame.backend.game.api.CreateRoundRequest;
import com.claimsgame.backend.game.api.GameResponse;
import com.claimsgame.backend.game.api.JoinGameRequest;
import com.claimsgame.backend.game.api.AddBotRequest;
import com.claimsgame.backend.game.api.RoundResponse;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final GameRepository games;
    private final RoundRepository rounds;

    public GameService(GameRepository games, RoundRepository rounds) {
        this.games = games;
        this.rounds = rounds;
    }

    @Transactional
    public GameResponse create(CreateGameRequest request) {
        String code;
        do {
            code = randomCode();
        } while (games.existsByJoinCode(code));
        Game game = new Game(code);
        Player owner = new Player(cleanName(request.playerName()), 0);
        game.addPlayer(owner);
        games.save(game);
        game.setOwnerId(owner.getId());
        return view(games.save(game));
    }

    @Transactional
    public GameResponse join(String code, JoinGameRequest request) {
        Game game = getGame(code);
        if (game.getStatus() != GameStatus.WAITING) throw new IllegalStateException("Game has already started");
        if (game.getPlayers().size() >= 6) throw new IllegalStateException("Game is full");
        String name = cleanName(request.playerName());
        if (game.getPlayers().stream().anyMatch(p -> p.getName().equalsIgnoreCase(name))) {
            throw new IllegalArgumentException("Player name is already in use");
        }
        game.addPlayer(new Player(name, game.getPlayers().size()));
        return view(game);
    }

    @Transactional
    public GameResponse start(String code, UUID ownerId) {
        Game game = getGame(code);
        authorizeOwner(game, ownerId);
        if (game.getStatus() != GameStatus.WAITING) throw new IllegalStateException("Game cannot be started");
        if (game.getPlayers().size() < 2) throw new IllegalStateException("At least 2 players are required");
        game.setStatus(GameStatus.IN_PROGRESS);
        return view(game);
    }

    @Transactional
    public GameResponse addBot(String code, UUID ownerId, AddBotRequest request) {
        Game game = getGame(code);
        authorizeOwner(game, ownerId);
        if (game.getStatus() != GameStatus.WAITING) throw new IllegalStateException("Game has already started");
        if (game.getPlayers().size() >= 6) throw new IllegalStateException("Game is full");
        String name = cleanName(request.name());
        if (game.getPlayers().stream().anyMatch(p -> p.getName().equalsIgnoreCase(name))) throw new IllegalArgumentException("Player name is already in use");
        game.addPlayer(new Player(name, game.getPlayers().size(), true));
        return view(game);
    }

    @Transactional
    public GameResponse get(String code) {
        return view(getGame(code));
    }

    @Transactional
    public RoundResponse createRound(String code, UUID ownerId, CreateRoundRequest request) {
        Game game = getGame(code);
        authorizeOwner(game, ownerId);
        if (game.getStatus() != GameStatus.IN_PROGRESS) throw new IllegalStateException("Game is not in progress");
        if (game.getRounds().stream().anyMatch(r -> r.getStatus() == RoundStatus.OPEN)) {
            throw new IllegalStateException("The current round must be resolved first");
        }
        List<Player> active = activePlayers(game);
        if (request.scores().size() != active.size()) throw new IllegalArgumentException("A score is required for every active player");
        List<UUID> ids = request.scores().stream().map(s -> s.playerId()).toList();
        if (ids.stream().distinct().count() != ids.size() || active.stream().anyMatch(p -> !ids.contains(p.getId()))) {
            throw new IllegalArgumentException("Scores must contain each active player exactly once");
        }
        Player claimer = request.claimerId() == null ? null : player(game, request.claimerId());
        if (claimer != null && claimer.isEliminated()) throw new IllegalArgumentException("Eliminated players cannot claim");
        if (claimer != null && request.scores().stream().filter(s -> s.playerId().equals(claimer.getId())).findFirst().orElseThrow().handPoints() >= 10) {
            throw new IllegalArgumentException("Claims can only be declared below 10 points");
        }
        Round round = new Round(game.getRounds().size() + 1, claimer);
        request.scores().forEach(score -> round.addScore(new com.claimsgame.backend.game.RoundPlayerScore(player(game, score.playerId()), score.handPoints())));
        game.addRound(round);
        games.save(game);
        return response(round);
    }

    @Transactional
    public RoundResponse resolve(String code, UUID ownerId, UUID roundId) {
        Game game = getGame(code);
        authorizeOwner(game, ownerId);
        Round round = rounds.findByIdAndGameId(roundId, game.getId()).orElseThrow(() -> new IllegalArgumentException("Round not found"));
        if (round.getStatus() != RoundStatus.OPEN) throw new IllegalStateException("Round is already resolved");
        List<Player> active = activePlayers(game);
        int minimum = round.getScores().stream().filter(s -> active.contains(s.getPlayer())).mapToInt(com.claimsgame.backend.game.RoundPlayerScore::getHandPoints).min().orElseThrow();
        Player starPlayer = null;
        if (round.getClaimer() != null) {
            List<Player> tied = round.getScores().stream().filter(s -> active.contains(s.getPlayer()) && s.getHandPoints() == minimum)
                    .map(com.claimsgame.backend.game.RoundPlayerScore::getPlayer).sorted(Comparator.comparingInt(Player::getPlayingOrder)).toList();
            starPlayer = tied.size() == 1 ? tied.getFirst() : nextTiedAfter(round.getClaimer(), tied, active.size());
        }
        for (com.claimsgame.backend.game.RoundPlayerScore score : round.getScores()) {
            Player player = score.getPlayer();
            boolean star = player == starPlayer;
            int points = round.getClaimer() == null || starPlayer == round.getClaimer() ? score.getHandPoints() : (player == round.getClaimer() ? 50 : 0);
            int before = player.getTotalScore();
            player.apply(points, star);
            int penalty = player.getTotalScore() - before - points;
            score.resolve(points + penalty, star, penalty);
        }
        round.setStatus(RoundStatus.RESOLVED);
        Player exactWinner = game.getPlayers().stream().filter(p -> p.getTotalScore() == 200).findFirst().orElse(null);
        if (exactWinner != null) {
            game.setWinnerId(exactWinner.getId());
            game.setStatus(GameStatus.FINISHED);
        } else if (activePlayers(game).size() <= 1) {
            game.setWinnerId(activePlayers(game).stream().findFirst().map(Player::getId).orElse(null));
            game.setStatus(GameStatus.FINISHED);
        }
        games.save(game);
        return response(round);
    }

    @Transactional
    public List<RoundResponse> history(String code) {
        return getGame(code).getRounds().stream().map(this::response).toList();
    }

    private Player nextTiedAfter(Player claimer, List<Player> tied, int playerCount) {
        return tied.stream().filter(p -> p.getPlayingOrder() > claimer.getPlayingOrder()).findFirst().orElse(tied.getFirst());
    }

    private void authorizeOwner(Game game, UUID ownerId) { if (!ownerId.equals(game.getOwnerId())) throw new IllegalStateException("Only the game owner can do this"); }
    private List<Player> activePlayers(Game game) { return game.getPlayers().stream().filter(p -> !p.isEliminated()).toList(); }
    private Player player(Game game, UUID id) { return game.getPlayers().stream().filter(p -> p.getId().equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("Player does not belong to this game")); }
    private Game getGame(String code) { return games.findByJoinCode(code.toUpperCase()).orElseThrow(() -> new IllegalArgumentException("Game not found")); }
    private String cleanName(String name) { String value = name.trim(); if (value.isEmpty() || value.length() > 30) throw new IllegalArgumentException("Player name must be 1 to 30 characters"); return value; }
    private String randomCode() { StringBuilder code = new StringBuilder(6); for (int i = 0; i < 6; i++) code.append(CODE_ALPHABET.charAt(ThreadLocalRandom.current().nextInt(CODE_ALPHABET.length()))); return code.toString(); }

    private GameResponse view(Game game) {
        return new GameResponse(game.getId(), game.getJoinCode(), game.getCreatedAt(), game.getStatus(), game.getOwnerId(), game.getPlayers().stream().sorted(Comparator.comparingInt(Player::getPlayingOrder)).map(p -> new GameResponse.PlayerResponse(p.getId(), p.getName(), p.getPlayingOrder(), p.getTotalScore(), p.getStarStreak(), p.isEliminated(), p.isBot())).toList(), game.getWinnerId());
    }

    private RoundResponse response(Round round) {
        return new RoundResponse(round.getId(), round.getRoundNumber(), round.getCreatedAt(), round.getStatus(), round.getClaimer() == null ? null : round.getClaimer().getId(), round.getScores().stream().map(s -> new RoundResponse.ScoreResponse(s.getPlayer().getId(), s.getPlayer().getName(), s.getHandPoints(), s.getAwardedPoints(), s.isReceivedStar(), s.getStarPenalty())).toList());
    }
}
