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
import com.mygdx.chess.screens.GameOverScreen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.mygdx.chess.screens.BotLevelScreen.Difficulty;

public class BotGameScreen implements Screen {
    private final ChessGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final ChessBoard chessBoard;
    private final GameLogic logic;
    private final boolean humanIsWhite;
    private final Difficulty difficulty;

    private Process engineProc;
    private BufferedWriter engineIn;
    private BufferedReader engineOut;

    private final List<String> moveHistory = new ArrayList<>();
    private boolean botThinking = false;

    public BotGameScreen(ChessGame game, Difficulty difficulty, boolean humanIsWhite) {
        this.game = game;
        this.difficulty = difficulty;
        this.humanIsWhite = humanIsWhite;
        this.batch = new SpriteBatch();

        this.camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800);

        this.chessBoard = new ChessBoard(!humanIsWhite);
        this.logic = chessBoard.getGameLogic();

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
            engineIn = new BufferedWriter(new OutputStreamWriter(engineProc.getOutputStream()));
            engineOut = new BufferedReader(new InputStreamReader(engineProc.getInputStream()));

            sendUCI("uci");
            sendUCI("setoption name UCI_LimitStrength value true");
            int elo;
            switch (difficulty) {
                case LOW:    elo = 600;  break;
                case MEDIUM: elo = 1200; break;
                default:     elo = 1500; break;
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
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(1f,1f,1f,0.7f);
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

            int ms;
            switch (difficulty) {
                case LOW:    ms = 1500; break;
                case MEDIUM: ms = 800;  break;
                default:     ms = 500;  break;
            }
            sendUCI("go movetime " + ms);

            String bestLine;
            String best = null;
            while ((bestLine = engineOut.readLine()) != null) {
                if (bestLine.startsWith("bestmove")) {
                    best = bestLine.split(" ")[1];
                    break;
                }
            }

            if (best != null && best.length() >= 4) {
                final String uciMove = best;
                final int fx = uciMove.charAt(0) - 'a';
                final int fy = uciMove.charAt(1) - '1';
                final int tx = uciMove.charAt(2) - 'a';
                final int ty = uciMove.charAt(3) - '1';
                final boolean isPromo = uciMove.length() == 5;
                final char promoChar = isPromo ? uciMove.charAt(4) : ' ';
                final String promoType;
                switch (promoChar) {
                    case 'q': promoType = "queen";  break;
                    case 'r': promoType = "rook";   break;
                    case 'b': promoType = "bishop"; break;
                    case 'n': promoType = "knight"; break;
                    default:  promoType = null;      break;
                }

                Thread.sleep(500);

                Gdx.app.postRunnable(() -> {
                    Iterator<ChessPiece> it = chessBoard.getPieces().iterator();
                    while (it.hasNext()) {
                        ChessPiece q = it.next();
                        if (q.getXPos() == tx && q.getYPos() == ty && q != findPieceAt(fx, fy)) {
                            it.remove();
                            break;
                        }
                    }
                    ChessPiece p = findPieceAt(fx, fy);
                    if (p != null) {
                        if (isPromo && promoType != null) {
                            ChessPiece promoted = new ChessPiece(p.getColor(), promoType, tx, ty);
                            chessBoard.getPieces().remove(p);
                            chessBoard.getPieces().add(promoted);
                        } else {
                            p.setPosition(tx, ty);
                        }
                        moveHistory.add(uciMove);
                        logic.toggleTurn();

                        String next = logic.isWhiteTurn() ? "white" : "black";
                        if (logic.isCheckmate(next, chessBoard.getPieces())) {
                            String winner = next.equals("white") ? "Black" : "White";
                            game.setScreen(new GameOverScreen(game, "Checkmate! " + winner + " wins."));
                        } else if (logic.isStalemate(next, chessBoard.getPieces())) {
                            game.setScreen(new GameOverScreen(game, "Stalemate! The game is a draw."));
                        }
                    }
                    botThinking = false;
                });
            } else {
                botThinking = false;
            }
        } catch (IOException | InterruptedException e) {
            Gdx.app.error("BotGame", "Bot thinking failed", e);
            botThinking = false;
        }
    }

    private ChessPiece findPieceAt(int x, int y) {
        for (ChessPiece p : chessBoard.getPieces()) {
            if (p.getXPos() == x && p.getYPos() == y) return p;
        }
        return null;
    }

    @Override public void resize(int width, int height) {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume(){}
    @Override public void dispose() {
        batch.dispose();
        chessBoard.dispose();
        if (engineProc != null) engineProc.destroy();
    }
}
