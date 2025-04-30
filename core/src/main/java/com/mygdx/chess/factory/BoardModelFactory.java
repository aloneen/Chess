package com.mygdx.chess.factory;

import com.mygdx.chess.model.BoardModel;
import com.mygdx.chess.model.IBoardModel;
import com.mygdx.chess.actors.ChessPiece;

import java.util.List;

/**
 * Factory to initialize a BoardModel with the standard chess starting setup.
 */
public class BoardModelFactory {

    /**
     * Creates a BoardModel with pieces in the standard initial positions.
     * @param flipY if true, black is at the bottom
     */
    public static IBoardModel createStandardBoard(boolean flipY) {
        IBoardModel model = new BoardModel(flipY);
        List<ChessPiece> pieces = model.getPieces();
        pieces.clear();
        // White pawns
        for (int x = 0; x < 8; x++) {
            pieces.add(ChessPieceFactory.create("white", "pawn", x, 1));
        }
        // Black pawns
        for (int x = 0; x < 8; x++) {
            pieces.add(ChessPieceFactory.create("black", "pawn", x, 6));
        }
        // Back ranks
        String[] order = {"rook","knight","bishop","queen","king","bishop","knight","rook"};
        for (int x = 0; x < 8; x++) {
            pieces.add(ChessPieceFactory.create("white", order[x], x, 0));
            pieces.add(ChessPieceFactory.create("black", order[x], x, 7));
        }
        return model;
    }
}
