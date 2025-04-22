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

    public GameScreen(ChessGame game) {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800);

        chessBoard = new ChessBoard();

        // Now pass game, chessBoard and camera to the input processor.
        Gdx.input.setInputProcessor(new ChessInputProcessor(game, chessBoard, camera));
    }

    @Override
    public void render(float delta) {
        // Update logic
        chessBoard.update(delta);

        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.setColor(1f, 1f, 1f, 0.7f);

        batch.begin();
        chessBoard.render(batch);
        batch.end();
    }

    @Override public void resize(int width, int height) { /* handle resizing */ }
    @Override public void show() { }
    @Override public void hide() { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void dispose() {
        batch.dispose();
        chessBoard.dispose();
    }
}
