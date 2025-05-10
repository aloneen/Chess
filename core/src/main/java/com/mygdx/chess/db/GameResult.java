package com.mygdx.chess.db;

import java.sql.Timestamp;

public class GameResult {
    private final String winner;
    private final String botSide;
    private final String gameMode;
    private final Timestamp timestamp;

    public GameResult(String winner, String botSide, String gameMode, Timestamp timestamp) {
        this.winner = winner;
        this.botSide = botSide;
        this.gameMode = gameMode;
        this.timestamp = timestamp;
    }

    public String getWinner() { return winner; }
    public String getBotSide() { return botSide; }
    public String getGameMode() { return gameMode; }
    public Timestamp getTimestamp() { return timestamp; }
}
