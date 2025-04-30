package com.mygdx.chess.logic;

import com.mygdx.chess.actors.ChessPiece;
import java.util.ArrayList;
import java.util.List;

public class GameLogic {
    // Used for move validation: a board representation.
    private ChessPiece[][] board;
    private boolean whiteTurn;

    // --- En passant fields ---
    private int enPassantTargetX = -1;
    private int enPassantTargetY = -1;
    private ChessPiece enPassantVulnerablePawn = null;

    public GameLogic() {
        board = new ChessPiece[8][8];
        whiteTurn = true;
    }

    // --- En passant getters/setters ---
    public void setEnPassantTarget(int x, int y, ChessPiece pawn) {
        this.enPassantTargetX = x;
        this.enPassantTargetY = y;
        this.enPassantVulnerablePawn = pawn;
    }

    public void clearEnPassantTarget() {
        this.enPassantTargetX = -1;
        this.enPassantTargetY = -1;
        this.enPassantVulnerablePawn = null;
    }

    public int getEnPassantTargetX() { return enPassantTargetX; }
    public int getEnPassantTargetY() { return enPassantTargetY; }
    public ChessPiece getEnPassantVulnerablePawn() { return enPassantVulnerablePawn; }
    // ------------------------------------

    /**
     * Rebuilds the board array from the supplied list of pieces.
     */
    public void updateBoardState(List<ChessPiece> pieces) {
        board = new ChessPiece[8][8];
        for (ChessPiece piece : pieces) {
            int x = piece.getXPos();
            int y = piece.getYPos();
            board[x][y] = piece;
        }
    }

    /**
     * Returns true if the move for 'piece' to (destX, destY) is legal.
     */
    public boolean isValidMove(ChessPiece piece, int destX, int destY, List<ChessPiece> pieces) {
        return isValidMove(piece, destX, destY, pieces, false);
    }

    /**
     * Overloaded version with the ignoreTurn flag (for simulation purposes).
     */
    public boolean isValidMove(ChessPiece piece, int destX, int destY, List<ChessPiece> pieces, boolean ignoreTurn) {
        updateBoardState(pieces);
        int startX = piece.getXPos();
        int startY = piece.getYPos();

        // Out-of-bounds check.
        if (destX < 0 || destX >= 8 || destY < 0 || destY >= 8)
            return false;

        // Cannot capture a piece of the same color.
        ChessPiece destPiece = board[destX][destY];
        if (destPiece != null && destPiece.getColor().equals(piece.getColor()))
            return false;

        // Enforce turn rules (unless ignoring).
        if (!ignoreTurn) {
            if (whiteTurn && !piece.getColor().equals("white"))
                return false;
            if (!whiteTurn && !piece.getColor().equals("black"))
                return false;
        }

        // Validate movement pattern for the piece type.
        if (!pieceSpecificValidation(piece, destX, destY, pieces, ignoreTurn))
            return false;

        // Simulate the move: if the move would leave the moving side’s king attacked, disallow it.
        if (wouldKingBeInCheckAfterMove(piece, destX, destY, pieces))
            return false;

        return true;
    }

    private boolean pieceSpecificValidation(ChessPiece piece, int destX, int destY, List<ChessPiece> pieces, boolean ignoreTurn) {
        String type = piece.getType().toLowerCase();
        switch (type) {
            case "pawn":   return isValidPawnMove(piece, destX, destY);
            case "rook":   return isValidRookMove(piece, destX, destY);
            case "knight": return isValidKnightMove(piece, destX, destY);
            case "bishop": return isValidBishopMove(piece, destX, destY);
            case "queen":  return isValidQueenMove(piece, destX, destY);
            case "king":   return isValidKingMove(piece, destX, destY, pieces, ignoreTurn);
            default:       return false;
        }
    }

    // ---------- Pawn Validation ----------
    private boolean isValidPawnMove(ChessPiece piece, int destX, int destY) {
        int startX = piece.getXPos(), startY = piece.getYPos();
        String color = piece.getColor();
        int direction = color.equals("white") ? 1 : -1;

        // Single-step forward (destination must be empty).
        if (destX == startX && destY - startY == direction)
            return board[destX][destY] == null;

        // Two-step forward move on first move.
        if (destX == startX && destY - startY == 2 * direction) {
            boolean isFirstMove = (color.equals("white") && startY == 1) || (color.equals("black") && startY == 6);
            if (isFirstMove && board[startX][startY + direction] == null && board[destX][destY] == null)
                return true;
        }

        // Diagonal capture.
        if (Math.abs(destX - startX) == 1 && (destY - startY) == direction) {
            // Normal capture.
            if (board[destX][destY] != null && !board[destX][destY].getColor().equals(color))
                return true;
            // Check for en passant capture.
            if (board[destX][destY] == null && destX == enPassantTargetX && destY == enPassantTargetY) {
                if (color.equalsIgnoreCase("white") && startY == enPassantTargetY - 1)
                    return true;
                if (color.equalsIgnoreCase("black") && startY == enPassantTargetY + 1)
                    return true;
            }
            return false;
        }

        return false;
    }

    // ---------- Rook Validation ----------
    private boolean isValidRookMove(ChessPiece piece, int destX, int destY) {
        int startX = piece.getXPos(), startY = piece.getYPos();
        if (startX != destX && startY != destY)
            return false;
        return isPathClear(startX, startY, destX, destY);
    }

    // ---------- Knight Validation ----------
    private boolean isValidKnightMove(ChessPiece piece, int destX, int destY) {
        int startX = piece.getXPos(), startY = piece.getYPos();
        int dx = Math.abs(destX - startX), dy = Math.abs(destY - startY);
        return (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
    }

    // ---------- Bishop Validation ----------
    private boolean isValidBishopMove(ChessPiece piece, int destX, int destY) {
        int startX = piece.getXPos(), startY = piece.getYPos();
        if (Math.abs(destX - startX) != Math.abs(destY - startY))
            return false;
        return isPathClear(startX, startY, destX, destY);
    }

    // ---------- Queen Validation ----------
    private boolean isValidQueenMove(ChessPiece piece, int destX, int destY) {
        return isValidRookMove(piece, destX, destY) || isValidBishopMove(piece, destX, destY);
    }

    // ---------- King Validation (including castling) ----------
    private boolean isValidKingMove(ChessPiece piece, int destX, int destY, List<ChessPiece> pieces, boolean ignoreTurn) {
        int startX = piece.getXPos(), startY = piece.getYPos();
        int dx = Math.abs(destX - startX), dy = Math.abs(destY - startY);

        // Normal one-square moves.
        if (dx <= 1 && dy <= 1)
            return true;

        // Castling: King must move exactly two squares horizontally.
        if (dx == 2 && dy == 0) {
            if (piece.hasMoved())
                return false;
            if (destX > startX) {
                ChessPiece rook = findPieceAt(7, startY, piece.getColor(), pieces);
                if (rook == null || !rook.getType().equalsIgnoreCase("rook") || rook.hasMoved())
                    return false;
                if (!isPathClear(startX, startY, 7, startY))
                    return false;
                if (isSquareAttacked(startX, startY, piece.getColor(), pieces) ||
                    isSquareAttacked(startX + 1, startY, piece.getColor(), pieces) ||
                    isSquareAttacked(startX + 2, startY, piece.getColor(), pieces))
                    return false;
                return true;
            } else {
                ChessPiece rook = findPieceAt(0, startY, piece.getColor(), pieces);
                if (rook == null || !rook.getType().equalsIgnoreCase("rook") || rook.hasMoved())
                    return false;
                if (!isPathClear(startX, startY, 0, startY))
                    return false;
                if (isSquareAttacked(startX, startY, piece.getColor(), pieces) ||
                    isSquareAttacked(startX - 1, startY, piece.getColor(), pieces) ||
                    isSquareAttacked(startX - 2, startY, piece.getColor(), pieces))
                    return false;
                return true;
            }
        }
        return false;
    }

    private boolean wouldKingBeInCheckAfterMove(ChessPiece movingPiece, int destX, int destY, List<ChessPiece> pieces) {
        List<ChessPiece> simPieces = deepCopyPieces(pieces);

        ChessPiece simMoving = null;
        for (ChessPiece cp : simPieces) {
            if (cp.equals(movingPiece)) {
                simMoving = cp;
                break;
            }
        }
        if (simMoving == null)
            return false;

        for (int i = 0; i < simPieces.size(); i++) {
            ChessPiece cp = simPieces.get(i);
            if (!cp.equals(simMoving) && cp.getXPos() == destX && cp.getYPos() == destY) {
                simPieces.remove(i);
                break;
            }
        }
        simMoving.setPosition(destX, destY);

        ChessPiece king = null;
        for (ChessPiece cp : simPieces) {
            if (cp.getColor().equals(simMoving.getColor()) && cp.getType().equalsIgnoreCase("king")) {
                king = cp;
                break;
            }
        }
        if (king == null)
            return false;

        return isSquareAttacked(king.getXPos(), king.getYPos(), king.getColor(), simPieces);
    }

    private List<ChessPiece> deepCopyPieces(List<ChessPiece> pieces) {
        List<ChessPiece> copy = new ArrayList<>();
        for (ChessPiece piece : pieces) {
            copy.add(piece.clone());
        }
        return copy;
    }

    private ChessPiece findPieceAt(int x, int y, String color, List<ChessPiece> pieces) {
        for (ChessPiece p : pieces) {
            if (p.getXPos() == x && p.getYPos() == y && p.getColor().equals(color)) {
                return p;
            }
        }
        return null;
    }

    public boolean isSquareAttacked(int x, int y, String defendingColor, List<ChessPiece> pieces) {
        for (ChessPiece p : pieces) {
            if (!p.getColor().equals(defendingColor)) {
                if (canPieceAttackSquare(p, x, y, pieces))
                    return true;
            }
        }
        return false;
    }

    private boolean canPieceAttackSquare(ChessPiece piece, int destX, int destY, List<ChessPiece> pieces) {
        updateBoardState(pieces);
        int startX = piece.getXPos();
        int startY = piece.getYPos();
        String type = piece.getType().toLowerCase();

        switch (type) {
            case "pawn":
                int direction = piece.getColor().equals("white") ? 1 : -1;
                return (destY - startY == direction && Math.abs(destX - startX) == 1);
            case "knight":
                int dx = Math.abs(destX - startX);
                int dy = Math.abs(destY - startY);
                return (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
            case "bishop":
                if (Math.abs(destX - startX) == Math.abs(destY - startY))
                    return isPathClear(startX, startY, destX, destY);
                return false;
            case "rook":
                if (startX == destX || startY == destY)
                    return isPathClear(startX, startY, destX, destY);
                return false;
            case "queen":
                if ((startX == destX || startY == destY) || (Math.abs(destX - startX) == Math.abs(destY - startY)))
                    return isPathClear(startX, startY, destX, destY);
                return false;
            case "king":
                return Math.abs(destX - startX) <= 1 && Math.abs(destY - startY) <= 1;
            default:
                return false;
        }
    }

    public boolean isKingInCheck(String color, List<ChessPiece> pieces) {
        ChessPiece king = null;
        for (ChessPiece p : pieces) {
            if (p.getColor().equals(color) && p.getType().equalsIgnoreCase("king")) {
                king = p;
                break;
            }
        }
        if (king == null)
            return false;
        return isSquareAttacked(king.getXPos(), king.getYPos(), color, pieces);
    }

    private boolean isPathClear(int startX, int startY, int destX, int destY) {
        int dx = Integer.compare(destX, startX);
        int dy = Integer.compare(destY, startY);
        int curX = startX + dx, curY = startY + dy;
        while (curX != destX || curY != destY) {
            if (board[curX][curY] != null)
                return false;
            curX += dx;
            curY += dy;
        }
        return true;
    }

    public void toggleTurn() {
        whiteTurn = !whiteTurn;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public List<Move> getPossibleMoves(ChessPiece piece, List<ChessPiece> pieces) {
        List<Move> moves = new ArrayList<>();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (isValidMove(piece, x, y, pieces))
                    moves.add(new Move(x, y));
            }
        }
        return moves;
    }

    public boolean hasLegalMoves(String color, List<ChessPiece> pieces) {
        for (ChessPiece piece : pieces) {
            if (piece.getColor().equals(color)) {
                List<Move> m = getPossibleMoves(piece, pieces);
                if (!m.isEmpty()) return true;
            }
        }
        return false;
    }

    public boolean isCheckmate(String color, List<ChessPiece> pieces) {
        return isKingInCheck(color, pieces) && !hasLegalMoves(color, pieces);
    }

    public boolean isStalemate(String color, List<ChessPiece> pieces) {
        return !isKingInCheck(color, pieces) && !hasLegalMoves(color, pieces);
    }
}
