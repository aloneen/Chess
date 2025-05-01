package com.mygdx.chess.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.chess.actors.ChessPiece;
import com.mygdx.chess.logic.Move;
import com.mygdx.chess.model.IBoardModel;
import com.mygdx.chess.proxy.TextureProxy;

import java.util.List;

import static com.mygdx.chess.util.BoardConfig.BOARD_SIZE;
import static com.mygdx.chess.util.BoardConfig.SQUARE_SIZE;

public class ChessRenderer implements IChessRenderer {
    private final IBoardModel model;
    private final Texture     boardTex;
    private final Texture     dotTex;

    public ChessRenderer(IBoardModel model) {
        this.model    = model;
//        this.boardTex = new Texture(Gdx.files.internal("images/chess_board.png"));
//        this.dotTex   = new Texture(Gdx.files.internal("images/move_indicator.png"));
        this.boardTex = TextureProxy.get("images/chess_board.png");
        this.dotTex   = TextureProxy.get("images/move_indicator.png");
    }

    @Override
    public void render(SpriteBatch batch) {
        // 1) Draw board background
        batch.draw(boardTex, 0, 0, BOARD_SIZE, BOARD_SIZE);

        batch.setColor(1f,1f,1f,0.7f);

        float pieceSize   = SQUARE_SIZE * 0.8f;
        float offset      = (SQUARE_SIZE - pieceSize) / 2f;

        for (ChessPiece p : model.getPieces()) {
            // pass squareSize, pieceSize, offset, flip
            p.render(batch, SQUARE_SIZE, pieceSize, offset, model.isFlipped());
        }


        List<Move> moves = model.getPossibleMoves();
        if (moves != null) {
            int d = 20;
            for (Move m : moves) {
                int mx = model.isFlipped() ? 7 - m.x : m.x;
                int my = model.isFlipped() ? 7 - m.y : m.y;
                batch.draw(
                    dotTex,
                    mx * SQUARE_SIZE + (SQUARE_SIZE - d)/2f,
                    my * SQUARE_SIZE + (SQUARE_SIZE - d)/2f,
                    d, d
                );
            }
        }

    }

    @Override
    public void dispose() {
        boardTex.dispose();
        dotTex.dispose();
    }
}
