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

public class MainMenuScreen implements Screen {
    private final ChessGame game;
    private final Stage stage;
    private final Skin skin; // We'll use a built-in skin or your custom one

    public MainMenuScreen(ChessGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());

        // You can use a built-in "default" skin if you place uiskin.json/uiskin.atlas in assets/
        // Or use any custom skin you like. For example:
        this.skin = new Skin(Gdx.files.internal("skins/uiskin.json"));

        // Create a table to layout widgets
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Create a label for the title
        Label titleLabel = new Label("My Chess Game", skin, "default");
        titleLabel.setFontScale(2f);
        titleLabel.setAlignment(Align.center);

        // Create a "Start Game" button
        TextButton startButton = new TextButton("Start Game", skin);

        // Add a click listener to startButton
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Move to the GameScreen
                game.setScreen(new GameScreen(game));
            }
        });

        // Add the widgets to the table
        table.add(titleLabel).expandX().center().padBottom(50f);
        table.row();
        table.add(startButton).center();

    }

    @Override
    public void show() {
        // Set input processor to this screen's stage
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update and draw the stage (Scene2D)
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
