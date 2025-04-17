package com.mygdx.chess.input;

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
    private final ChessGame game;
    private final ChessBoard chessBoard;
    private final OrthographicCamera camera;
    private ChessPiece selectedPiece = null;

    public ChessInputProcessor(ChessGame game, ChessBoard board, OrthographicCamera camera) {
        this.game = game;
        this.chessBoard = board;
        this.camera = camera;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Convert screen click to board coordinates
        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
        int rawX = (int) (world.x / ChessBoard.SQUARE_SIZE);
        int rawY = (int) (world.y / ChessBoard.SQUARE_SIZE);

        // Flip both axes if playing as black
        boolean flip = chessBoard.isFlipY();
        int boardX = flip ? 7 - rawX : rawX;
        int boardY = flip ? 7 - rawY : rawY;

        if (selectedPiece == null) {
            // Select a piece
            for (ChessPiece p : chessBoard.getPieces()) {
                if (p.getXPos() == boardX && p.getYPos() == boardY) {
                    selectedPiece = p;
                    List<Move> moves = chessBoard.getGameLogic()
                        .getPossibleMoves(p, chessBoard.getPieces());
                    chessBoard.setPossibleMoves(moves);
                    return true;
                }
            }
        } else {
            GameLogic logic = chessBoard.getGameLogic();
            int originalX = selectedPiece.getXPos();
            int originalY = selectedPiece.getYPos();

            if (logic.isValidMove(selectedPiece, boardX, boardY, chessBoard.getPieces())) {
                // ---- Handle castling ----
                if (selectedPiece.getType().equalsIgnoreCase("king")
                    && Math.abs(boardX - originalX) == 2) {
                    int y = originalY;
                    if (boardX > originalX) {
                        ChessPiece rook = findPieceAt(7, y, selectedPiece.getColor());
                        if (rook != null) rook.setPosition(originalX + 1, y);
                    } else {
                        ChessPiece rook = findPieceAt(0, y, selectedPiece.getColor());
                        if (rook != null) rook.setPosition(originalX - 1, y);
                    }
                }

                // ---- Detect en passant ----
                boolean isEnPassant = false;
                if (selectedPiece.getType().equalsIgnoreCase("pawn")
                    && Math.abs(boardX - originalX) == 1
                    && boardY == logic.getEnPassantTargetY()
                    && boardX == logic.getEnPassantTargetX()) {
                    isEnPassant = true;
                }

                // ---- Perform capture ----
                if (!isEnPassant) {
                    Iterator<ChessPiece> iter = chessBoard.getPieces().iterator();
                    while (iter.hasNext()) {
                        ChessPiece p = iter.next();
                        if (p.getXPos() == boardX && p.getYPos() == boardY
                            && !p.getColor().equals(selectedPiece.getColor())) {
                            iter.remove();
                            break;
                        }
                    }
                } else {
                    int capY = selectedPiece.getColor().equalsIgnoreCase("white")
                        ? boardY - 1
                        : boardY + 1;
                    Iterator<ChessPiece> iter = chessBoard.getPieces().iterator();
                    while (iter.hasNext()) {
                        ChessPiece p = iter.next();
                        if (p.getXPos() == boardX && p.getYPos() == capY) {
                            iter.remove();
                            break;
                        }
                    }
                }

                // ---- Move the piece ----
                selectedPiece.setPosition(boardX, boardY);

                // ---- Promotion check ----
                if (selectedPiece.getType().equalsIgnoreCase("pawn")
                    && ((selectedPiece.getColor().equalsIgnoreCase("white") && boardY == 7)
                    || (selectedPiece.getColor().equalsIgnoreCase("black") && boardY == 0))) {
                    game.setScreen(new PromotionScreen(game, chessBoard, selectedPiece));
                    selectedPiece = null;
                    chessBoard.setPossibleMoves(null);
                    return true;
                }

                // ---- Set or clear en passant target ----
                if (selectedPiece.getType().equalsIgnoreCase("pawn")
                    && Math.abs(boardY - originalY) == 2) {
                    int targetY = selectedPiece.getColor().equalsIgnoreCase("white")
                        ? originalY + 1
                        : originalY - 1;
                    logic.setEnPassantTarget(originalX, targetY, selectedPiece);
                } else {
                    logic.clearEnPassantTarget();
                }

                // ---- Toggle turn & check for endgame ----
                logic.toggleTurn();
                String nextColor = logic.isWhiteTurn() ? "white" : "black";
                if (logic.isCheckmate(nextColor, chessBoard.getPieces())) {
                    String winner = nextColor.equals("white") ? "Black" : "White";
                    game.setScreen(new GameOverScreen(game, "Checkmate! " + winner + " wins."));
                } else if (logic.isStalemate(nextColor, chessBoard.getPieces())) {
                    game.setScreen(new GameOverScreen(game, "Stalemate! The game is a draw."));
                }

                // ---- Cleanup ----
                selectedPiece = null;
                chessBoard.setPossibleMoves(null);
                return true;
            }

            // Invalid move: deselect
            selectedPiece = null;
            chessBoard.setPossibleMoves(null);
            return true;
        }

        return true;
    }

    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override public boolean touchDragged(int screenX, int screenY, int pointer)         { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY)                        { return false; }
    @Override public boolean scrolled(float amountX, float amountY)                      { return false; }
    @Override public boolean keyDown(int keycode)                                        { return false; }
    @Override public boolean keyUp(int keycode)                                          { return false; }
    @Override public boolean keyTyped(char character)                                    { return false; }

    private ChessPiece findPieceAt(int x, int y, String color) {
        for (ChessPiece p : chessBoard.getPieces()) {
            if (p.getXPos() == x && p.getYPos() == y && p.getColor().equals(color)) {
                return p;
            }
        }
        return null;
    }
}
