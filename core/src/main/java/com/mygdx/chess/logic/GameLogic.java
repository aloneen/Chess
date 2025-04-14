package com.mygdx.chess.logic;

import com.mygdx.chess.actors.ChessPiece;
import java.util.ArrayList;
import java.util.List;

public class GameLogic {
    // Used for move validation: a board representation.
    private ChessPiece[][] board;
    private boolean whiteTurn;

    public GameLogic() {
        board = new ChessPiece[8][8];
        whiteTurn = true;
    }

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

        // (Optional Debug)
        // System.out.println("=== Board State ===");
        // for (ChessPiece piece : pieces) {
        //     System.out.println(piece.getColor() + " " + piece.getType() +
        //                        " at " + piece.getXPos() + ", " + piece.getYPos());
        // }
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

    /**
     * Checks movement rules for each piece type.
     */
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
        if (Math.abs(destX - startX) == 1 && (destY - startY) == direction)
            return board[destX][destY] != null && !board[destX][destY].getColor().equals(color);

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
            return true; // Later, the king safety simulation will prevent moving next to enemy king.

        // Castling: King must move exactly two squares horizontally.
        if (dx == 2 && dy == 0) {
            if (piece.hasMoved())
                return false;
            if (destX > startX) { // Kingside castling.
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
            } else { // Queenside castling.
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

    /**
     * Simulation method: returns true if moving the piece to (destX, destY)
     * would leave its own king in check.
     *
     * This method creates a deep copy of the pieces, simulates the move, and then uses
     * our attack detection (isSquareAttacked) to decide whether the king is attacked.
     */
    private boolean wouldKingBeInCheckAfterMove(ChessPiece movingPiece, int destX, int destY, List<ChessPiece> pieces) {
        List<ChessPiece> simPieces = deepCopyPieces(pieces);

        // Locate the copy of the moving piece in simPieces.
        ChessPiece simMoving = null;
        for (ChessPiece cp : simPieces) {
            if (cp.equals(movingPiece)) {  // equals() compares color, type, and position.
                simMoving = cp;
                break;
            }
        }
        if (simMoving == null)
            return false; // Should not happen.

        // Remove any piece (other than simMoving) occupying (destX, destY).
        for (int i = 0; i < simPieces.size(); i++) {
            ChessPiece cp = simPieces.get(i);
            if (!cp.equals(simMoving) && cp.getXPos() == destX && cp.getYPos() == destY) {
                simPieces.remove(i);
                break;
            }
        }

        // Simulate moving the piece.
        simMoving.setPosition(destX, destY);

        // Find the king of the moving piece’s color in simPieces.
        ChessPiece king = null;
        for (ChessPiece cp : simPieces) {
            if (cp.getColor().equals(simMoving.getColor()) && cp.getType().equalsIgnoreCase("king")) {
                king = cp;
                break;
            }
        }
        if (king == null)
            return false; // Should not happen.

        // Instead of looping through enemy moves, simply use our updated attack detection.
        return isSquareAttacked(king.getXPos(), king.getYPos(), king.getColor(), simPieces);
    }

    // ---------- Helper: Create a deep copy of the list of pieces ----------
    private List<ChessPiece> deepCopyPieces(List<ChessPiece> pieces) {
        List<ChessPiece> copy = new ArrayList<>();
        for (ChessPiece piece : pieces) {
            copy.add(piece.clone());  // Assumes ChessPiece.clone() is implemented.
        }
        return copy;
    }

    // ---------- Helper: Find a piece at (x, y) of the given color ----------
    private ChessPiece findPieceAt(int x, int y, String color, List<ChessPiece> pieces) {
        for (ChessPiece p : pieces) {
            if (p.getXPos() == x && p.getYPos() == y && p.getColor().equals(color)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Helper method for attack detection.
     * Determines whether the given piece can attack the square (destX, destY)
     * based solely on its inherent movement rules—ignoring whether moving it would expose its own king.
     */
    private boolean canPieceAttackSquare(ChessPiece piece, int destX, int destY, List<ChessPiece> pieces) {
        updateBoardState(pieces);
        int startX = piece.getXPos();
        int startY = piece.getYPos();
        String type = piece.getType().toLowerCase();

        switch (type) {
            case "pawn":
                // For attacks, pawns strike diagonally.
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
                if ((startX == destX || startY == destY) ||
                    (Math.abs(destX - startX) == Math.abs(destY - startY)))
                    return isPathClear(startX, startY, destX, destY);
                return false;
            case "king":
                return Math.abs(destX - startX) <= 1 && Math.abs(destY - startY) <= 1;
            default:
                return false;
        }
    }

    /**
     * Determines whether a given square is attacked by any enemy piece.
     * Uses canPieceAttackSquare to decide whether an enemy piece has the inherent ability
     * to reach the target square.
     */
    public boolean isSquareAttacked(int x, int y, String defendingColor, List<ChessPiece> pieces) {
        for (ChessPiece p : pieces) {
            if (!p.getColor().equals(defendingColor)) {
                if (canPieceAttackSquare(p, x, y, pieces))
                    return true;
            }
        }
        return false;
    }

    // ---------- King safety checks ----------

    /**
     * Returns true if the king of the given color is currently under attack.
     */
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

    // ---------- Path Clearance (for sliding pieces) ----------
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

    // ---------- Turn Management ----------
    public void toggleTurn() {
        whiteTurn = !whiteTurn;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    // ---------- Generating Possible Moves ----------
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

    /**
     * Checks if any legal move exists for any piece of the given color.
     */
    public boolean hasLegalMoves(String color, List<ChessPiece> pieces) {
        for (ChessPiece piece : pieces) {
            if (piece.getColor().equals(color)) {
                List<Move> moves = getPossibleMoves(piece, pieces);
                if (!moves.isEmpty())
                    return true;
            }
        }
        return false;
    }

    /**
     * Checkmate: the king is in check and no legal move exists.
     */
    public boolean isCheckmate(String color, List<ChessPiece> pieces) {
        return isKingInCheck(color, pieces) && !hasLegalMoves(color, pieces);
    }

    /**
     * Stalemate: the king is not in check but no legal move exists.
     */
    public boolean isStalemate(String color, List<ChessPiece> pieces) {
        return !isKingInCheck(color, pieces) && !hasLegalMoves(color, pieces);
    }
}
