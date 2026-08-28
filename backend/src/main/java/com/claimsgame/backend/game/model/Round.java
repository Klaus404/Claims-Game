package com.claimsgame.backend.game.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rounds")
public class Round {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private int roundNumber;
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private RoundStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    private Player claimer;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoundPlayerScore> scores = new ArrayList<>();

    protected Round() {
    }

    public Round(int roundNumber, Player claimer) {
        this.roundNumber = roundNumber;
        this.claimer = claimer;
        this.createdAt = Instant.now();
        this.status = RoundStatus.OPEN;
    }

    public UUID getId() { return id; }
    public int getRoundNumber() { return roundNumber; }
    public Instant getCreatedAt() { return createdAt; }
    public RoundStatus getStatus() { return status; }
    public Game getGame() { return game; }
    public Player getClaimer() { return claimer; }
    public List<RoundPlayerScore> getScores() { return scores; }
    public void setGame(Game game) { this.game = game; }
    public void setClaimer(Player claimer) { this.claimer = claimer; }
    public void setStatus(RoundStatus status) { this.status = status; }
    public void addScore(RoundPlayerScore score) { score.setRound(this); scores.add(score); }
}
