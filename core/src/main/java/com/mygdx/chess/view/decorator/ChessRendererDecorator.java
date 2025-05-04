
package com.mygdx.chess.view.decorator;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.chess.view.IChessRenderer;


public abstract class ChessRendererDecorator implements IChessRenderer {
    protected final IChessRenderer inner;

    public ChessRendererDecorator(IChessRenderer inner) {
        this.inner = inner;
    }

    @Override
    public void render(SpriteBatch batch) {
        inner.render(batch);
    }

    @Override
    public void dispose() {
        inner.dispose();
    }
}
