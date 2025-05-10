package com.mygdx.chess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.chess.ChessGame;

public class SettingsScreen implements Screen {
    private final ChessGame game;
    private final Stage stage;
    private final Skin skin;
    private final Texture bg;

    public SettingsScreen(ChessGame game) {
        this.game  = game;
        this.stage = new Stage(new ScreenViewport());
        this.skin  = new Skin(Gdx.files.internal("skins/uiskin.json"));
        this.bg    = new Texture(Gdx.files.internal("images/main_bg.jpg")); // same background

        Table outer = new Table();
        outer.setFillParent(true);
        stage.addActor(outer);

        // Inner container with styled background
        Table container = new Table(skin);
        container.setBackground("default-rect");
        container.pad(20);
        container.defaults().pad(10);

        Label title = new Label("Settings", skin);
        title.setFontScale(2f);
        container.add(title).padBottom(20f).colspan(1).center().row();

        container.add(new Label("ESC → Return to Main Menu", skin)).left().row();
        container.add(new Label("R   → Undo Move", skin)).left().row();
        container.add(new Label("M   → Toggle Move Sound", skin)).left().row();

        // Button to pick board theme
        TextButton themeBtn = new TextButton("Board Theme", skin);
        themeBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new BoardThemeScreen(game));
            }
        });
        container.add(themeBtn).width(200).padTop(15).row();

        // Back to main menu
        TextButton back = new TextButton("← Back", skin);
        back.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        container.add(back).width(200).padTop(10).center();

        outer.add(container).center();
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override public void render(float dt) {
        Gdx.gl.glClearColor(0,0,0,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getBatch().begin();
        stage.getBatch().draw(bg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.getBatch().end();

        stage.act(dt);
        stage.draw();
    }

    @Override public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
        bg.dispose();
    }
}
