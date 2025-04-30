package com.mygdx.chess.logic;

import com.mygdx.chess.actors.ChessPiece;
import java.util.List;

/**
 * Validates rook moves (horizontal or vertical, path clear).
 */
public class RookMoveValidator implements IMoveValidator {
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
        if (startX != destX && startY != destY) {
            return false;
        }
        int dx = Integer.compare(destX, startX);
        int dy = Integer.compare(destY, startY);
        int x = startX + dx;
        int y = startY + dy;
        while (x != destX || y != destY) {
            if (boardState[x][y] != null) {
                return false;
            }
            x += dx;
            y += dy;
        }
        return true;
    }
}
