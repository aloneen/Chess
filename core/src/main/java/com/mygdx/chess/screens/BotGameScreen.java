
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
import com.mygdx.chess.input.ChessInputProcessor;
import com.mygdx.chess.logic.GameLogic;
import com.mygdx.chess.model.BoardModel;
import com.mygdx.chess.model.IBoardModel;
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
    private final ChessGame      game;
    private final SpriteBatch    batch;
    private final OrthographicCamera camera;
    private final IBoardModel    model;
    private final IChessRenderer renderer;
    private final GameLogic      logic;
    private final boolean        humanIsWhite;
    private final Difficulty     difficulty;

    private Process              engineProc;
    private BufferedWriter       engineIn;
    private BufferedReader       engineOut;

    private final List<String>   moveHistory = new ArrayList<>();
    private boolean              botThinking = false;

    public BotGameScreen(ChessGame game, Difficulty difficulty, boolean humanIsWhite) {
        this.game         = game;
        this.difficulty   = difficulty;
        this.humanIsWhite = humanIsWhite;

        // libGDX setup
        this.batch  = new SpriteBatch();
        this.camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800);

        // model, renderer, logic
        //Factory Method Design Pattern.
        this.model = BoardModelFactory.createStandardBoard(!humanIsWhite);
        this.renderer = new ChessRenderer(model);
        this.logic    = model.getGameLogic();

        // input
        Gdx.input.setInputProcessor(new ChessInputProcessor(
            game, model, camera, renderer
        ));

        startEngine();
    }

    public GameLogic getGameLogic() {
        return logic;
    }

    public boolean isHumanWhite() {
        return humanIsWhite;
    }

    public void recordHumanMove(int fromX, int fromY, int toX, int toY) {
        String uci = ""
            + (char)('a' + fromX)
            + (char)('1' + fromY)
            + (char)('a' + toX)
            + (char)('1' + toY);
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
                case LOW:    elo =  300; break;
                case MEDIUM: elo = 400; break;
                default:     elo = 1000; break;
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
        // draw board
        Gdx.gl.glClearColor(1f,1f,1f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderer.render(batch);
        batch.end();

        // if it's bot's turn, think
        boolean whiteToMove = logic.isWhiteTurn();
        boolean botTurn = (whiteToMove && !humanIsWhite) || (!whiteToMove && humanIsWhite);
        if (botTurn && !botThinking) {
            botThinking = true;
            new Thread(new Runnable() { public void run() { thinkAndMove(); } }).start();
        }
    }

    private void thinkAndMove() {
        try {
            // 1) position
            sendUCI("position startpos moves " + String.join(" ", moveHistory));

            // 2) go
            int ms;
            switch (difficulty) {
                case LOW:    ms = 1500; break;
                case MEDIUM: ms =  800; break;
                default:     ms =  500; break;
            }
            sendUCI("go movetime " + ms);

            // 3) read bestmove
            String line, best = null;
            while ((line = engineOut.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    best = line.split(" ")[1];
                    break;
                }
            }

            if (best != null && best.length() >= 4) {
                final String uci = best;
                final int fx = uci.charAt(0) - 'a';
                final int fy = uci.charAt(1) - '1';
                final int tx = uci.charAt(2) - 'a';
                final int ty = uci.charAt(3) - '1';
                final boolean isPromo = (uci.length() == 5);
                final char promC = isPromo ? uci.charAt(4) : ' ';
                final String promType;
                switch (promC) {
                    case 'q': promType = "queen";  break;
                    case 'r': promType = "rook";   break;
                    case 'b': promType = "bishop"; break;
                    case 'n': promType = "knight"; break;
                    default:  promType = null;     break;
                }

                // small delay
                Thread.sleep(500);

                Gdx.app.postRunnable(new Runnable() {
                    @Override public void run() {
                        List<ChessPiece> pieces = model.getPieces();

                        // a) find mover
                        ChessPiece mover = findPieceAt(fx, fy);

                        // b) castling?
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

                        // c) remove capture
                        Iterator<ChessPiece> it = pieces.iterator();
                        while (it.hasNext()) {
                            ChessPiece p = it.next();
                            if (p.getXPos() == tx && p.getYPos() == ty && p != mover) {
                                it.remove();
                                break;
                            }
                        }

                        // d) promotion or normal move
                        if (mover != null) {
                            if (isPromo && promType != null) {
                                pieces.remove(mover);
                                pieces.add(new ChessPiece(
                                    mover.getColor(), promType, tx, ty));
                                logic.clearEnPassantTarget();
                            } else {
                                // en passant setup
                                if (mover.getType().equalsIgnoreCase("pawn")
                                    && Math.abs(ty - fy) == 2)
                                {
                                    int midY = (fy + ty) / 2;
                                    logic.setEnPassantTarget(tx, midY, mover);
                                } else {
                                    logic.clearEnPassantTarget();
                                }
                                mover.setPosition(tx, ty);
                            }

                            // e) record
                            moveHistory.add(uci);

                            // f) switch and endgame
                            logic.toggleTurn();

                            //DECORATOR


                            // Clear all existing decorators
                            for (ChessPiece p : pieces) {
                                p.clearDecorators();
                            }

                            // Highlight the moved piece
                            ChessPiece finalPiece = (isPromo && promType != null)
                                ? findPieceAt(tx, ty)
                                : mover;

                            if (finalPiece != null) {
                                finalPiece.addDecorator(new HighlightDecorator());
                            }



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
                    }
                });
            } else {
                botThinking = false;
            }
        } catch (IOException | InterruptedException e) {
            Gdx.app.error("BotGame", "Bot thinking failed", e);
            botThinking = false;
        }
    }

    /** Finds the piece at board coordinates (x,y). */
    private ChessPiece findPieceAt(int x, int y) {
        for (ChessPiece p : model.getPieces()) {
            if (p.getXPos() == x && p.getYPos() == y) {
                return p;
            }
        }
        return null;
    }

    // Screen stubs
    @Override public void resize(int w, int h) {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        batch.dispose();
        renderer.dispose();
        if (engineProc != null) engineProc.destroy();
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
}
