package com.mygdx.chess.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.chess.logic.GameLogic;
import com.mygdx.chess.logic.Move;

import java.util.ArrayList;
import java.util.List;

public class ChessBoard {
    public static final int BOARD_SIZE  = 800;
    public static final int SQUARE_SIZE = BOARD_SIZE / 8;

    private final boolean flip;              // flip both axes when true
    private final Texture boardTex;
    private final Texture moveDot;
    private final List<ChessPiece> pieces;
    private final GameLogic logic;
    private       List<Move> possibleMoves;

    public ChessBoard() {
        this(false);
    }

    public ChessBoard(boolean playAsBlack) {
        this.flip         = playAsBlack;
        this.boardTex     = new Texture(Gdx.files.internal("images/chess_board.png"));
        this.moveDot      = new Texture(Gdx.files.internal("images/move_indicator.png"));
        this.pieces       = new ArrayList<>();
        this.logic        = new GameLogic();
        this.possibleMoves= new ArrayList<>();
        initializePieces();
    }

    private void initializePieces() {
        // white pawns
        for (int x=0; x<8; x++) pieces.add(new ChessPiece("white","pawn",   x,1));
        // black pawns
        for (int x=0; x<8; x++) pieces.add(new ChessPiece("black","pawn",   x,6));
        // white back rank
        pieces.add(new ChessPiece("white","rook",   0,0));
        pieces.add(new ChessPiece("white","knight", 1,0));
        pieces.add(new ChessPiece("white","bishop", 2,0));
        pieces.add(new ChessPiece("white","queen",  3,0));
        pieces.add(new ChessPiece("white","king",   4,0));
        pieces.add(new ChessPiece("white","bishop", 5,0));
        pieces.add(new ChessPiece("white","knight", 6,0));
        pieces.add(new ChessPiece("white","rook",   7,0));
        // black back rank
        pieces.add(new ChessPiece("black","rook",   0,7));
        pieces.add(new ChessPiece("black","knight", 1,7));
        pieces.add(new ChessPiece("black","bishop", 2,7));
        pieces.add(new ChessPiece("black","queen",  3,7));
        pieces.add(new ChessPiece("black","king",   4,7));
        pieces.add(new ChessPiece("black","bishop", 5,7));
        pieces.add(new ChessPiece("black","knight", 6,7));
        pieces.add(new ChessPiece("black","rook",   7,7));
    }

    public void update(float dt) { /* no-op */ }

    public void render(SpriteBatch batch) {
        batch.draw(boardTex, 0, 0, BOARD_SIZE, BOARD_SIZE);

        float size   = SQUARE_SIZE * 0.8f;
        float offset = (SQUARE_SIZE - size) / 2f;

        for (ChessPiece p : pieces) {
            int x = p.getXPos(), y = p.getYPos();
            // flip both axes if requested:
            int dx = flip ? 7 - x : x;
            int dy = flip ? 7 - y : y;
            batch.draw(p.getTexture(),
                dx * SQUARE_SIZE + offset,
                dy * SQUARE_SIZE + offset,
                size, size);
        }

        if (possibleMoves != null) {
            int d = 20;
            for (Move m : possibleMoves) {
                int mx = flip ? 7 - m.x : m.x;
                int my = flip ? 7 - m.y : m.y;
                batch.draw(moveDot,
                    mx * SQUARE_SIZE + (SQUARE_SIZE - d)/2f,
                    my * SQUARE_SIZE + (SQUARE_SIZE - d)/2f,
                    d, d);
            }
        }
    }

    public void dispose() {
        boardTex.dispose();
        moveDot.dispose();
        for (ChessPiece p : pieces) p.dispose();
    }

    public List<ChessPiece> getPieces()      { return pieces;        }
    public GameLogic      getGameLogic()     { return logic;         }
    public void           setPossibleMoves(List<Move> m) { possibleMoves = m; }
    public boolean        isFlipY()          { return flip;          }
}
