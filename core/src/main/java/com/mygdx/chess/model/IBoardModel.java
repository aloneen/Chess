
package com.mygdx.chess.model;

import com.mygdx.chess.actors.ChessPiece;
import com.mygdx.chess.logic.GameLogic;
import com.mygdx.chess.logic.Move;

import java.util.List;

/**
 * Abstraction for board state and game logic.
 */
public interface IBoardModel {
    List<ChessPiece> getPieces();
    GameLogic getGameLogic();
    boolean isFlipped();
    List<Move> getPossibleMoves();
    void       setPossibleMoves(List<Move> moves);
}
