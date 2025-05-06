package com.mygdx.chess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.chess.ChessGame;
import com.mygdx.chess.factory.BoardModelFactory;
import com.mygdx.chess.input.ChessInputProcessor;
import com.mygdx.chess.input.IGameInputProcessor;
import com.mygdx.chess.model.IBoardModel;
import com.mygdx.chess.view.ChessRenderer;
import com.mygdx.chess.view.IChessRenderer;
import com.mygdx.chess.view.decorator.CheckDecoratorRenderer;

import static com.mygdx.chess.util.BoardConfig.BOARD_SIZE;

/**
 * Renders a chess game using a separate model and renderer.
 */
public class GameScreen implements Screen {
    private final ChessGame           game;
    private final SpriteBatch         batch;
    private final OrthographicCamera  camera;
    private final IBoardModel         model;
    private final IChessRenderer      renderer;
    private Stage                      uiStage;
    private Skin                       skin;
    private Dialog                     confirmExitDialog;

    public GameScreen(ChessGame game, boolean flipY) {
        this.game   = game;
        this.batch  = new SpriteBatch();
        this.camera = new OrthographicCamera();
        camera.setToOrtho(false, BOARD_SIZE, BOARD_SIZE);

        model    = BoardModelFactory.createStandardBoard(flipY);
        renderer = new CheckDecoratorRenderer(
            new ChessRenderer(model), model
        );

        initUI();
        hookInputs();
    }

    /**
     * Constructor for resuming a game with existing model & renderer (e.g. after promotion).
     */
    public GameScreen(ChessGame game, IBoardModel model, IChessRenderer renderer) {
        this.game     = game;
        this.batch    = new SpriteBatch();
        this.camera   = new OrthographicCamera();
        camera.setToOrtho(false, BOARD_SIZE, BOARD_SIZE);

        this.model    = model;
        this.renderer = renderer;

        initUI();
        hookInputs();
    }

    /**
     * Set up the UI stage, skin, and the ESC confirm-exit dialog.
     */
    private void initUI() {
        uiStage = new Stage(new ScreenViewport());
        skin    = new Skin(Gdx.files.internal("skins/uiskin.json"));

        confirmExitDialog = new Dialog("Confirm Exit", skin) {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    game.setScreen(new MainMenuScreen(game));
                }
            }
        };
        confirmExitDialog.text("Return to main menu?");
        confirmExitDialog.button("Yes", true);
        confirmExitDialog.button("No",  false);

        uiStage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    confirmExitDialog.show(uiStage);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Combine UI stage input and board input via an InputMultiplexer.
     */
    private void hookInputs() {
        ChessInputProcessor boardInput = new ChessInputProcessor(
            game, model, camera, renderer
        );
        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(uiStage);
        mux.addProcessor(boardInput);
        Gdx.input.setInputProcessor(mux);
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

        uiStage.act(delta);
        uiStage.draw();
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
        uiStage.dispose();
        skin.dispose();
    }
}
