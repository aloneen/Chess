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
     * Record a non‐promotion move.
     */
    public void recordHumanMove(int fx, int fy, int tx, int ty) {
        recordHumanMove(fx, fy, tx, ty, '\0');
    }

    /**
     * Record a (possibly promoted) move into moveHistory,
     * flipping coordinates if human is Black.
     *
     * @param fx    source file 0–7
     * @param fy    source rank 0–7
     * @param tx    target file 0–7
     * @param ty    target rank 0–7
     * @param promo promotion char 'q','r','b','n' or '\0'
     */
    private void recordHumanMove(int fx, int fy, int tx, int ty, char promo) {
        if (!humanIsWhite) {
            fx = 7 - fx; fy = 7 - fy;
            tx = 7 - tx; ty = 7 - ty;
        }
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

            int elo;
            switch (difficulty) {
                case LOW:    elo =  300; break;
                case MEDIUM: elo =  400; break;
                default:     elo = 1000; break;
            }
            sendUCI("setoption name UCI_Elo value " + elo);
            sendUCI("isready");
            // consume the readyok
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
            // 1) position
            String moves = String.join(" ", moveHistory);
            Gdx.app.log("BotGame", "UCI position: startpos moves " + moves);
            sendUCI("position startpos moves " + moves);
            sendUCI("isready");
            String line;
            while (!(line = engineOut.readLine()).equals("readyok")) {
                Gdx.app.log("BotGame", "Engine_out ▶ " + line);
            }

            // 2) go
            int ms;
            switch (difficulty) {
                case LOW:    ms = 1500; break;
                case MEDIUM: ms =  800; break;
                default:     ms =  500; break;
            }
            sendUCI("go movetime " + ms);

            // 3) read bestmove
            String best = null;
            while ((line = engineOut.readLine()) != null) {
                Gdx.app.log("BotGame", "Engine_out ▶ " + line);
                if (line.startsWith("bestmove")) {
                    best = line.split("\\s+")[1];
                    break;
                }
            }

            if (best != null && best.length() >= 4) {
                // apply on libGDX board, flipping if human is Black
                final String engineUCI   = best;
                final String uciToApply = humanIsWhite
                    ? engineUCI
                    : flipUCI(engineUCI);

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

                    // castling
                    if (mover != null
                        && mover.getType().equalsIgnoreCase("king")
                        && Math.abs(tx - fx) == 2)
                    {
                        int rookFromX = (tx > fx) ? 7 : 0;
                        int rookToX   = (tx > fx) ? fx + 1 : fx - 1;
                        ChessPiece rook = findPieceAt(rookFromX, fy);
                        if (rook != null && rook.getType().equalsIgnoreCase("rook")) {
                            rook.setPosition(rookToX, fy);
                        }
                    }

                    // capture
                    boolean isCapture = false;
                    Iterator<ChessPiece> it = pieces.iterator();
                    while (it.hasNext()) {
                        ChessPiece p = it.next();
                        if (p.getXPos() == tx && p.getYPos() == ty && p != mover) {
                            isCapture = true;
                            it.remove();
                            break;
                        }
                    }

                    // move or promote
                    if (mover != null) {
                        if (isPromo && promC != ' ') {
                            pieces.remove(mover);
                            pieces.add(new ChessPiece(
                                mover.getColor(),
                                promTypeFromChar(promC),
                                tx, ty
                            ));
                            logic.clearEnPassantTarget();
                            SoundManager.playPromote();
                        } else {
                            // en passant target
                            if (mover.getType().equalsIgnoreCase("pawn")
                                && Math.abs(ty - fy) == 2)
                            {
                                int midY = (fy + ty) / 2;
                                logic.setEnPassantTarget(tx, midY, mover);
                            } else {
                                logic.clearEnPassantTarget();
                            }
                            mover.setPosition(tx, ty);

                            // sound
                            if (isCapture) SoundManager.playCapture();
                            else            SoundManager.playMove();
                        }

                        // record into moveHistory for engine (always white-perspective)
                        moveHistory.add(engineUCI);

                        // toggle and decorators
                        logic.toggleTurn();
                        for (ChessPiece p : pieces) p.clearDecorators();
                        ChessPiece moved = (isPromo && promC != ' ')
                            ? findPieceAt(tx, ty) : mover;
                        if (moved != null) moved.addDecorator(new HighlightDecorator());

                        String next = logic.isWhiteTurn() ? "white" : "black";
                        if (logic.isCheckmate(next, pieces)) {
                            String winner = next.equals("white") ? "Black" : "White";
                            game.setScreen(new GameOverScreen(
                                game, "Checkmate! " + winner + " wins."
                            ));
                        } else if (logic.isStalemate(next, pieces)) {
                            game.setScreen(new GameOverScreen(
                                game, "Stalemate! The game is a draw."
                            ));
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

    /** Flip a UCI move (e.g. "e2e4") around the board for Black. */
    private String flipUCI(String uci) {
        int fx = 7 - (uci.charAt(0) - 'a');
        int fy = 7 - (uci.charAt(1) - '1');
        int tx = 7 - (uci.charAt(2) - 'a');
        int ty = 7 - (uci.charAt(3) - '1');
        StringBuilder sb = new StringBuilder();
        sb.append((char)('a' + fx))
            .append((char)('1' + fy))
            .append((char)('a' + tx))
            .append((char)('1' + ty));
        if (uci.length() == 5) sb.append(uci.charAt(4));
        return sb.toString();
    }

    /** Convert promotion letter to piece type string. */
    private String promTypeFromChar(char c) {
        switch (c) {
            case 'q': return "queen";
            case 'r': return "rook";
            case 'b': return "bishop";
            case 'n': return "knight";
            default:  return null;
        }
    }

    /** Finds the piece at board coordinates (x,y). */
    private ChessPiece findPieceAt(int x, int y) {
        for (ChessPiece p : model.getPieces()) {
            if (p.getXPos() == x && p.getYPos() == y) return p;
        }
        return null;
    }

    /** Applies a human‐pawn promotion in this BotGameScreen instance. */
    // in BotGameScreen.java, add this alongside your existing applyPromotion:

    /**
     * Promotion that knows both origin and target squares:
     */
    public void applyPromotion(
        ChessPiece pawn,
        int fx, int fy,
        int tx, int ty,
        String newType
    ) {
        // 1) Remove pawn + add new piece
        model.getPieces().remove(pawn);
        model.getPieces().add(
            ChessPieceFactory.create(
                pawn.getColor(), newType, tx, ty
            )
        );
        logic.clearEnPassantTarget();

        // 2) Record the real promotion UCI, flipping if needed:
        char promoChar = newType.toLowerCase().charAt(0);
        recordHumanMove(fx, fy, tx, ty, promoChar);

        // 3) Toggle back to bot and unblock thinking
        logic.toggleTurn();
        botThinking = false;

        // 4) Restore input
        Gdx.input.setInputProcessor(new ChessInputProcessor(
            game, model, camera, renderer
        ));
    }



    // ——————— SCREEN STUBS ———————

    @Override public void resize(int w, int h) {}
    @Override public void show()    {}
    @Override public void hide()    {}
    @Override public void pause()   {}
    @Override public void resume()  {}
    @Override public void dispose() {
        batch.dispose();
        renderer.dispose();
        if (engineProc != null) engineProc.destroy();
    }
}
