package com.mygdx.chess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
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
import jdk.tools.jmod.Main;

public class BoardThemeScreen implements Screen {
    private final ChessGame game;
    private final Stage     stage;
    private final Skin      skin;

    public BoardThemeScreen(ChessGame game) {
        this.game  = game;
        this.stage = new Stage(new ScreenViewport());
        this.skin  = new Skin(Gdx.files.internal("skins/uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("Select Board Theme", skin);
        title.setFontScale(1.5f);
        table.add(title).padBottom(30).row();

        // Helper to build each theme button
        for (String theme : new String[]{"classic", "stone", "marble"}) {
            TextButton btn = new TextButton(
                theme.substring(0,1).toUpperCase() + theme.substring(1) + " Board",
                skin
            );
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    Preferences prefs = Gdx.app.getPreferences("chess_settings");
                    prefs.putString("boardTheme", theme);
                    prefs.flush();
                    // go back to settings (or main menu)
                    game.setScreen(new MainMenuScreen(game));
                }
            });
            table.add(btn).width(200).pad(10).row();
        }

        // Back button
        TextButton back = new TextButton("Back", skin);
        back.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new SettingsScreen(game));
            }
        });
        table.add(back).width(200).padTop(20);
    }

    @Override public void show()               { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float dt) {
        Gdx.gl.glClearColor(0.1f,0.1f,0.1f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(dt);
        stage.draw();
    }
    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void pause()              { }
    @Override public void resume()             { }
    @Override public void hide()               { }
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
