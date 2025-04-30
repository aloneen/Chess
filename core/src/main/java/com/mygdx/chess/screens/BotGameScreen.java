package com.mygdx.chess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.chess.ChessGame;
import com.mygdx.chess.actors.ChessBoard;
import com.mygdx.chess.actors.ChessPiece;
import com.mygdx.chess.input.ChessInputProcessor;
import com.mygdx.chess.logic.GameLogic;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.mygdx.chess.screens.BotLevelScreen.Difficulty;

public class BotGameScreen implements Screen {
    private final ChessGame      game;
    private final SpriteBatch    batch;
    private final OrthographicCamera camera;
    private final ChessBoard     chessBoard;
    private final GameLogic      logic;
    private final boolean        humanIsWhite;
    private final Difficulty     difficulty;

    private Process        engineProc;
    private BufferedWriter engineIn;
    private BufferedReader engineOut;

    private final List<String> moveHistory = new ArrayList<>();
    private boolean botThinking = false;

    public BotGameScreen(ChessGame game, Difficulty difficulty, boolean humanIsWhite) {
        this.game         = game;
        this.difficulty   = difficulty;
        this.humanIsWhite = humanIsWhite;
        this.batch        = new SpriteBatch();

        this.camera       = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800);

        this.chessBoard = new ChessBoard(!humanIsWhite);
        this.logic      = chessBoard.getGameLogic();

        Gdx.input.setInputProcessor(new ChessInputProcessor(game, chessBoard, camera));
        startEngine();
    }

    public GameLogic getGameLogic() {
        return logic;
    }

    public boolean isHumanWhite() {
        return humanIsWhite;
    }

    public void recordHumanMove(int fromX, int fromY, int toX, int toY) {
        String uci = "" + (char)('a' + fromX) + (char)('1' + fromY)
            + (char)('a' + toX) + (char)('1' + toY);
        moveHistory.add(uci);
    }

    private void startEngine() {
        try {
            String enginePath = "/opt/homebrew/bin/stockfish";
            engineProc = new ProcessBuilder(enginePath)
                .redirectErrorStream(true)
                .start();
            engineIn  = new BufferedWriter(new OutputStreamWriter(engineProc.getOutputStream()));
            engineOut = new BufferedReader(new InputStreamReader(engineProc.getInputStream()));

            sendUCI("uci");
            sendUCI("setoption name UCI_LimitStrength value true");
            int elo;
            switch (difficulty) {
                case LOW:    elo = 600;  break;  // weaker Elo
                case MEDIUM: elo =1200;  break;
                default:     elo =1500;  break;
            }
            sendUCI("setoption name UCI_Elo value " + elo);
            sendUCI("isready");
        } catch (IOException e) {
            Gdx.app.error("BotGame", "Failed to launch Stockfish", e);
        }
    }

    private void sendUCI(String cmd) throws IOException {
        engineIn.write(cmd + "\n");
        engineIn.flush();
    }

    @Override
    public void render(float delta) {
        chessBoard.update(delta);
        Gdx.gl.glClearColor(1f,1f,1f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.setColor(1f,1f,1f,0.7f);
        batch.begin();
        chessBoard.render(batch);
        batch.end();

        boolean whiteToMove = logic.isWhiteTurn();
        boolean botTurn = (whiteToMove && !humanIsWhite) || (!whiteToMove && humanIsWhite);
        if (botTurn && !botThinking) {
            botThinking = true;
            new Thread(this::thinkAndMove).start();
        }
    }

    private void thinkAndMove() {
        try {
            sendUCI("position startpos moves " + String.join(" ", moveHistory));

            if (difficulty == Difficulty.LOW) {
                // restrict search depth for low level
                sendUCI("go depth 1");
            } else {
                int ms = (difficulty == Difficulty.MEDIUM) ? 500 : 1000;
                sendUCI("go movetime " + ms);
            }

            String best = null, line;
            while ((line = engineOut.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    best = line.split(" ")[1]; break;
                }
            }

            if (best != null && best.length()>=4) {
                final int fx = best.charAt(0)-'a';
                final int fy = best.charAt(1)-'1';
                final int tx = best.charAt(2)-'a';
                final int ty = best.charAt(3)-'1';
                final String uciMove = best;

                Gdx.app.postRunnable(() -> {
                    Iterator<ChessPiece> it = chessBoard.getPieces().iterator();
                    while (it.hasNext()) {
                        ChessPiece q = it.next();
                        if (q.getXPos()==tx && q.getYPos()==ty && q!=findPieceAt(fx,fy)) {
                            it.remove(); break;
                        }
                    }
                    ChessPiece p = findPieceAt(fx,fy);
                    if (p!=null) {
                        p.setPosition(tx,ty);
                        moveHistory.add(uciMove);
                        logic.toggleTurn();

                        String turnColor = logic.isWhiteTurn()?"white":"black";
                        if (logic.isCheckmate(turnColor, chessBoard.getPieces())) {
                            String winner = turnColor.equals("white")?"Black":"White";
                            game.setScreen(new GameOverScreen(game, "Checkmate! " + winner + " wins."));
                        } else if (logic.isStalemate(turnColor, chessBoard.getPieces())) {
                            game.setScreen(new GameOverScreen(game, "Stalemate! The game is a draw."));
                        }
                    }
                    botThinking = false;
                });
            } else {
                Gdx.app.postRunnable(() -> botThinking=false);
            }
        } catch (IOException e) {
            Gdx.app.error("BotGame","Bot thinking failed",e);
            Gdx.app.postRunnable(() -> botThinking=false);
        }
    }

    private ChessPiece findPieceAt(int x,int y) {
        for (ChessPiece p: chessBoard.getPieces()) {
            if (p.getXPos()==x && p.getYPos()==y) return p;
        }
        return null;
    }

    @Override public void resize(int width,int height) { }
    @Override public void show() { }
    @Override public void hide() { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void dispose() {
        batch.dispose(); chessBoard.dispose();
        if (engineProc!=null) engineProc.destroy();
    }
}
