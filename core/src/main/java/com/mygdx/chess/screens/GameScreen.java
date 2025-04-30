package com.mygdx.chess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.chess.ChessGame;
import com.mygdx.chess.factory.BoardModelFactory;
import com.mygdx.chess.input.ChessInputProcessor;
import com.mygdx.chess.input.IGameInputProcessor;
import com.mygdx.chess.model.IBoardModel;
import com.mygdx.chess.view.IChessRenderer;

import static com.mygdx.chess.util.BoardConfig.BOARD_SIZE;

/**
 * Renders a chess game using a separate model and renderer.
 */
public class GameScreen implements Screen {
    private final ChessGame            game;
    private final SpriteBatch          batch;
    private final OrthographicCamera   camera;
    private final IBoardModel          model;
    private final IChessRenderer       renderer;
    private final IGameInputProcessor  inputProcessor;

    /**
     * @param flipY false = white on bottom; true = black on bottom
     */
    public GameScreen(ChessGame game, boolean flipY) {
        this.game   = game;
        this.batch  = new SpriteBatch();
        this.camera = new OrthographicCamera();
        camera.setToOrtho(false, BOARD_SIZE, BOARD_SIZE);

        //this.model          = new com.mygdx.chess.model.BoardModel(flipY); <- whitout Factory method.

        this.model = BoardModelFactory.createStandardBoard(flipY);
        this.renderer       = new com.mygdx.chess.view.ChessRenderer(model);
        this.inputProcessor = new ChessInputProcessor(game, model, camera, renderer);
        Gdx.input.setInputProcessor(inputProcessor);
    }

    /**
     * Constructor for resuming a game with existing model & renderer (e.g. after promotion).
     */
    public GameScreen(ChessGame game, IBoardModel model, IChessRenderer renderer) {
        this.game   = game;
        this.batch  = new SpriteBatch();
        this.camera = new OrthographicCamera();
        camera.setToOrtho(false, BOARD_SIZE, BOARD_SIZE);

        this.model          = model;
        this.renderer       = renderer;
        this.inputProcessor = new ChessInputProcessor(game, model, camera, renderer);
        Gdx.input.setInputProcessor(inputProcessor);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderer.render(batch);
        batch.end();
    }

    @Override public void resize(int width, int height) { }
    @Override public void show()    { }
    @Override public void hide()    { }
    @Override public void pause()   { }
    @Override public void resume()  { }

    @Override
    public void dispose() {
        batch.dispose();
        renderer.dispose();
    }
}
