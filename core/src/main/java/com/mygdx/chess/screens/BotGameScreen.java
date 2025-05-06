package com.mygdx.chess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.chess.ChessGame;
import com.mygdx.chess.actors.ChessPiece;
import com.mygdx.chess.decorator.HighlightDecorator;
import com.mygdx.chess.engine.ChessEngineAdapter;
import com.mygdx.chess.engine.StockfishAdapter;
import com.mygdx.chess.factory.BoardModelFactory;
import com.mygdx.chess.factory.ChessPieceFactory;
import com.mygdx.chess.input.ChessInputProcessor;
import com.mygdx.chess.logic.GameLogic;
import com.mygdx.chess.model.IBoardModel;
import com.mygdx.chess.sound.SoundManager;
import com.mygdx.chess.view.ChessRenderer;
import com.mygdx.chess.view.IChessRenderer;
import com.mygdx.chess.view.decorator.CheckDecoratorRenderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.mygdx.chess.screens.BotLevelScreen.Difficulty;

public class BotGameScreen implements Screen {
    private final ChessGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final IBoardModel model;
    private final IChessRenderer renderer;
    private final GameLogic logic;
    private final boolean humanIsWhite;
    private final Difficulty difficulty;
    private final ChessEngineAdapter engineAdapter;

    private Stage uiStage;
    private Skin skin;
    private Dialog confirmExitDialog;

    private final List<String> moveHistory = new ArrayList<>();
    private boolean botThinking = false;

    public BotGameScreen(ChessGame game, Difficulty difficulty, boolean humanIsWhite) {
        this.game         = game;
        this.difficulty   = difficulty;
        this.humanIsWhite = humanIsWhite;

        batch  = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800);

        model    = BoardModelFactory.createStandardBoard(!humanIsWhite);
        renderer = new CheckDecoratorRenderer(new ChessRenderer(model), model);
        logic    = model.getGameLogic();

        initUI();            // setup Stage, Skin, Dialog + ESC listener
        hookInputs();        // combine UI and board input processors

        engineAdapter = new StockfishAdapter(difficulty);
        try {
            engineAdapter.startEngine();
        } catch (Exception e) {
            Gdx.app.error("BotGame", "Failed to initialize chess engine", e);
            game.setScreen(new GameOverScreen(game, "Failed to start chess engine"));
        }
    }

    private void initUI() {
        uiStage = new Stage(new ScreenViewport());
        skin    = new Skin(Gdx.files.internal("skins/uiskin.json"));

        confirmExitDialog = new Dialog("Confirm Exit", skin) {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    game.setScreen(new MainMenuScreen(game));
                }
            }
        };
        confirmExitDialog.text("Return to main menu?");
        confirmExitDialog.button("Yes", true);
        confirmExitDialog.button("No", false);

        uiStage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    confirmExitDialog.show(uiStage);
                    return true;
                }
                return false;
            }
        });
    }

    private void hookInputs() {
        ChessInputProcessor boardInput = new ChessInputProcessor(game, model, camera, renderer);
        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(uiStage);
        mux.addProcessor(boardInput);
        Gdx.input.setInputProcessor(mux);
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
     * Record a non-promotion move.
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

    // ——————— GAME LOOP ———————

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderer.render(batch);
        batch.end();

        boolean whiteToMove = logic.isWhiteTurn();
        boolean botTurn = (whiteToMove && !humanIsWhite) || (!whiteToMove && humanIsWhite);

        if (botTurn && !botThinking) {
            botThinking = true;
            new Thread(this::thinkAndMove).start();
        }

        uiStage.act(delta);
        uiStage.draw();
    }

    private void thinkAndMove() {
        try {
            String moves = String.join(" ", moveHistory);
            String bestMove = engineAdapter.getBestMove(moves);

            if (bestMove != null && bestMove.length() >= 4) {
                final String engineUCI = bestMove;
                final int fx = engineUCI.charAt(0) - 'a';
                final int fy = engineUCI.charAt(1) - '1';
                final int tx = engineUCI.charAt(2) - 'a';
                final int ty = engineUCI.charAt(3) - '1';
                final boolean isPromo = engineUCI.length() == 5;
                final char promC = isPromo ? engineUCI.charAt(4) : ' ';

                Thread.sleep(500); // Small delay for better UX

                Gdx.app.postRunnable(() -> {
                    applyEngineMove(fx, fy, tx, ty, isPromo, promC, engineUCI);
                    botThinking = false;
                });
            } else {
                botThinking = false;
            }
        } catch (Exception e) {
            Gdx.app.error("BotGame", "Bot thinking failed", e);
            botThinking = false;
        }
    }

    private void applyEngineMove(int fx, int fy, int tx, int ty, boolean isPromo, char promC, String engineUCI) {
        List<ChessPiece> pieces = model.getPieces();
        ChessPiece mover = findPieceAt(fx, fy);

        // Handle castling first
        if (mover != null && mover.getType().equalsIgnoreCase("king") && Math.abs(tx - fx) == 2) {
            handleCastling(mover, fx, fy, tx, ty);
        }

        // Handle captures - must happen BEFORE the move is applied
        ChessPiece capturedPiece;
        if (mover != null) {
            capturedPiece = handleCapture(mover, tx, ty, pieces);
        } else {
            capturedPiece = null;
        }

        // Now apply the move or promotion
        if (mover != null) {
            if (isPromo && promC != ' ') {
                handlePromotion(mover, tx, ty, promC, pieces);
            } else {
                handleRegularMove(mover, fx, fy, tx, ty);
            }
        }

        // Important: Force a render update here to prevent visual glitches
        Gdx.app.postRunnable(() -> {
            if (capturedPiece != null) {
                pieces.remove(capturedPiece);
            }
        });

        // Record move and toggle turn
        moveHistory.add(engineUCI);
        logic.toggleTurn();

        // Update decorators and check game state
        updateDecoratorsAndGameState(pieces, mover, tx, ty, isPromo);
    }

    private ChessPiece handleCapture(ChessPiece mover, int tx, int ty, List<ChessPiece> pieces) {
        // Regular capture
        ChessPiece targetPiece = findPieceAt(tx, ty);
        if (targetPiece != null && targetPiece != mover) {
            SoundManager.playCapture();
            return targetPiece;
        }

        // En passant capture
        if (mover.getType().equalsIgnoreCase("pawn")) {
            int epTargetX = logic.getEnPassantTargetX();
            int epTargetY = logic.getEnPassantTargetY();
            if (epTargetX == tx && epTargetY == ty) {
                ChessPiece epPiece = findPieceAt(tx, mover.getYPos());
                if (epPiece != null) {
                    SoundManager.playCapture();
                    return epPiece;
                }
            }
        }
        return null;
    }

    private void handleRegularMove(ChessPiece piece, int fx, int fy, int tx, int ty) {
        piece.setPosition(tx, ty);
        SoundManager.playMove();

        // Update en passant status
        if (piece.getType().equalsIgnoreCase("pawn")) {
            if (Math.abs(ty - fy) == 2) {
                logic.setEnPassantTarget(tx, (fy + ty) / 2, piece);
            } else {
                logic.clearEnPassantTarget();
            }
        } else {
            logic.clearEnPassantTarget();
        }
    }



    private void handleCastling(ChessPiece king, int fx, int fy, int tx, int ty) {
        int rookFromX = (tx > fx) ? 7 : 0;
        int rookToX = (tx > fx) ? fx + 1 : fx - 1;
        ChessPiece rook = findPieceAt(rookFromX, fy);
        if (rook != null && rook.getType().equalsIgnoreCase("rook")) {
            rook.setPosition(rookToX, fy);
        }
    }



    private void handlePromotion(ChessPiece pawn, int tx, int ty, char promC, List<ChessPiece> pieces) {
        pieces.remove(pawn);
        pieces.add(new ChessPiece(pawn.getColor(), promTypeFromChar(promC), tx, ty));
        logic.clearEnPassantTarget();
        SoundManager.playPromote();
    }


    private void updateDecoratorsAndGameState(List<ChessPiece> pieces, ChessPiece mover, int tx, int ty, boolean isPromo) {
        // Clear all decorators
        for (ChessPiece p : pieces) p.clearDecorators();

        // Highlight moved piece
        ChessPiece moved = (mover != null && isPromo) ? findPieceAt(tx, ty) : mover;
        if (moved != null) moved.addDecorator(new HighlightDecorator());

        // Check game end conditions
        String next = logic.isWhiteTurn() ? "white" : "black";
        if (logic.isCheckmate(next, pieces)) {
            game.setScreen(new GameOverScreen(game, "Checkmate! " + (next.equals("white") ? "Black" : "White") + " wins."));
        } else if (logic.isStalemate(next, pieces)) {
            game.setScreen(new GameOverScreen(game, "Stalemate! The game is a draw."));
        }
    }

    /** Applies a human-pawn promotion. */
    public void applyPromotion(ChessPiece pawn, int fx, int fy, int tx, int ty, String newType) {
        model.getPieces().remove(pawn);
        model.getPieces().add(ChessPieceFactory.create(pawn.getColor(), newType, tx, ty));
        logic.clearEnPassantTarget();
        recordHumanMove(fx, fy, tx, ty, newType.toLowerCase().charAt(0));
        logic.toggleTurn();
        botThinking = false;
        hookInputs();
    }

    private ChessPiece findPieceAt(int x, int y) {
        for (ChessPiece p : model.getPieces()) {
            if (p.getXPos() == x && p.getYPos() == y) {
                return p;
            }
        }
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

    @Override
    public void resize(int width, int height) {
    }
    @Override
    public void show() {}
    @Override
    public void hide() {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void dispose() {
        batch.dispose();
        renderer.dispose();
        engineAdapter.stopEngine();

        uiStage.dispose();
        skin.dispose();
    }
}
