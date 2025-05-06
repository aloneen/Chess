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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.chess.ChessGame;

public class SettingsScreen implements Screen {
    private final ChessGame game;
    private final Stage     stage;
    private final Skin      skin;

    public SettingsScreen(ChessGame game) {
        this.game  = game;
        this.stage = new Stage(new ScreenViewport());
        this.skin  = new Skin(Gdx.files.internal("skins/uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("Settings", skin);
        title.setFontScale(1.5f);
        table.add(title).padBottom(30).row();

        // Control hints
        table.add(new Label("ESC → Return to Main Menu", skin)).left().pad(5).row();
        table.add(new Label("R   → Undo Move", skin)).left().pad(5).row();
        table.add(new Label("M   → Toggle Move Sound", skin)).left().pad(5).row();

        // Button to pick board theme
        TextButton themeBtn = new TextButton("Board Theme", skin);
        themeBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new BoardThemeScreen(game));
            }
        });
        table.add(themeBtn).width(200).padTop(20).row();

        // Back to main menu
        TextButton back = new TextButton("Back", skin);
        back.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        table.add(back).width(200).padTop(10);
    }

    @Override public void show()                 { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float dt) {
        Gdx.gl.glClearColor(0.1f,0.1f,0.1f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(dt);
        stage.draw();
    }
    @Override public void resize(int w, int h)   { stage.getViewport().update(w, h, true); }
    @Override public void pause()                { }
    @Override public void resume()               { }
    @Override public void hide()                 { }
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
