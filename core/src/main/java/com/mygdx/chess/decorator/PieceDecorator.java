package com.mygdx.chess.decorator;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.chess.actors.ChessPiece;

public interface PieceDecorator {
    void render(ChessPiece piece, SpriteBatch batch, float squareSize, float pieceSize, float offset, boolean flipped);
}
