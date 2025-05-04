package com.mygdx.chess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.chess.ChessGame;
import com.mygdx.chess.actors.ChessPiece;
import com.mygdx.chess.decorator.HighlightDecorator;
import com.mygdx.chess.factory.BoardModelFactory;
import com.mygdx.chess.factory.ChessPieceFactory;
import com.mygdx.chess.input.ChessInputProcessor;
import com.mygdx.chess.logic.GameLogic;
import com.mygdx.chess.model.IBoardModel;
import com.mygdx.chess.sound.SoundManager;
import com.mygdx.chess.view.ChessRenderer;
import com.mygdx.chess.view.IChessRenderer;

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
    private final ChessGame        game;
    private final SpriteBatch      batch;
    private final OrthographicCamera camera;
    private final IBoardModel      model;
    private final IChessRenderer   renderer;
    private final GameLogic        logic;
    private final boolean          humanIsWhite;
    private final Difficulty       difficulty;

    private Process        engineProc;
    private BufferedWriter engineIn;
    private BufferedReader engineOut;

    /** UCI move list from White's perspective */
    private final List<String> moveHistory = new ArrayList<>();
    private boolean           botThinking = false;

    public BotGameScreen(ChessGame game, Difficulty difficulty, boolean humanIsWhite) {
        this.game         = game;
        this.difficulty   = difficulty;
        this.humanIsWhite = humanIsWhite;

        batch   = new SpriteBatch();
        camera  = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800);

        model    = BoardModelFactory.createStandardBoard(!humanIsWhite);
        renderer = new ChessRenderer(model);
        logic    = model.getGameLogic();

        Gdx.input.setInputProcessor(new ChessInputProcessor(
            game, model, camera, renderer
        ));

        startEngine();
    }

    // ——————— PUBLIC API ———————

    public ChessGame getGame() {
        return game;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public boolean isHumanWhite() {
        return humanIsWhite;
    }

    /**
     * Record a non‑promotion move.
     */
    public void recordHumanMove(int fx, int fy, int tx, int ty) {
        recordHumanMove(fx, fy, tx, ty, '\0');
    }

    /**
     * Record a (possibly promoted) move into moveHistory.
     */
    private void recordHumanMove(int fx, int fy, int tx, int ty, char promo) {
        StringBuilder uci = new StringBuilder()
            .append((char)('a' + fx))
            .append((char)('1' + fy))
            .append((char)('a' + tx))
            .append((char)('1' + ty));
        if (promo != '\0') uci.append(promo);
        moveHistory.add(uci.toString());
    }

    // ——————— ENGINE STARTUP ———————

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

//            int elo;
//            switch (difficulty) {
//                case LOW:    elo =  300; break;
//                case MEDIUM: elo =  400; break;
//                default:     elo = 1000; break;
//            }
//            sendUCI("setoption name UCI_Elo value " + elo);

            // Stockfish won’t go below ~1350, so use a shallow search instead
            sendUCI("setoption name UCI_Elo value 1350"); // minimum supported

            sendUCI("isready");
            String line;
            while (!(line = engineOut.readLine()).equals("readyok")) {
                Gdx.app.log("BotGame", "Engine_out ▶ " + line);
            }
        } catch (IOException e) {
            Gdx.app.error("BotGame", "Failed to launch Stockfish", e);
        }
    }

    private void sendUCI(String cmd) throws IOException {
        engineIn.write(cmd + "\n");
        engineIn.flush();
        Gdx.app.log("BotGame", "Engine_in  ◀ " + cmd);
    }

    // ——————— GAME LOOP ———————

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1f,1f,1f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderer.render(batch);
        batch.end();

        boolean whiteToMove = logic.isWhiteTurn();
        boolean botTurn     = (whiteToMove && !humanIsWhite)
            || (!whiteToMove &&  humanIsWhite);

        if (botTurn && !botThinking) {
            botThinking = true;
            new Thread(this::thinkAndMove).start();
        }
    }

    private void thinkAndMove() {
        try {
            // 1) send position
            String moves = String.join(" ", moveHistory);
            sendUCI("position startpos moves " + moves);
            sendUCI("isready");
            String line;
            while (!(line = engineOut.readLine()).equals("readyok")) {
                Gdx.app.log("BotGame", "Engine_out ▶ " + line);
            }

//            // 2) go
//            int ms;
//            switch (difficulty) {
//                case LOW:    ms = 1500; break;
//                case MEDIUM: ms =  800; break;
//                default:     ms =  500; break;
//            }
//            sendUCI("go movetime " + ms);
            // 2) go: force very shallow search on LOW, modest on MEDIUM
            switch (difficulty) {
                case LOW:
                    sendUCI("go nodes 1 movetime 1");    // examines only a single search node      // essentially random legal moves
                    break;
                case MEDIUM:
                    sendUCI("go depth 3");       // still quite weak
                    break;
                default:
                    sendUCI("go movetime 500");  // full-strength “Strong” bot
                    break;
            }



            // 3) read bestmove
            String best = null;
            while ((line = engineOut.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    best = line.split("\\s+")[1];
                    break;
                }
            }

            if (best != null && best.length() >= 4) {
                final String engineUCI   = best;
                final String uciToApply = engineUCI;
                final int fx = uciToApply.charAt(0) - 'a';
                final int fy = uciToApply.charAt(1) - '1';
                final int tx = uciToApply.charAt(2) - 'a';
                final int ty = uciToApply.charAt(3) - '1';
                final boolean isPromo = uciToApply.length() == 5;
                final char promC     = isPromo ? uciToApply.charAt(4) : ' ';

                Thread.sleep(500);

                Gdx.app.postRunnable(() -> {
                    List<ChessPiece> pieces = model.getPieces();
                    ChessPiece mover = findPieceAt(fx, fy);

                    // 1) castling
                    if (mover != null && mover.getType().equalsIgnoreCase("king") && Math.abs(tx - fx) == 2) {
                        int rookFromX = (tx > fx) ? 7 : 0;
                        int rookToX   = (tx > fx) ? fx + 1 : fx - 1;
                        ChessPiece rook = findPieceAt(rookFromX, fy);
                        if (rook != null && rook.getType().equalsIgnoreCase("rook")) rook.setPosition(rookToX, fy);
                    }

                    // 2) capture
                    if (mover != null) {
                        Iterator<ChessPiece> it = pieces.iterator();
                        while (it.hasNext()) {
                            ChessPiece p = it.next();
                            if (p.getXPos() == tx && p.getYPos() == ty && p != mover) {
                                it.remove();
                                SoundManager.playCapture();
                                break;
                            }
                        }
                    }

                    // 3) move or promotion
                    if (mover != null) {
                        if (isPromo && promC != ' ') {
                            pieces.remove(mover);
                            pieces.add(new ChessPiece(mover.getColor(), promTypeFromChar(promC), tx, ty));
                            logic.clearEnPassantTarget();
                            SoundManager.playPromote();
                        } else {
                            if (mover.getType().equalsIgnoreCase("pawn") && Math.abs(ty - fy) == 2) {
                                logic.setEnPassantTarget(tx, (fy + ty) / 2, mover);
                            } else {
                                logic.clearEnPassantTarget();
                            }
                            mover.setPosition(tx, ty);
                            SoundManager.playMove();
                        }
                    }

                    // 4) record engine move & toggle turn
                    moveHistory.add(engineUCI);
                    logic.toggleTurn();

                    // 5) decorators & endgame
                    for (ChessPiece p : pieces) p.clearDecorators();
                    ChessPiece moved = (mover != null && isPromo) ? findPieceAt(tx, ty) : mover;
                    if (moved != null) moved.addDecorator(new HighlightDecorator());
                    String next = logic.isWhiteTurn() ? "white" : "black";
                    if (logic.isCheckmate(next, pieces)) game.setScreen(new GameOverScreen(game, "Checkmate! " + (next.equals("white") ? "Black" : "White") + " wins."));
                    else if (logic.isStalemate(next, pieces)) game.setScreen(new GameOverScreen(game, "Stalemate! The game is a draw."));

                    // 6) done
                    botThinking = false;
                });
            } else botThinking = false;
        } catch (IOException | InterruptedException e) {
            Gdx.app.error("BotGame", "Bot thinking failed", e);
            botThinking = false;
        }
    }

    /** Applies a human‑pawn promotion. */
    public void applyPromotion(ChessPiece pawn, int fx, int fy, int tx, int ty, String newType) {
        model.getPieces().remove(pawn);
        model.getPieces().add(ChessPieceFactory.create(pawn.getColor(), newType, tx, ty));
        logic.clearEnPassantTarget();
        recordHumanMove(fx, fy, tx, ty, newType.toLowerCase().charAt(0));
        logic.toggleTurn();
        botThinking = false;
        Gdx.input.setInputProcessor(new ChessInputProcessor(game, model, camera, renderer));
    }

    private ChessPiece findPieceAt(int x, int y) {
        for (ChessPiece p : model.getPieces()) if (p.getXPos() == x && p.getYPos() == y) return p;
        return null;
    }

    private String promTypeFromChar(char c) {
        switch (c) {
            case 'q': return "queen";
            case 'r': return "rook";
            case 'b': return "bishop";
            case 'n': return "knight";
            default:  return null;
        }
    }

    @Override public void resize(int w, int h) {}
    @Override public void show()    {}
    @Override public void hide()    {}
    @Override public void pause()   {}
    @Override public void resume()  {}
    @Override public void dispose() {
        batch.dispose(); renderer.dispose(); if (engineProc!=null) engineProc.destroy();
    }
}

