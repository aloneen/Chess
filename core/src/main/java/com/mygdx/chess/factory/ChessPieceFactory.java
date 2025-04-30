package com.mygdx.chess.factory;

import com.mygdx.chess.actors.ChessPiece;

/**
 * Factory for creating ChessPiece instances by type and color.
 */
public class ChessPieceFactory {

    /**
     * Creates a ChessPiece of the given color and type at (x, y).
     * @param color "white" or "black"
     * @param type  "pawn", "knight", "bishop", "rook", "queen", or "king"
     * @param x     board file (0–7)
     * @param y     board rank (0–7)
     */
    public static ChessPiece create(String color, String type, int x, int y) {
        return new ChessPiece(color, type, x, y);
    }
}
