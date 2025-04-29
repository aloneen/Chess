package com.mygdx.chess.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.chess.ChessGame;
import com.mygdx.chess.actors.ChessBoard;
import com.mygdx.chess.actors.ChessPiece;
import com.mygdx.chess.logic.GameLogic;
import com.mygdx.chess.logic.Move;
import com.mygdx.chess.screens.GameOverScreen;
import com.mygdx.chess.screens.PromotionScreen;

import java.util.Iterator;
import java.util.List;

public class ChessInputProcessor implements InputProcessor {
    private ChessGame game;
    private ChessBoard chessBoard;
    private OrthographicCamera camera;
    private ChessPiece selectedPiece = null;

    public ChessInputProcessor(ChessGame game, ChessBoard board, OrthographicCamera camera) {
        this.game = game;
        this.chessBoard = board;
        this.camera = camera;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
        int rawX = (int)(world.x / ChessBoard.SQUARE_SIZE);
        int rawY = (int)(world.y / ChessBoard.SQUARE_SIZE);
        int boardX = rawX;
        int boardY = chessBoard.isFlipY() ? (7 - rawY) : rawY;

        if (selectedPiece == null) {
            // select piece
            for (ChessPiece p : chessBoard.getPieces()) {
                if (p.getXPos() == boardX && p.getYPos() == boardY) {
                    selectedPiece = p;
                    List<Move> moves = chessBoard.getGameLogic()
                        .getPossibleMoves(selectedPiece, chessBoard.getPieces());
                    chessBoard.setPossibleMoves(moves);
                    return true;
                }
            }
        } else {
            GameLogic logic = chessBoard.getGameLogic();
            int originalX = selectedPiece.getXPos();
            int originalY = selectedPiece.getYPos();

            if (logic.isValidMove(selectedPiece, boardX, boardY, chessBoard.getPieces())) {
                // ---- handle castling ----
                if (selectedPiece.getType().equalsIgnoreCase("king") &&
                    Math.abs(boardX - originalX) == 2) {
                    int startY = originalY;
                    if (boardX > originalX) {
                        // kingside
                        ChessPiece rook = findPieceAt(7, startY, selectedPiece.getColor());
                        if (rook != null) rook.setPosition(originalX + 1, startY);
                    } else {
                        // queenside
                        ChessPiece rook = findPieceAt(0, startY, selectedPiece.getColor());
                        if (rook != null) rook.setPosition(originalX - 1, startY);
                    }
                }

                // ---- detect en passant capture ----
                boolean isEnPassant = false;
                if (selectedPiece.getType().equalsIgnoreCase("pawn") &&
                    Math.abs(boardX - originalX) == 1 &&
                    boardY == logic.getEnPassantTargetY() &&
                    boardX == logic.getEnPassantTargetX()) {
                    isEnPassant = true;
                }

                // ---- perform capture ----
                if (!isEnPassant) {
                    // normal capture
                    Iterator<ChessPiece> iter = chessBoard.getPieces().iterator();
                    while (iter.hasNext()) {
                        ChessPiece p = iter.next();
                        if (p.getXPos() == boardX && p.getYPos() == boardY &&
                            !p.getColor().equals(selectedPiece.getColor())) {
                            iter.remove();
                            break;
                        }
                    }
                } else {
                    // remove the pawn that moved two squares last turn
                    int capturedPawnY = selectedPiece.getColor().equalsIgnoreCase("white")
                        ? boardY - 1
                        : boardY + 1;
                    Iterator<ChessPiece> iter = chessBoard.getPieces().iterator();
                    while (iter.hasNext()) {
                        ChessPiece p = iter.next();
                        if (p.getXPos() == boardX && p.getYPos() == capturedPawnY) {
                            iter.remove();
                            break;
                        }
                    }
                }

                // ---- move the selected piece ----
                selectedPiece.setPosition(boardX, boardY);

                // promotion
                if (selectedPiece.getType().equalsIgnoreCase("pawn") &&
                    ((selectedPiece.getColor().equalsIgnoreCase("white") && boardY == 7) ||
                        (selectedPiece.getColor().equalsIgnoreCase("black") && boardY == 0))) {
                    game.setScreen(new PromotionScreen(game, chessBoard, selectedPiece));
                    selectedPiece = null;
                    chessBoard.setPossibleMoves(null);
                    return true;
                }

                // en passant target, turn toggle, endgame check...
                logic.toggleTurn();
                String currentTurn = logic.isWhiteTurn() ? "white" : "black";
                if (logic.isCheckmate(currentTurn, chessBoard.getPieces())) {
                    String winner = currentTurn.equals("white") ? "Black" : "White";
                    game.setScreen(new GameOverScreen(game, "Checkmate! " + winner + " wins."));
                } else if (logic.isStalemate(currentTurn, chessBoard.getPieces())) {
                    game.setScreen(new GameOverScreen(game, "Stalemate! The game is a draw."));
                }

                selectedPiece = null;
                chessBoard.setPossibleMoves(null);
                return true;
            }

            // invalid move: deselect
            selectedPiece = null;
            chessBoard.setPossibleMoves(null);
            return true;
        }

        return true;
    }


    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int i, int i1, int i2, int i3) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }

    private ChessPiece findPieceAt(int x, int y, String color) {
        for (ChessPiece p : chessBoard.getPieces()) {
            if (p.getXPos() == x && p.getYPos() == y && p.getColor().equals(color))
                return p;
        }
        return null;
    }
}
