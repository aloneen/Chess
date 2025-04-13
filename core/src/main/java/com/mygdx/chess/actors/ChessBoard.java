package com.mygdx.chess.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.chess.logic.GameLogic;
import com.mygdx.chess.logic.Move;
import java.util.ArrayList;
import java.util.List;

public class ChessBoard {
    public static final int BOARD_SIZE = 800;
    public static final int SQUARE_SIZE = BOARD_SIZE / 8; // equals 100

    private Texture boardTexture;
    private Texture moveIndicatorTexture;
    private List<ChessPiece> pieces;
    private GameLogic gameLogic;

    // List to hold legal moves for the currently selected piece
    private List<Move> possibleMoves;

    public ChessBoard() {
        boardTexture = new Texture(Gdx.files.internal("images/chess_board.png"));
        // Load a small dot texture to indicate possible moves
        moveIndicatorTexture = new Texture(Gdx.files.internal("images/move_indicator.png"));
        pieces = new ArrayList<>();
        gameLogic = new GameLogic();
        possibleMoves = new ArrayList<>();

        initializePieces();
    }

    private void initializePieces() {
        // White Pawns (row 1)
        for (int col = 0; col < 8; col++) {
            pieces.add(new ChessPiece("white", "pawn", col, 1));
        }
        // Black Pawns (row 6)
        for (int col = 0; col < 8; col++) {
            pieces.add(new ChessPiece("black", "pawn", col, 6));
        }
        // White pieces
        pieces.add(new ChessPiece("white", "rook", 0, 0));
        pieces.add(new ChessPiece("white", "rook", 7, 0));
        pieces.add(new ChessPiece("white", "knight", 1, 0));
        pieces.add(new ChessPiece("white", "knight", 6, 0));
        pieces.add(new ChessPiece("white", "bishop", 2, 0));
        pieces.add(new ChessPiece("white", "bishop", 5, 0));
        pieces.add(new ChessPiece("white", "queen", 3, 0));
        pieces.add(new ChessPiece("white", "king", 4, 0));
        // Black pieces
        pieces.add(new ChessPiece("black", "rook", 0, 7));
        pieces.add(new ChessPiece("black", "rook", 7, 7));
        pieces.add(new ChessPiece("black", "knight", 1, 7));
        pieces.add(new ChessPiece("black", "knight", 6, 7));
        pieces.add(new ChessPiece("black", "bishop", 2, 7));
        pieces.add(new ChessPiece("black", "bishop", 5, 7));
        pieces.add(new ChessPiece("black", "queen", 3, 7));
        pieces.add(new ChessPiece("black", "king", 4, 7));
    }

    public void update(float delta) {
        // Update animations or game logic if needed.
    }

    public void render(SpriteBatch batch) {
        // Draw the board.
        batch.draw(boardTexture, 0, 0, BOARD_SIZE, BOARD_SIZE);

        // Draw each chess piece, passing in the square size.
        for (ChessPiece piece : pieces) {
            piece.render(batch, SQUARE_SIZE);
        }

        // Draw move indicators if available.
        if (possibleMoves != null) {
            int indicatorSize = 20;
            for (Move move : possibleMoves) {
                float indicatorX = move.x * SQUARE_SIZE + (SQUARE_SIZE - indicatorSize) / 2f;
                float indicatorY = move.y * SQUARE_SIZE + (SQUARE_SIZE - indicatorSize) / 2f;
                batch.draw(moveIndicatorTexture, indicatorX, indicatorY, indicatorSize, indicatorSize);
            }
        }
    }

    public void dispose() {
        boardTexture.dispose();
        moveIndicatorTexture.dispose();
        for (ChessPiece piece : pieces) {
            piece.dispose();
        }
    }

    public List<ChessPiece> getPieces() {
        return pieces;
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }

    // Setter to update the possible moves list (or clear it with null)
    public void setPossibleMoves(List<Move> moves) {
        this.possibleMoves = moves;
    }
}
