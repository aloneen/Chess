package com.mygdx.chess.decorator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.chess.actors.ChessPiece;

public class HighlightDecorator implements PieceDecorator {
    private static final ShapeRenderer shapeRenderer = new ShapeRenderer();

    @Override
    public void render(ChessPiece piece, SpriteBatch batch, float squareSize, float pieceSize, float offset, boolean flipped) {
        batch.end();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        int x = piece.getXPos();
        int y = piece.getYPos();
        int drawX = flipped ? 7 - x : x;
        int drawY = flipped ? 7 - y : y;

        float rectX = drawX * squareSize + 1f;
        float rectY = drawY * squareSize + 1f;
        float rectSize = squareSize - 2f;

        // Outer glow
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(rectX - 1, rectY - 1, rectSize + 2, rectSize + 2);

        // Middle bright gold
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(rectX, rectY, rectSize, rectSize);

        // Inner outline to thicken
        shapeRenderer.rect(rectX + 1.5f, rectY + 1.5f, rectSize - 3f, rectSize - 3f);

        shapeRenderer.end();
        batch.begin();
    }
}
