package com.mygdx.chess.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Renderer interface for drawing the chess board and pieces.
 */
public interface IChessRenderer {
    /** Draws board, pieces, and move indicators. */
    void render(SpriteBatch batch);

    /** Releases renderer resources. */
    void dispose();
}
