package com.mygdx.chess.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.chess.ChessGame;
import com.mygdx.chess.actors.ChessPiece;

import com.mygdx.chess.logic.GameLogic;
import com.mygdx.chess.logic.Move;
import com.mygdx.chess.memento.GameMemento;
import com.mygdx.chess.model.IBoardModel;
import com.mygdx.chess.screens.BotGameScreen;
import com.mygdx.chess.screens.PromotionScreen;
import com.mygdx.chess.screens.GameOverScreen;
import com.mygdx.chess.view.IChessRenderer;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import static com.mygdx.chess.util.BoardConfig.SQUARE_SIZE;

/**
 * Processes touch input for selecting and moving pieces,
 * including en passant, castling, and bot‐turn restrictions.
 */
public class ChessInputProcessor implements InputProcessor, IGameInputProcessor {
    private final ChessGame         game;
    private final IBoardModel        boardModel;
    private final OrthographicCamera camera;
    private final IChessRenderer     renderer;
    private ChessPiece               selected;

    private final Stack<GameMemento> mementoStack = new Stack<>();

    public ChessInputProcessor(
        ChessGame game,
        IBoardModel model,
        OrthographicCamera camera,
        IChessRenderer renderer
    ) {
        this.game       = game;
        this.boardModel = model;
        this.camera     = camera;
        this.renderer   = renderer;
        this.selected   = null;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        GameLogic logic = boardModel.getGameLogic();
        List<ChessPiece> pieces = boardModel.getPieces();
        mementoStack.push(boardModel.createMemento());


        // 1) Bot‐mode: ignore taps when it's the engine’s turn
        if (game.getScreen() instanceof BotGameScreen) {
            BotGameScreen bgs = (BotGameScreen)game.getScreen();
            boolean whiteToMove = logic.isWhiteTurn();
            boolean humanTurn   = (whiteToMove && bgs.isHumanWhite())
                || (!whiteToMove && !bgs.isHumanWhite());
            if (!humanTurn) return false;
        }

        // 2) Convert to board coords
        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
        int rawX = (int)(world.x / SQUARE_SIZE);
        int rawY = (int)(world.y / SQUARE_SIZE);
        boolean flip = boardModel.isFlipped();
        int boardX = flip ? 7 - rawX : rawX;
        int boardY = flip ? 7 - rawY : rawY;

        // 3) Select piece
        if (selected == null) {
            for (ChessPiece p : pieces) {
                if (p.getXPos() == boardX && p.getYPos() == boardY) {
                    selected = p;
                    boardModel.setPossibleMoves(logic.getPossibleMoves(p, pieces));
                    return true;
                }
            }
            return false;
        }

        // 4) Attempt move
        int startX = selected.getXPos();
        int startY = selected.getYPos();

        boolean isEnPassant = selected.getType().equalsIgnoreCase("pawn")
            && Math.abs(boardX - startX) == 1
            && boardY == logic.getEnPassantTargetY()
            && boardX == logic.getEnPassantTargetX();

        if (logic.isValidMove(selected, boardX, boardY, pieces)) {
            // ---- CASTLING ----
            if (selected.getType().equalsIgnoreCase("king")
                && Math.abs(boardX - startX) == 2) {
                int rookStartX = (boardX > startX) ? 7 : 0;
                int rookDestX  = (boardX > startX) ? boardX - 1 : boardX + 1;
                for (ChessPiece p : pieces) {
                    if (p.getType().equalsIgnoreCase("rook")
                        && p.getXPos() == rookStartX
                        && p.getYPos() == startY
                        && p.getColor().equals(selected.getColor())) {
                        p.setPosition(rookDestX, startY);
                        break;
                    }
                }
            }

            // ---- CAPTURE / EN PASSANT ----
            if (isEnPassant) {
                int capY = selected.getColor().equalsIgnoreCase("white")
                    ? boardY - 1 : boardY + 1;
                pieces.removeIf(p -> p.getXPos() == boardX && p.getYPos() == capY);
            } else {
                pieces.removeIf(p ->
                    p != selected
                        && p.getXPos() == boardX
                        && p.getYPos() == boardY
                );
            }

            // ---- MOVE PIECE ----
            selected.setPosition(boardX, boardY);







            // ---- SET/CLEAR EN PASSANT TARGET ----
            if (selected.getType().equalsIgnoreCase("pawn")
                && Math.abs(boardY - startY) == 2) {
                int targetY = selected.getColor().equalsIgnoreCase("white")
                    ? startY + 1 : startY - 1;
                logic.setEnPassantTarget(startX, targetY, selected);
            } else {
                logic.clearEnPassantTarget();
            }

            // ---- PROMOTION ----
            if (selected.getType().equalsIgnoreCase("pawn")
                && ((selected.getColor().equalsIgnoreCase("white") && boardY == 7)
                || (selected.getColor().equalsIgnoreCase("black") && boardY == 0))) {
                ChessPiece pawn = selected;
                selected = null;
                boardModel.setPossibleMoves(null);
                game.setScreen(new PromotionScreen(
                    game, boardModel, renderer, pawn
                ));
                return true;
            }

            // ---- RECORD HUMAN MOVE FOR BOT ----
            if (game.getScreen() instanceof BotGameScreen) {
                ((BotGameScreen)game.getScreen())
                    .recordHumanMove(startX, startY, boardX, boardY);
            }

            // ---- TOGGLE TURN & ENDGAME ----
            logic.toggleTurn();
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

            // ---- CLEANUP ----
            selected = null;
            boardModel.setPossibleMoves(null);
            return true;
        }

        // ---- INVALID MOVE: DESELECT ----
        selected = null;
        boardModel.setPossibleMoves(null);
        return true;
    }





    // Other InputProcessor stubs — no changes
    @Override
    public boolean keyDown(int keycode) {
        if (!(game.getScreen() instanceof BotGameScreen) && keycode == Input.Keys.R) {
            if (!mementoStack.isEmpty()) {
                GameMemento last = mementoStack.pop();
                boardModel.restoreMemento(last);
                boardModel.setPossibleMoves(null);
            }
            return true;
        }
        return false;
    }

    @Override public boolean keyUp(int keycode)                     { return false; }
    @Override public boolean keyTyped(char character)               { return false; }
    @Override public boolean touchUp(int x, int y, int p, int b)    { return false; }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override public boolean touchDragged(int x, int y, int p)      { return false; }
    @Override public boolean mouseMoved(int x, int y)               { return false; }
    @Override public boolean scrolled(float ax, float ay)           { return false; }
}
