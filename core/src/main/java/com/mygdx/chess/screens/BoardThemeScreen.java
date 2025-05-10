package com.mygdx.chess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.chess.ChessGame;

public class BoardThemeScreen implements Screen {
    private final ChessGame game;
    private final Stage stage;
    private final Skin skin;
    private final Texture bg;

    public BoardThemeScreen(ChessGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        this.skin = new Skin(Gdx.files.internal("skins/uiskin.json"));
        this.bg = new Texture(Gdx.files.internal("images/main_bg.jpg"));

        Table outer = new Table();
        outer.setFillParent(true);
        stage.addActor(outer);

        Table container = new Table(skin);
        container.setBackground("default-rect");
        container.pad(20);
        container.defaults().pad(10);

        Label title = new Label("Select Board Theme", skin);
        title.setFontScale(2f);
        container.add(title).padBottom(20f).center().row();

        // Theme options
        for (String theme : new String[]{"classic", "stone", "marble"}) {
            TextButton btn = new TextButton(
                theme.substring(0, 1).toUpperCase() + theme.substring(1) + " Board",
                skin
            );
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    Preferences prefs = Gdx.app.getPreferences("chess_settings");
                    prefs.putString("boardTheme", theme);
                    prefs.flush();
                    game.setScreen(new SettingsScreen(game));
                }
            });
            container.add(btn).width(200).row();
        }

        // Back button
        TextButton back = new TextButton("← Back", skin);
        back.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new SettingsScreen(game));
            }
        });
        container.add(back).width(200).padTop(10);

        outer.add(container).center();
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override public void render(float dt) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
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
