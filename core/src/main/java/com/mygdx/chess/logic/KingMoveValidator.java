package com.mygdx.chess.logic;

import com.mygdx.chess.actors.ChessPiece;
import java.util.List;

/**
 * Validates king moves, including castling.
 */
public class KingMoveValidator implements IMoveValidator {
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
        int dx = Math.abs(destX - startX);
        int dy = Math.abs(destY - startY);

        // Normal one-square move
        if (dx <= 1 && dy <= 1) {
            return true;
        }

        // Castling: two-square horizontal move
        if (dx == 2 && dy == 0) {
            // King must not have moved
            if (piece.hasMoved()) return false;

            // Determine rook position
            int rookX = (destX > startX) ? 7 : 0;

            // Find the rook at rookX, startY
            ChessPiece rook = findRook(allPieces, rookX, startY, piece.getColor());
            if (rook == null || !"rook".equalsIgnoreCase(rook.getType()) || rook.hasMoved()) {
                return false;
            }

            // Check path clear between king and rook (exclusive)
            int dir = (destX > startX) ? 1 : -1;
            for (int x = startX + dir; x != rookX; x += dir) {
                if (boardState[x][startY] != null) {
                    return false;
                }
            }

            // Check that king is not in check on any square it passes or lands
            for (int x = startX; x != destX + dir; x += dir) {
                if (logic.isSquareAttacked(x, startY, piece.getColor(), allPieces)) {
                    return false;
                }
            }

            return true;
        }

        // All other moves invalid
        return false;
    }

    /**
     * Locates a rook of the given color at the specified square.
     */
    private ChessPiece findRook(
        List<ChessPiece> pieces,
        int x, int y,
        String color
    ) {
        for (ChessPiece p : pieces) {
            if (p.getXPos() == x && p.getYPos() == y && color.equalsIgnoreCase(p.getColor())) {
                return p;
            }
        }
        return null;
    }
}
