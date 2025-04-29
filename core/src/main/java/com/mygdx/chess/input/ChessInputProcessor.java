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
    private ChessGame game; // Reference to the main game instance.
    private ChessBoard chessBoard;
    private OrthographicCamera camera;
    private ChessPiece selectedPiece = null;

    // Constructor now accepts the ChessGame instance, ChessBoard, and camera.
    public ChessInputProcessor(ChessGame game, ChessBoard board, OrthographicCamera camera) {
        this.game = game;
        this.chessBoard = board;
        this.camera = camera;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 worldCoordinates = camera.unproject(new Vector3(screenX, screenY, 0));
        int boardX = (int)(worldCoordinates.x / ChessBoard.SQUARE_SIZE);
        int boardY = (int)(worldCoordinates.y / ChessBoard.SQUARE_SIZE);

        if (selectedPiece == null) {
            for (ChessPiece piece : chessBoard.getPieces()) {
                if (piece.getXPos() == boardX && piece.getYPos() == boardY) {
                    selectedPiece = piece;
                    List<Move> moves = chessBoard.getGameLogic().getPossibleMoves(selectedPiece, chessBoard.getPieces());
                    chessBoard.setPossibleMoves(moves);
                    return true;
                }
            }
        } else {
            GameLogic logic = chessBoard.getGameLogic();
            if (logic.isValidMove(selectedPiece, boardX, boardY, chessBoard.getPieces())) {
                // Handle castling.
                if (selectedPiece.getType().equalsIgnoreCase("king") && Math.abs(boardX - selectedPiece.getXPos()) == 2) {
                    int startX = selectedPiece.getXPos();
                    int startY = selectedPiece.getYPos();
                    if (boardX > startX) { // kingside castling
                        ChessPiece rook = findPieceAt(7, startY, selectedPiece.getColor());
                        if (rook != null) {
                            rook.setPosition(startX + 1, startY);
                        }
                    } else { // queenside castling
                        ChessPiece rook = findPieceAt(0, startY, selectedPiece.getColor());
                        if (rook != null) {
                            rook.setPosition(startX - 1, startY);
                        }
                    }
                }
                // Capture logic.
                Iterator<ChessPiece> iter = chessBoard.getPieces().iterator();
                while (iter.hasNext()) {
                    ChessPiece piece = iter.next();
                    if (piece.getXPos() == boardX && piece.getYPos() == boardY &&
                        !piece.getColor().equals(selectedPiece.getColor())) {
                        iter.remove();
                        break;
                    }
                }
                // Move the selected piece.
                selectedPiece.setPosition(boardX, boardY);

                // Check for pawn promotion.
                if(selectedPiece.getType().equalsIgnoreCase("pawn") &&
                    ((selectedPiece.getColor().equalsIgnoreCase("white") && boardY == 7) ||
                        (selectedPiece.getColor().equalsIgnoreCase("black") && boardY == 0))) {
                    // Switch to the promotion selection screen.
                    game.setScreen(new PromotionScreen(game, chessBoard, selectedPiece));
                    selectedPiece = null;
                    chessBoard.setPossibleMoves(null);
                    return true;
                }

                // No promotion: toggle turn and check for game end.
                logic.toggleTurn();
                String currentTurnColor = logic.isWhiteTurn() ? "white" : "black";
                if (logic.isCheckmate(currentTurnColor, chessBoard.getPieces())) {
                    String winner = currentTurnColor.equals("white") ? "Black" : "White";
                    game.setScreen(new GameOverScreen(game, "Checkmate! " + winner + " wins."));
                    return true;
                } else if (logic.isStalemate(currentTurnColor, chessBoard.getPieces())) {
                    game.setScreen(new GameOverScreen(game, "Stalemate! The game is a draw."));
                    return true;
                }
            }
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
        for (ChessPiece piece : chessBoard.getPieces()) {
            if (piece.getXPos() == x && piece.getYPos() == y && piece.getColor().equals(color))
                return piece;
        }
        return null;
    }

}
