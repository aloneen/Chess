package com.mygdx.chess.logic;

import com.mygdx.chess.actors.ChessPiece;
import java.util.List;

/**
 * Validates queen moves (combines rook and bishop).
 */
public class QueenMoveValidator implements IMoveValidator {
    private final RookMoveValidator   rookValidator   = new RookMoveValidator();
    private final BishopMoveValidator bishopValidator = new BishopMoveValidator();

    @Override
    public boolean isValid(
        ChessPiece piece,
        int destX, int destY,
        List<ChessPiece> allPieces,
        ChessPiece[][] boardState,
        GameLogic logic
    ) {
        return rookValidator.isValid(piece, destX, destY, allPieces, boardState, logic)
            || bishopValidator.isValid(piece, destX, destY, allPieces, boardState, logic);
    }
}
