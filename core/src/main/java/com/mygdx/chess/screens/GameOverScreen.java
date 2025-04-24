package com.mygdx.chess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.chess.ChessGame;

public class GameOverScreen implements Screen {
    private final ChessGame game;
    private final String message;
    private final Stage stage;
    private final Skin skin;

    public GameOverScreen(ChessGame game, String message) {
        this.game = game;
        this.message = message;
        this.stage = new Stage(new ScreenViewport());
        // Use your same ui skin as above or another skin if you want a different style
        this.skin = new Skin(Gdx.files.internal("skins/uiskin.json"));

        // Create a table to position UI elements
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Create the label for the checkmate/stalemate message
        Label messageLabel = new Label(message, skin, "default");
        messageLabel.setFontScale(1.5f);
        messageLabel.setAlignment(Align.center);

        // Create a "NEW GAME" button
        TextButton newGameButton = new TextButton("NEW GAME", skin);

        // On click, return to the main menu or start a fresh game
        newGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Option 1: Return to the Main Menu
                // game.setScreen(new MainMenuScreen(game));

                // Option 2: Start a fresh new GameScreen
                game.setScreen(new GameScreen(game));
            }
        });

        // Layout in the table
        table.add(messageLabel).center().padBottom(50f);
        table.row();
        table.add(newGameButton).center();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}

