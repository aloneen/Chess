package com.mygdx.chess.logic;

import com.mygdx.chess.actors.ChessPiece;
import java.util.List;

/**
 * Validates knight moves (L-shape).
 */
public class KnightMoveValidator implements IMoveValidator {
    @Override
    public boolean isValid(
        ChessPiece piece,
        int destX, int destY,
        List<ChessPiece> allPieces,
        ChessPiece[][] boardState,
        GameLogic logic
    ) {
        int dx = Math.abs(destX - piece.getXPos());
        int dy = Math.abs(destY - piece.getYPos());
        return (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
    }
}
