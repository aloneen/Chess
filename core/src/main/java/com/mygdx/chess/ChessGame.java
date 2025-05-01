
package com.mygdx.chess;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.chess.proxy.TextureProxy;
import com.mygdx.chess.screens.MainMenuScreen;

/**
 * The main entry point for the LibGDX chess game.
 */
public class ChessGame extends Game {
    private SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        // Launch the main menu screen
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render(); // delegate to current screen's render method
    }

    @Override
    public void dispose() {
        TextureProxy.disposeAll(); // PROXY: after running dispose all textures.
    
        if (batch != null) batch.dispose();
        // Screens and their assets will be disposed by LibGDX when setScreen is changed or on exit
        super.dispose();
    }
}
