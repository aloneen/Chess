package com.mygdx.chess.logic;

import com.mygdx.chess.actors.ChessPiece;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {
    private ChessPiece[][] board;
    private boolean whiteTurn;

    public GameLogic() {
        board = new ChessPiece[8][8];
        whiteTurn = true;
    }

    public void updateBoardState(List<ChessPiece> pieces) {
        board = new ChessPiece[8][8];
        for (ChessPiece piece : pieces) {
            int x = piece.getXPos();
            int y = piece.getYPos();
            board[x][y] = piece;
        }
    }

    // Primary entry point:
    public boolean isValidMove(ChessPiece piece, int destX, int destY, List<ChessPiece> pieces) {
        return isValidMove(piece, destX, destY, pieces, false);
    }

    // Overloaded version with ignoreTurn flag.
    public boolean isValidMove(ChessPiece piece, int destX, int destY, List<ChessPiece> pieces, boolean ignoreTurn) {
        updateBoardState(pieces);

        int startX = piece.getXPos();
        int startY = piece.getYPos();

        // Basic bounds and same-color occupancy check.
        if (destX < 0 || destX >= 8 || destY < 0 || destY >= 8)
            return false;
        ChessPiece destPiece = board[destX][destY];
        if (destPiece != null && destPiece.getColor().equals(piece.getColor()))
            return false;

        // Turn enforcement
        if (!ignoreTurn) {
            if (whiteTurn && !piece.getColor().equals("white"))
                return false;
            if (!whiteTurn && !piece.getColor().equals("black"))
                return false;
        }

        // Piece-specific validations.
        if (!pieceSpecificValidation(piece, destX, destY, pieces, ignoreTurn))
            return false;

        // Simulate the move to check if the King remains in (or is put into) check.
        if (wouldKingBeInCheckAfterMove(piece, destX, destY, pieces))
            return false;

        return true;
    }

    private boolean pieceSpecificValidation(ChessPiece piece, int destX, int destY,
                                            List<ChessPiece> pieces, boolean ignoreTurn) {
        String type = piece.getType().toLowerCase();
        switch (type) {
            case "pawn":
                return isValidPawnMove(piece, destX, destY);
            case "rook":
                return isValidRookMove(piece, destX, destY);
            case "knight":
                return isValidKnightMove(piece, destX, destY);
            case "bishop":
                return isValidBishopMove(piece, destX, destY);
            case "queen":
                return isValidQueenMove(piece, destX, destY);
            case "king":
                return isValidKingMove(piece, destX, destY, pieces, ignoreTurn);
            default:
                return false;
        }
    }

    // Pawn, Rook, Knight, Bishop, and Queen validations remain unchanged.
    private boolean isValidPawnMove(ChessPiece piece, int destX, int destY) {
        int startX = piece.getXPos();
        int startY = piece.getYPos();
        String color = piece.getColor();
        int direction = color.equals("white") ? 1 : -1;

        // One-square forward move.
        if (destX == startX && destY - startY == direction)
            return board[destX][destY] == null;

        // Two-square forward move on first move.
        if (destX == startX && destY - startY == 2 * direction) {
            boolean isFirstMove = (color.equals("white") && startY == 1) || (color.equals("black") && startY == 6);
            if (isFirstMove && board[startX][startY + direction] == null && board[destX][destY] == null)
                return true;
        }

        // Diagonal capture.
        if (Math.abs(destX - startX) == 1 && destY - startY == direction) {
            return board[destX][destY] != null && !board[destX][destY].getColor().equals(color);
        }

        return false;
    }

    private boolean isValidRookMove(ChessPiece piece, int destX, int destY) {
        int startX = piece.getXPos(), startY = piece.getYPos();
        if (startX != destX && startY != destY)
            return false;
        return isPathClear(startX, startY, destX, destY);
    }

    private boolean isValidKnightMove(ChessPiece piece, int destX, int destY) {
        int startX = piece.getXPos(), startY = piece.getYPos();
        int dx = Math.abs(destX - startX), dy = Math.abs(destY - startY);
        return (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
    }

    private boolean isValidBishopMove(ChessPiece piece, int destX, int destY) {
        int startX = piece.getXPos(), startY = piece.getYPos();
        if (Math.abs(destX - startX) != Math.abs(destY - startY))
            return false;
        return isPathClear(startX, startY, destX, destY);
    }

    private boolean isValidQueenMove(ChessPiece piece, int destX, int destY) {
        return isValidRookMove(piece, destX, destY) || isValidBishopMove(piece, destX, destY);
    }

    // ----- King Move Validation (including castling) -----
    private boolean isValidKingMove(ChessPiece piece, int destX, int destY,
                                    List<ChessPiece> pieces, boolean ignoreTurn) {
        int startX = piece.getXPos(), startY = piece.getYPos();
        int dx = Math.abs(destX - startX), dy = Math.abs(destY - startY);

        // Normal one-square move.
        if (dx <= 1 && dy <= 1) {
            return true;
        }

        // Castling move: King moves two squares horizontally, no vertical change.
        if (dx == 2 && dy == 0) {
            // King must not have moved before.
            if (piece.hasMoved())
                return false;

            // Determine castling side.
            if (destX > startX) { // Kingside castling.
                // Look for the kingside rook at (7, startY).
                ChessPiece rook = findPieceAt(7, startY, piece.getColor(), pieces);
                if (rook == null || !rook.getType().equalsIgnoreCase("rook") || rook.hasMoved())
                    return false;
                if (!isPathClear(startX, startY, 7, startY))
                    return false;
                // Ensure that the squares the king passes through and lands on are not attacked.
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

    // ---------------------------------------------------------

    // Simulate the move and check if the King remains in check.
    private boolean wouldKingBeInCheckAfterMove(ChessPiece piece, int destX, int destY, List<ChessPiece> pieces) {
        // Create a shallow copy of the pieces list for simulation.
        List<ChessPiece> simPieces = new ArrayList<>(pieces);

        // Update the board using the simulation list.
        updateBoardState(simPieces);

        int oldX = piece.getXPos();
        int oldY = piece.getYPos();

        // Determine if a piece would be captured at the destination.
        ChessPiece captured = board[destX][destY];
        if (captured != null) {
            // Remove the captured piece from the simulation list.
            simPieces.remove(captured);
        }

        // Simulate the move on the board array.
        board[oldX][oldY] = null;
        board[destX][destY] = piece;

        // Store the current moved status and simulate the move.
        boolean oldMoved = piece.hasMoved();
        piece.setPosition(destX, destY); // This sets hasMoved = true.

        // Check if the king is in check after the simulated move using the simulation list.
        boolean kingInCheckNow = isKingInCheck(piece.getColor(), simPieces);

        // Revert the simulation: restore board state and piece position.
        board[oldX][oldY] = piece;
        board[destX][destY] = captured;
        piece.setPosition(oldX, oldY);
        piece.setHasMoved(oldMoved);

        return kingInCheckNow;
    }


    // Find a piece at coordinates (x,y) that matches the given color.
    private ChessPiece findPieceAt(int x, int y, String color, List<ChessPiece> pieces) {
        for (ChessPiece p : pieces) {
            if (p.getXPos() == x && p.getYPos() == y && p.getColor().equals(color))
                return p;
        }
        return null;
    }

    // Check if square (x,y) is attacked by any enemy.
    public boolean isSquareAttacked(int x, int y, String defendingColor, List<ChessPiece> pieces) {
        for (ChessPiece p : pieces) {
            if (!p.getColor().equals(defendingColor)) {
                if (isValidMove(p, x, y, pieces, true))
                    return true;
            }
        }
        return false;
    }

    // Check whether the King of a given color is in check.
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

    // Check that all squares along the path are clear.
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

    // Return all legal moves for a given piece.
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
     * Returns true if any piece of the given color has at least one legal move.
     */
    public boolean hasLegalMoves(String color, List<ChessPiece> pieces) {
        for (ChessPiece piece : pieces) {
            if (piece.getColor().equals(color)) {
                List<Move> moves = getPossibleMoves(piece, pieces);
                if (!moves.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the player of the given color is checkmated.
     * Checkmate: the King is in check and there are no legal moves.
     */
    public boolean isCheckmate(String color, List<ChessPiece> pieces) {
        return isKingInCheck(color, pieces) && !hasLegalMoves(color, pieces);
    }

    /**
     * Returns true if the game is in stalemate for the given color.
     * Stalemate: the King is not in check but no legal moves exist.
     */
    public boolean isStalemate(String color, List<ChessPiece> pieces) {
        return !isKingInCheck(color, pieces) && !hasLegalMoves(color, pieces);
    }




}
