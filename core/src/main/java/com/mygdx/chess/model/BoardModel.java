package com.mygdx.chess.model;

import com.mygdx.chess.actors.ChessPiece;
import com.mygdx.chess.logic.GameLogic;
import com.mygdx.chess.logic.Move;
import com.mygdx.chess.memento.GameMemento;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete model holding pieces and logic.
 */
public class BoardModel implements IBoardModel {
    private final boolean flip;
    private final List<ChessPiece> pieces = new ArrayList<>();
    private final GameLogic logic = GameLogic.getInstance();
    private List<Move> possibleMoves = null;

    public BoardModel(boolean flip) {
        this.flip = flip;
        GameLogic.getInstance().reset();
        initializePieces();
    }

    private void initializePieces() {
        // White pawns
        for (int x = 0; x < 8; x++) pieces.add(new ChessPiece("white", "pawn", x, 1));
        // Black pawns
        for (int x = 0; x < 8; x++) pieces.add(new ChessPiece("black", "pawn", x, 6));
        // Back ranks
        String[] order = {"rook","knight","bishop","queen","king","bishop","knight","rook"};
        for (int i = 0; i < 8; i++) {
            pieces.add(new ChessPiece("white", order[i], i, 0));
            pieces.add(new ChessPiece("black", order[i], i, 7));
        }
    }

    @Override public List<Move> getPossibleMoves() {
        return possibleMoves;
    }

    @Override public void setPossibleMoves(List<Move> moves) {
        this.possibleMoves = moves;
    }


    @Override
    public List<ChessPiece> getPieces() {
        return pieces;
    }

    @Override
    public GameLogic getGameLogic() {
        return logic;
    }

    @Override
    public boolean isFlipped() {
        return flip;
    }










    @Override
    public GameMemento createMemento() {
        return new GameMemento(
            getPieces(),
            logic.isWhiteTurn(),
            logic.getEnPassantTargetX(),
            logic.getEnPassantTargetY(),
            logic.getEnPassantVulnerablePawn()
        );
    }

    @Override
    public void restoreMemento(GameMemento memento) {
        pieces.clear();
        pieces.addAll(memento.getPieceSnapshot());

        logic.updateBoardState(pieces); // Sync logic’s internal board
        if (memento.isWhiteTurn() != logic.isWhiteTurn()) {
            logic.toggleTurn(); // Fix turn
        }

        logic.setEnPassantTarget(
            memento.getEnPassantX(),
            memento.getEnPassantY(),
            memento.getEnPassantPawn()
        );
    }

}

