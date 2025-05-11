package com.mygdx.chess.logic;

import com.mygdx.chess.actors.ChessPiece;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to assist GameLogic with helper methods.
 */
public class GameLogicHelper {

    public static List<ChessPiece> deepCopyPieces(List<ChessPiece> pieces) {
        List<ChessPiece> copy = new ArrayList<>();
        for (ChessPiece c : pieces) copy.add(c.clone());
        return copy;
    }

    public static ChessPiece findPieceAt(int x, int y, String color, List<ChessPiece> pieces) {
        for (ChessPiece c : pieces)
            if (c.getXPos() == x && c.getYPos() == y && c.getColor().equals(color))
                return c;
        return null;
    }

    public static int findKingX(String color, List<ChessPiece> pieces) {
        for (ChessPiece p : pieces)
            if (p.getType().equalsIgnoreCase("king") && p.getColor().equals(color))
                return p.getXPos();
        return -1;
    }

    public static int findKingY(String color, List<ChessPiece> pieces) {
        for (ChessPiece p : pieces)
            if (p.getType().equalsIgnoreCase("king") && p.getColor().equals(color))
                return p.getYPos();
        return -1;
    }

    public static boolean isPathClear(ChessPiece[][] board, int sx, int sy, int dx, int dy) {
        int stepx = Integer.compare(dx, sx);
        int stepy = Integer.compare(dy, sy);
        int cx = sx + stepx, cy = sy + stepy;
        while (cx != dx || cy != dy) {
            if (board[cx][cy] != null) return false;
            cx += stepx;
            cy += stepy;
        }
        return true;
    }

    public static boolean canPieceAttackSquare(GameLogic logic, ChessPiece p, int x, int y, List<ChessPiece> pieces) {
        logic.updateBoardState(pieces);
        ChessPiece[][] board = logic.getInternalBoard();
        int sx = p.getXPos(), sy = p.getYPos();
        String type = p.getType().toLowerCase();
        switch (type) {
            case "pawn":
                int dir = p.getColor().equals("white") ? 1 : -1;
                return y - sy == dir && Math.abs(x - sx) == 1;
            case "knight":
                int dx = Math.abs(x - sx), dy = Math.abs(y - sy);
                return dx * dy == 2;
            case "bishop":
                if (Math.abs(x - sx) == Math.abs(y - sy))
                    return isPathClear(board, sx, sy, x, y);
                return false;
            case "rook":
                if (sx == x || sy == y)
                    return isPathClear(board, sx, sy, x, y);
                return false;
            case "queen":
                if ((sx == x || sy == y) || (Math.abs(x - sx) == Math.abs(y - sy)))
                    return isPathClear(board, sx, sy, x, y);
                return false;
            case "king":
                return Math.abs(x - sx) <= 1 && Math.abs(y - sy) <= 1;
            default:
                return false;
        }
    }

    public static boolean wouldKingBeInCheckAfterMove(GameLogic logic, ChessPiece moving, int x, int y, List<ChessPiece> pieces) {
        List<ChessPiece> sim = deepCopyPieces(pieces);
        ChessPiece mover = null;
        for (ChessPiece c : sim) {
            if (c.equals(moving)) {
                mover = c;
                break;
            }
        }
        if (mover == null) return false;

        for (Iterator<ChessPiece> it = sim.iterator(); it.hasNext(); ) {
            ChessPiece c = it.next();
            if (!c.equals(mover) && c.getXPos() == x && c.getYPos() == y) {
                it.remove();
                break;
            }
        }
        mover.setPosition(x, y);

        ChessPiece king = null;
        for (ChessPiece c : sim) {
            if (c.getColor().equals(mover.getColor()) && c.getType().equalsIgnoreCase("king")) {
                king = c;
                break;
            }
        }
        if (king == null) return false;

        return logic.isSquareAttacked(king.getXPos(), king.getYPos(), king.getColor(), sim);
    }
}
