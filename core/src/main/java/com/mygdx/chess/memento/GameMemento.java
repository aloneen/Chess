package com.mygdx.chess.memento;

import com.mygdx.chess.actors.ChessPiece;

import java.util.ArrayList;
import java.util.List;

public class GameMemento {
    private final List<ChessPiece> pieceSnapshot;
    private final boolean whiteTurn;
    private final int enPassantX, enPassantY;
    private final ChessPiece enPassantPawn;

    public GameMemento(List<ChessPiece> pieces, boolean whiteTurn,
                       int enPassantX, int enPassantY, ChessPiece enPassantPawn) {
        // Deep clone pieces
        this.pieceSnapshot = new ArrayList<>();
        for (ChessPiece p : pieces) {
            this.pieceSnapshot.add(p.clone());
        }

        this.whiteTurn = whiteTurn;
        this.enPassantX = enPassantX;
        this.enPassantY = enPassantY;
        this.enPassantPawn = enPassantPawn != null ? enPassantPawn.clone() : null;
    }

    public List<ChessPiece> getPieceSnapshot() {
        // Return deep copy to prevent mutation
        List<ChessPiece> copy = new ArrayList<>();
        for (ChessPiece p : pieceSnapshot) {
            copy.add(p.clone());
        }
        return copy;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public int getEnPassantX() {
        return enPassantX;
    }

    public int getEnPassantY() {
        return enPassantY;
    }

    public ChessPiece getEnPassantPawn() {
        return enPassantPawn != null ? enPassantPawn.clone() : null;
    }
}
