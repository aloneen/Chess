// GameScreen.java
package com.mygdx.chess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.chess.actors.ChessBoard;
import com.mygdx.chess.input.ChessInputProcessor;
import com.mygdx.chess.ChessGame;

public class GameScreen implements Screen {
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final ChessBoard chessBoard;

    /** Standard constructor: white on bottom. */
    public GameScreen(ChessGame game) {
        this(game, false);
    }

    /**
     * @param flipY
     *   false = white on bottom (default)
     *   true  = black on bottom
     */
    public GameScreen(ChessGame game, boolean flipY) {
        batch      = new SpriteBatch();
        camera     = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800);
        chessBoard = new ChessBoard(flipY);
        Gdx.input.setInputProcessor(new ChessInputProcessor(game, chessBoard, camera));
    }

    /** Used after a pawn promotion to preserve board state + flipY flag. */
    public GameScreen(ChessGame game, ChessBoard board) {
        batch      = new SpriteBatch();
        camera     = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800);
        chessBoard = board;
        Gdx.input.setInputProcessor(new ChessInputProcessor(game, chessBoard, camera));
    }

    @Override
    public void render(float delta) {
        chessBoard.update(delta);
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(1f,1f,1f,0.7f);
        chessBoard.render(batch);
        batch.end();
    }

    @Override public void resize(int width, int height) { }
    @Override public void show()    { }
    @Override public void hide()    { }
    @Override public void pause()   { }
    @Override public void resume()  { }
    @Override public void dispose() {
        batch.dispose();
        chessBoard.dispose();
    }
}
