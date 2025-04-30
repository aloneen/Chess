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

    public boolean isValidMove(ChessPiece piece, int destX, int destY, List<ChessPiece> pieces) {
        return isValidMove(piece, destX, destY, pieces, false);
    }

    public boolean isValidMove(ChessPiece piece, int destX, int destY,
                               List<ChessPiece> pieces, boolean ignoreTurn) {
        updateBoardState(pieces);
        int startX = piece.getXPos(), startY = piece.getYPos();

        // Bounds check
        if (destX < 0 || destX > 7 || destY < 0 || destY > 7) return false;

        // Cannot capture own piece
        ChessPiece target = board[destX][destY];
        if (target != null && target.getColor().equals(piece.getColor())) return false;

        // Turn enforcement
        if (!ignoreTurn) {
            if (whiteTurn && !piece.getColor().equals("white")) return false;
            if (!whiteTurn && !piece.getColor().equals("black")) return false;
        }

        // Piece-specific movement
        if (!pieceSpecificValidation(piece, destX, destY, pieces, ignoreTurn)) return false;

        // King safety
        if (wouldKingBeInCheckAfterMove(piece, destX, destY, pieces)) return false;

        return true;
    }

    private boolean pieceSpecificValidation(ChessPiece piece, int x, int y,
                                            List<ChessPiece> pieces, boolean ignoreTurn) {
        updateBoardState(pieces); // ensure board[][] is up-to-date
        String type = piece.getType().toLowerCase();

        IMoveValidator validator = validatorMap.get(type);
        if (validator == null) return false;

        return validator.isValid(piece, x, y, pieces, board, this);
    }



    /**
     * Simulate the move, drop any captured piece, and check king safety.
     */
    private boolean wouldKingBeInCheckAfterMove(ChessPiece moving,
                                                int x, int y,
                                                List<ChessPiece> pieces) {
        List<ChessPiece> sim = deepCopyPieces(pieces);
        ChessPiece mover = null;
        for (ChessPiece c : sim) {
            if (c.equals(moving)) { mover = c; break; }
        }
        if (mover==null) return false;
        // Remove captured if any
        for (Iterator<ChessPiece> it = sim.iterator(); it.hasNext();) {
            ChessPiece c = it.next();
            if (!c.equals(mover) && c.getXPos()==x && c.getYPos()==y) {
                it.remove();
                break;
            }
        }
        // Move the piece
        mover.setPosition(x, y);
        // Find own king
        ChessPiece king = null;
        for (ChessPiece c : sim) {
            if (c.getColor().equals(mover.getColor())
                && c.getType().equalsIgnoreCase("king")) {
                king = c; break;
            }
        }
        if (king==null) return false;
        // Is king under attack?
        return isSquareAttacked(king.getXPos(), king.getYPos(), king.getColor(), sim);
    }

    private List<ChessPiece> deepCopyPieces(List<ChessPiece> pieces) {
        List<ChessPiece> copy = new ArrayList<>();
        for (ChessPiece c : pieces) copy.add(c.clone());
        return copy;
    }

    private ChessPiece findPieceAt(int x, int y, String color, List<ChessPiece> pieces) {
        for (ChessPiece c : pieces)
            if (c.getXPos()==x && c.getYPos()==y && c.getColor().equals(color))
                return c;
        return null;
    }

    public boolean isSquareAttacked(int x, int y, String defender, List<ChessPiece> pieces) {
        for (ChessPiece c : pieces) {
            if (!c.getColor().equals(defender) && canPieceAttackSquare(c, x, y, pieces))
                return true;
        }
        return false;
    }

    private boolean canPieceAttackSquare(ChessPiece p, int x, int y, List<ChessPiece> pieces) {
        updateBoardState(pieces);
        int sx = p.getXPos(), sy = p.getYPos();
        String type = p.getType().toLowerCase();
        switch (type) {
            case "pawn":
                int dir = p.getColor().equals("white") ? 1 : -1;
                return y-sy==dir && Math.abs(x-sx)==1;
            case "knight":
                int dx = Math.abs(x-sx), dy = Math.abs(y-sy);
                return dx*dy==2;
            case "bishop":
                if (Math.abs(x-sx)==Math.abs(y-sy))
                    return isPathClear(sx, sy, x, y);
                return false;
            case "rook":
                if (sx==x||sy==y)
                    return isPathClear(sx, sy, x, y);
                return false;
            case "queen":
                if ((sx==x||sy==y)||(Math.abs(x-sx)==Math.abs(y-sy)))
                    return isPathClear(sx, sy, x, y);
                return false;
            case "king":
                return Math.abs(x-sx)<=1 && Math.abs(y-sy)<=1;
            default:
                return false;
        }
    }

    private boolean isPathClear(int sx, int sy, int dx, int dy) {
        int stepx = Integer.compare(dx, sx);
        int stepy = Integer.compare(dy, sy);
        int cx = sx+stepx, cy = sy+stepy;
        while (cx!=dx || cy!=dy) {
            if (board[cx][cy]!=null) return false;
            cx+=stepx; cy+=stepy;
        }
        return true;
    }

    public void toggleTurn() { whiteTurn = !whiteTurn; }
    public boolean isWhiteTurn() { return whiteTurn; }

    public List<Move> getPossibleMoves(ChessPiece p, List<ChessPiece> pieces) {
        List<Move> moves = new ArrayList<>();
        for (int x=0; x<8; x++) for (int y=0; y<8; y++)
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
        return isSquareAttacked(findKingX(color, pieces),
            findKingY(color, pieces),
            color, pieces)
            && !hasLegalMoves(color, pieces);
    }

    public boolean isStalemate(String color, List<ChessPiece> pieces) {
        return !isSquareAttacked(findKingX(color, pieces),
            findKingY(color, pieces),
            color, pieces)
            && !hasLegalMoves(color, pieces);
    }

    private int findKingX(String color, List<ChessPiece> pieces) {
        for (ChessPiece p : pieces)
            if (p.getType().equalsIgnoreCase("king") && p.getColor().equals(color))
                return p.getXPos();
        return -1;
    }
    private int findKingY(String color, List<ChessPiece> pieces) {
        for (ChessPiece p : pieces)
            if (p.getType().equalsIgnoreCase("king") && p.getColor().equals(color))
                return p.getYPos();
        return -1;
    }


    public void reset() {
        board = new ChessPiece[8][8];
        whiteTurn = true;
        enPassantTargetX = -1;
        enPassantTargetY = -1;
        enPassantVulnerablePawn = null;
    }

}
