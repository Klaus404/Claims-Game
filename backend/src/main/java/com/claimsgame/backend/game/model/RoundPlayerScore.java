package com.claimsgame.backend.game.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "round_player_scores")
public class RoundPlayerScore {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private int handPoints;
    private int awardedPoints;
    private boolean receivedStar;
    private int starPenalty;
    private Integer previousTotalScore;
    private Integer previousStarStreak;
    private Boolean previousEliminated;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Round round;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Player player;

    protected RoundPlayerScore() {
    }

    public RoundPlayerScore(Player player, int handPoints) {
        this.player = player;
        this.handPoints = handPoints;
    }

    public UUID getId() { return id; }
    public int getHandPoints() { return handPoints; }
    public int getAwardedPoints() { return awardedPoints; }
    public boolean isReceivedStar() { return receivedStar; }
    public int getStarPenalty() { return starPenalty; }
    public Integer getPreviousTotalScore() { return previousTotalScore; }
    public Integer getPreviousStarStreak() { return previousStarStreak; }
    public Boolean getPreviousEliminated() { return previousEliminated; }
    public Player getPlayer() { return player; }
    public void setRound(Round round) { this.round = round; }
    public void setHandPoints(int handPoints) { this.handPoints = handPoints; }
    public void capturePreviousState(Player player) {
        previousTotalScore = player.getTotalScore();
        previousStarStreak = player.getStarStreak();
        previousEliminated = player.isEliminated();
    }
    public void resolve(int awardedPoints, boolean receivedStar, int starPenalty) {
        this.awardedPoints = awardedPoints;
        this.receivedStar = receivedStar;
        this.starPenalty = starPenalty;
    }
}
