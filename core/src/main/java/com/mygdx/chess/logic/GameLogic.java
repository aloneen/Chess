package com.mygdx.chess.logic;

import com.mygdx.chess.actors.ChessPiece;

import java.util.*;

public class GameLogic {

    private static GameLogic instance;
    private final Map<String, IMoveValidator> validatorMap = new HashMap<>();
    private ChessPiece[][] board;
    private boolean whiteTurn;

    // En passant state
    private int enPassantTargetX = -1;
    private int enPassantTargetY = -1;
    private ChessPiece enPassantVulnerablePawn = null;

    // Singleton Design Pattern
    public static synchronized GameLogic getInstance() {
        if (instance == null) {
            instance = new GameLogic();
        }
        return instance;
    }

    public GameLogic() {
        board = new ChessPiece[8][8];
        whiteTurn = true;

        validatorMap.put("pawn", new PawnMoveValidator());
        validatorMap.put("rook", new RookMoveValidator());
        validatorMap.put("knight", new KnightMoveValidator());
        validatorMap.put("bishop", new BishopMoveValidator());
        validatorMap.put("queen", new QueenMoveValidator());
        validatorMap.put("king", new KingMoveValidator());
    }

    public void setEnPassantTarget(int x, int y, ChessPiece pawn) {
        enPassantTargetX = x;
        enPassantTargetY = y;
        enPassantVulnerablePawn = pawn;
    }

    public void clearEnPassantTarget() {
        enPassantTargetX = enPassantTargetY = -1;
        enPassantVulnerablePawn = null;
    }

    public int getEnPassantTargetX() { return enPassantTargetX; }
    public int getEnPassantTargetY() { return enPassantTargetY; }
    public ChessPiece getEnPassantVulnerablePawn() { return enPassantVulnerablePawn; }

    public void updateBoardState(List<ChessPiece> pieces) {
        board = new ChessPiece[8][8];
        for (ChessPiece p : pieces) {
            board[p.getXPos()][p.getYPos()] = p;
        }
    }

    public ChessPiece[][] getInternalBoard() {
        return board;
    }

    public boolean isValidMove(ChessPiece piece, int destX, int destY, List<ChessPiece> pieces) {
        return isValidMove(piece, destX, destY, pieces, false);
    }

    public boolean isValidMove(ChessPiece piece, int destX, int destY,
                               List<ChessPiece> pieces, boolean ignoreTurn) {
        updateBoardState(pieces);
        int startX = piece.getXPos(), startY = piece.getYPos();

        if (destX < 0 || destX > 7 || destY < 0 || destY > 7) return false;

        ChessPiece target = board[destX][destY];
        if (target != null && target.getColor().equals(piece.getColor())) return false;

        if (!ignoreTurn) {
            if (whiteTurn && !piece.getColor().equals("white")) return false;
            if (!whiteTurn && !piece.getColor().equals("black")) return false;
        }

        if (!pieceSpecificValidation(piece, destX, destY, pieces, ignoreTurn)) return false;

        if (GameLogicHelper.wouldKingBeInCheckAfterMove(this, piece, destX, destY, pieces)) return false;

        return true;
    }

    private boolean pieceSpecificValidation(ChessPiece piece, int x, int y,
                                            List<ChessPiece> pieces, boolean ignoreTurn) {
        updateBoardState(pieces);
        String type = piece.getType().toLowerCase();
        IMoveValidator validator = validatorMap.get(type);
        return validator != null && validator.isValid(piece, x, y, pieces, board, this);
    }

    public boolean isSquareAttacked(int x, int y, String defender, List<ChessPiece> pieces) {
        for (ChessPiece c : pieces) {
            if (!c.getColor().equals(defender) && GameLogicHelper.canPieceAttackSquare(this, c, x, y, pieces))
                return true;
        }
        return false;
    }

    public void toggleTurn() { whiteTurn = !whiteTurn; }
    public boolean isWhiteTurn() { return whiteTurn; }

    public List<Move> getPossibleMoves(ChessPiece p, List<ChessPiece> pieces) {
        List<Move> moves = new ArrayList<>();
        for (int x = 0; x < 8; x++) for (int y = 0; y < 8; y++)
            if (isValidMove(p, x, y, pieces))
                moves.add(new Move(x, y));
        return moves;
    }

    public boolean hasLegalMoves(String color, List<ChessPiece> pieces) {
        for (ChessPiece p : pieces)
            if (p.getColor().equals(color) && !getPossibleMoves(p, pieces).isEmpty())
                return true;
        return false;
    }

    public boolean isCheckmate(String color, List<ChessPiece> pieces) {
        return isSquareAttacked(GameLogicHelper.findKingX(color, pieces),
            GameLogicHelper.findKingY(color, pieces),
            color, pieces)
            && !hasLegalMoves(color, pieces);
    }

    public boolean isStalemate(String color, List<ChessPiece> pieces) {
        return !isSquareAttacked(GameLogicHelper.findKingX(color, pieces),
            GameLogicHelper.findKingY(color, pieces),
            color, pieces)
            && !hasLegalMoves(color, pieces);
    }

    public void reset() {
        board = new ChessPiece[8][8];
        whiteTurn = true;
        enPassantTargetX = -1;
        enPassantTargetY = -1;
        enPassantVulnerablePawn = null;
    }
}
