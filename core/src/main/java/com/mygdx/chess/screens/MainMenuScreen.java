// MainMenuScreen.java
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
    private final Skin skin;

    public MainMenuScreen(ChessGame game) {
        this.game  = game;
        this.stage = new Stage(new ScreenViewport());
        this.skin  = new Skin(Gdx.files.internal("skins/uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("My Chess Game", skin, "default");
        title.setFontScale(2f);
        title.setAlignment(Align.center);

        TextButton whiteBtn = new TextButton("Play as White", skin);
        whiteBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // white on bottom
                game.setScreen(new GameScreen(game, false));
            }
        });

        TextButton blackBtn = new TextButton("Play as Black", skin);
        blackBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // black on bottom
                game.setScreen(new GameScreen(game, true));
            }
        });

        table.add(title).expandX().center().padBottom(40f);
        table.row();
        table.add(whiteBtn).width(200).pad(10);
        table.row();
        table.add(blackBtn).width(200).pad(10);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }
    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void pause()  { }
    @Override public void resume() { }
    @Override public void hide()   { }
    @Override public void dispose(){ stage.dispose(); skin.dispose(); }
}
