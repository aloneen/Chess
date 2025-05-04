package com.mygdx.chess.engine;

import com.badlogic.gdx.Gdx;
import java.io.*;
import java.util.List;
import com.mygdx.chess.screens.BotLevelScreen.Difficulty;

public class StockfishAdapter implements ChessEngineAdapter {
    private Process engineProc;
    private BufferedWriter engineIn;
    private BufferedReader engineOut;
    private Difficulty difficulty;

    public StockfishAdapter(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public void startEngine() throws IOException {
        String enginePath = "/opt/homebrew/bin/stockfish";
        engineProc = new ProcessBuilder(enginePath)
            .redirectErrorStream(true)
            .start();

        engineIn = new BufferedWriter(new OutputStreamWriter(engineProc.getOutputStream()));
        engineOut = new BufferedReader(new InputStreamReader(engineProc.getInputStream()));

        sendCommand("uci");
        setDifficulty(difficulty);
        sendCommand("isready");

        String line;
        while (!(line = readResponse()).equals("readyok")) {
            Gdx.app.log("StockfishAdapter", "Engine_out ▶ " + line);
        }
    }

    @Override
    public void setDifficulty(Difficulty difficulty) throws IOException {
        this.difficulty = difficulty;
        sendCommand("setoption name UCI_LimitStrength value true");
        sendCommand("setoption name UCI_Elo value 1350"); // minimum supported
    }

    @Override
    public String getBestMove(String movesHistory) throws IOException {
        sendCommand("position startpos moves " + movesHistory);
        sendCommand("isready");

        String line;
        while (!(line = readResponse()).equals("readyok")) {
            Gdx.app.log("StockfishAdapter", "Engine_out ▶ " + line);
        }

        switch (difficulty) {
            case LOW:
                sendCommand("go nodes 1 movetime 1");
                break;
            case MEDIUM:
                sendCommand("go depth 3");
                break;
            default:
                sendCommand("go movetime 500");
                break;
        }

        while ((line = readResponse()) != null) {
            if (line.startsWith("bestmove")) {
                return line.split("\\s+")[1];
            }
        }
        return null;
    }

    @Override
    public void sendCommand(String command) throws IOException {
        engineIn.write(command + "\n");
        engineIn.flush();
        Gdx.app.log("StockfishAdapter", "Engine_in  ◀ " + command);
    }

    @Override
    public String readResponse() throws IOException {
        return engineOut.readLine();
    }

    @Override
    public void stopEngine() {
        if (engineProc != null) {
            engineProc.destroy();
        }
    }
}
