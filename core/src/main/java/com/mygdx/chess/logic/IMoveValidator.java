package com.mygdx.chess.logic;

import com.mygdx.chess.actors.ChessPiece;
import java.util.List;

/**
 * Strategy interface for piece-specific move validation.
 */
public interface IMoveValidator {
    /**
     * @param piece the moving piece
     * @param destX target column (0–7)
     * @param destY target row (0–7)
     * @param allPieces list of all pieces
     * @param boardState current board[x][y] array
     * @param logic GameLogic for context (e.g., en passant)
     * @return true if move is valid for this piece
     */
    boolean isValid(
        ChessPiece piece,
        int destX, int destY,
        List<ChessPiece> allPieces,
        ChessPiece[][] boardState,
        GameLogic logic
    );
}
