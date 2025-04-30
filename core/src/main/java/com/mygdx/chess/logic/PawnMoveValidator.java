package com.mygdx.chess.logic;

import com.mygdx.chess.actors.ChessPiece;
import java.util.List;

/**
 * Validates pawn moves (single/double step, capture, en passant).
 */
public class PawnMoveValidator implements IMoveValidator {
    @Override
    public boolean isValid(
        ChessPiece piece,
        int destX, int destY,
        List<ChessPiece> allPieces,
        ChessPiece[][] boardState,
        GameLogic logic
    ) {
        int startX = piece.getXPos();
        int startY = piece.getYPos();
        String color = piece.getColor();
        int dir = color.equals("white") ? 1 : -1;

        // Single-step forward
        if (destX == startX && destY - startY == dir) {
            return boardState[destX][destY] == null;
        }

        // Double-step on first move
        if (destX == startX && destY - startY == 2 * dir) {
            boolean firstMove = (color.equals("white") && startY == 1)
                || (color.equals("black") && startY == 6);
            return firstMove
                && boardState[startX][startY + dir] == null
                && boardState[destX][destY] == null;
        }

        // Diagonal capture / en passant
        if (Math.abs(destX - startX) == 1 && destY - startY == dir) {
            ChessPiece target = boardState[destX][destY];
            // normal capture
            if (target != null && !target.getColor().equals(color)) {
                return true;
            }
            // en passant
            if (target == null
                && destX == logic.getEnPassantTargetX()
                && destY == logic.getEnPassantTargetY()) {
                return true;
            }
        }

        return false;
    }
}
