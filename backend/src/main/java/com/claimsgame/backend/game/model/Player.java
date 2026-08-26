package com.claimsgame.backend.game.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(name = "players", uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "playing_order"}))
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private int playingOrder;
    private int totalScore;
    private int starStreak;
    private boolean eliminated;
    private boolean bot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Game game;

    protected Player() {
    }

    public Player(String name, int playingOrder) {
        this.name = name;
        this.playingOrder = playingOrder;
    }

    public Player(String name, int playingOrder, boolean bot) {
        this(name, playingOrder);
        this.bot = bot;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public int getPlayingOrder() { return playingOrder; }
    public void setPlayingOrder(int playingOrder) { this.playingOrder = playingOrder; }
    public int getTotalScore() { return totalScore; }
    public int getStarStreak() { return starStreak; }
    public boolean isEliminated() { return eliminated; }
    public void setEliminated(boolean eliminated) { this.eliminated = eliminated; }
    public boolean isBot() { return bot; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public void apply(int points, boolean receivesStar) {
        totalScore += points;
        if (receivesStar) {
            starStreak++;
            if (starStreak == 3) {
                totalScore -= 50;
                starStreak = 0;
            }
        } else {
            starStreak = 0;
        }
        eliminated = totalScore > 200;
    }
}
