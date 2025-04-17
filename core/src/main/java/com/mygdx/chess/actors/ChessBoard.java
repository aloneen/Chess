// ChessBoard.java
package com.mygdx.chess.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.chess.logic.GameLogic;
import com.mygdx.chess.logic.Move;

import java.util.ArrayList;
import java.util.List;

public class ChessBoard {
    public static final int BOARD_SIZE   = 800;
    public static final int SQUARE_SIZE  = BOARD_SIZE / 8; // 100

    private Texture boardTexture;
    private Texture moveIndicatorTexture;
    private List<ChessPiece> pieces;
    private GameLogic gameLogic;
    private List<Move> possibleMoves;
    private boolean flipY;

    /** Default: white on bottom. */
    public ChessBoard() {
        this(false);
    }

    /**
     * @param flipY
     *   if true, pieces are drawn at (x, 7-y) and inputs are remapped accordingly,
     *   so black will start on bottom.
     */
    public ChessBoard(boolean flipY) {
        this.flipY = flipY;
        boardTexture = new Texture(Gdx.files.internal("images/chess_board.png"));
        moveIndicatorTexture = new Texture(Gdx.files.internal("images/move_indicator.png"));
        pieces       = new ArrayList<>();
        gameLogic    = new GameLogic();
        possibleMoves = new ArrayList<>();
        initializePieces();
    }

    public boolean isFlipY() {
        return flipY;
    }

    private void initializePieces() {
        // White Pawns
        for (int col = 0; col < 8; col++)
            pieces.add(new ChessPiece("white", "pawn", col, 1));
        // Black Pawns
        for (int col = 0; col < 8; col++)
            pieces.add(new ChessPiece("black", "pawn", col, 6));
        // White major pieces
        pieces.add(new ChessPiece("white", "rook",   0, 0));
        pieces.add(new ChessPiece("white", "rook",   7, 0));
        pieces.add(new ChessPiece("white", "knight", 1, 0));
        pieces.add(new ChessPiece("white", "knight", 6, 0));
        pieces.add(new ChessPiece("white", "bishop", 2, 0));
        pieces.add(new ChessPiece("white", "bishop", 5, 0));
        pieces.add(new ChessPiece("white", "queen",  3, 0));
        pieces.add(new ChessPiece("white", "king",   4, 0));
        // Black major pieces
        pieces.add(new ChessPiece("black", "rook",   0, 7));
        pieces.add(new ChessPiece("black", "rook",   7, 7));
        pieces.add(new ChessPiece("black", "knight", 1, 7));
        pieces.add(new ChessPiece("black", "knight", 6, 7));
        pieces.add(new ChessPiece("black", "bishop", 2, 7));
        pieces.add(new ChessPiece("black", "bishop", 5, 7));
        pieces.add(new ChessPiece("black", "queen",  3, 7));
        pieces.add(new ChessPiece("black", "king",   4, 7));
    }

    public void update(float delta) { /* no-op */ }

    public void render(SpriteBatch batch) {
        // Draw board background (symmetric image)
        batch.draw(boardTexture, 0, 0, BOARD_SIZE, BOARD_SIZE);

        // Draw pieces, flipping Y if needed
        float pieceSize = SQUARE_SIZE * 0.8f;
        float offset    = (SQUARE_SIZE - pieceSize) / 2f;
        for (ChessPiece piece : pieces) {
            Texture tex = piece.getTexture();
            int x = piece.getXPos();
            int y = piece.getYPos();
            int drawY = flipY ? (7 - y) : y;
            float px = x * SQUARE_SIZE + offset;
            float py = drawY * SQUARE_SIZE + offset;
            batch.draw(tex, px, py, pieceSize, pieceSize);
        }

        // Draw move indicators
        int indicatorSize = 20;
        if (possibleMoves != null) {
            for (Move m : possibleMoves) {
                int drawY = flipY ? (7 - m.y) : m.y;
                float ix = m.x * SQUARE_SIZE + (SQUARE_SIZE - indicatorSize) / 2f;
                float iy = drawY * SQUARE_SIZE + (SQUARE_SIZE - indicatorSize) / 2f;
                batch.draw(moveIndicatorTexture, ix, iy, indicatorSize, indicatorSize);
            }
        }
    }

    public void dispose() {
        boardTexture.dispose();
        moveIndicatorTexture.dispose();
        for (ChessPiece p : pieces) p.dispose();
    }

    public List<ChessPiece> getPieces() {
        return pieces;
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }

    public void setPossibleMoves(List<Move> moves) {
        this.possibleMoves = moves;
    }
}
