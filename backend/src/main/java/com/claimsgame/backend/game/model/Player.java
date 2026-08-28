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
    private String icon;
    private int playingOrder;
    private int totalScore;
    private int starStreak;
    private boolean eliminated;
    private boolean leftGame;
    private Integer eliminationOrder;
    private boolean bot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Game game;

    protected Player() {
    }

    public Player(String name, int playingOrder) {
        this.name = name;
        this.playingOrder = playingOrder;
        this.icon = defaultIcon(playingOrder);
    }

    public Player(String name, int playingOrder, boolean bot) {
        this(name, playingOrder);
        this.bot = bot;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public int getPlayingOrder() { return playingOrder; }
    public void setPlayingOrder(int playingOrder) { this.playingOrder = playingOrder; }
    public void setIcon(String icon) { this.icon = icon; }
    public int getTotalScore() { return totalScore; }
    public int getStarStreak() { return starStreak; }
    public boolean isEliminated() { return eliminated; }
    public boolean isLeft() { return leftGame; }
    public void setLeft(boolean left) { this.leftGame = left; }
    public Integer getEliminationOrder() { return eliminationOrder; }
    public void setEliminationOrder(Integer eliminationOrder) { this.eliminationOrder = eliminationOrder; }
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

    public void restore(int totalScore, int starStreak, boolean eliminated) {
        this.totalScore = totalScore;
        this.starStreak = starStreak;
        this.eliminated = eliminated;
    }

    private static String defaultIcon(int playingOrder) {
        return new String[]{"hedgehog", "sloth", "tiger", "parrot", "elephant", "fox", "chick", "dog", "turtle", "lion", "icons8-bear-94", "icons8-koi-fish-94", "icons8-corgi-94", "icons8-cockroach-94"}[playingOrder % 14];
    }
}
