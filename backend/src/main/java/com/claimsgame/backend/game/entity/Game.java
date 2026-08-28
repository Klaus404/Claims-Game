package com.claimsgame.backend.game.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String joinCode;
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private GameStatus status;
    private UUID ownerId;
    private UUID winnerId;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Round> rounds = new ArrayList<>();

    protected Game() {
    }

    public Game(String joinCode) {
        this.joinCode = joinCode;
        this.createdAt = Instant.now();
        this.status = GameStatus.WAITING;
    }

    public UUID getId() { return id; }
    public String getJoinCode() { return joinCode; }
    public Instant getCreatedAt() { return createdAt; }
    public GameStatus getStatus() { return status; }
    public UUID getOwnerId() { return ownerId; }
    public UUID getWinnerId() { return winnerId; }
    public List<Player> getPlayers() { return players; }
    public List<Round> getRounds() { return rounds; }
    public void setStatus(GameStatus status) { this.status = status; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    public void setWinnerId(UUID winnerId) { this.winnerId = winnerId; }

    public void addPlayer(Player player) {
        player.setGame(this);
        players.add(player);
    }

    public void addRound(Round round) {
        round.setGame(this);
        rounds.add(round);
    }

    public void removePlayer(Player player) {
        players.remove(player);
        player.setGame(null);
        players.stream().sorted(java.util.Comparator.comparingInt(Player::getPlayingOrder)).forEach(p -> p.setPlayingOrder(players.indexOf(p)));
    }
}
