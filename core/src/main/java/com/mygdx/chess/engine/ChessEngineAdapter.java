package com.mygdx.chess.engine;

import java.io.IOException;
import java.util.List;
import com.mygdx.chess.screens.BotLevelScreen.Difficulty;

/** Adapter interface to decouple BotGameScreen from concrete UCI engine. */
public interface ChessEngineAdapter {
    void startEngine() throws IOException;
    void sendCommand(String command) throws IOException;
    String readResponse() throws IOException;
    void stopEngine();
    void setDifficulty(Difficulty difficulty) throws IOException;
    String getBestMove(String movesHistory) throws IOException;
}

