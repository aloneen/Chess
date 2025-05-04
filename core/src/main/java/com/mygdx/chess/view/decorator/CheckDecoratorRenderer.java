package com.mygdx.chess.view.decorator;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.chess.actors.ChessPiece;
import com.mygdx.chess.logic.GameLogic;
import com.mygdx.chess.model.IBoardModel;
import com.mygdx.chess.view.IChessRenderer;

import static com.mygdx.chess.sound.SoundManager.playMoveCheck;
import static com.mygdx.chess.util.BoardConfig.SQUARE_SIZE;


public class CheckDecoratorRenderer extends ChessRendererDecorator {
    private final IBoardModel model;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private boolean checkPlayed = false;
    private static final float BORDER_THICKNESS = 4f;

    public CheckDecoratorRenderer(IChessRenderer inner, IBoardModel model) {
        super(inner);
        this.model = model;
    }

    @Override
    public void render(SpriteBatch batch) {

        super.render(batch);


        GameLogic logic = model.getGameLogic();
        String toMove = logic.isWhiteTurn() ? "white" : "black";
        ChessPiece king = null;
        for (ChessPiece p : model.getPieces()) {
            if ("king".equalsIgnoreCase(p.getType()) && toMove.equalsIgnoreCase(p.getColor())) {
                king = p;
                break;
            }
        }
        if (king == null) return;

        boolean inCheck = logic.isSquareAttacked(
            king.getXPos(), king.getYPos(), king.getColor(), model.getPieces());


        if (inCheck && !checkPlayed) {
            playMoveCheck();
            checkPlayed = true;
        } else if (!inCheck) {
            checkPlayed = false;
        }

        if (!inCheck) return;


        boolean flip = model.isFlipped();
        int dx = flip ? 7 - king.getXPos() : king.getXPos();
        int dy = flip ? 7 - king.getYPos() : king.getYPos();
        float x = dx * SQUARE_SIZE;
        float y = dy * SQUARE_SIZE;


        batch.end();
        shapes.setProjectionMatrix(batch.getProjectionMatrix());
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(1f, 0f, 0f, 0.6f);


        shapes.rect(x, y + SQUARE_SIZE - BORDER_THICKNESS, SQUARE_SIZE, BORDER_THICKNESS);

        shapes.rect(x, y, SQUARE_SIZE, BORDER_THICKNESS);

        shapes.rect(x, y, BORDER_THICKNESS, SQUARE_SIZE);

        shapes.rect(x + SQUARE_SIZE - BORDER_THICKNESS, y, BORDER_THICKNESS, SQUARE_SIZE);

        shapes.end();
        batch.begin();
    }

    @Override
    public void dispose() {
        super.dispose();
        shapes.dispose();
    }
}
