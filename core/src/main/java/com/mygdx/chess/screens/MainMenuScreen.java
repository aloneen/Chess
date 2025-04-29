
package com.mygdx.chess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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

        // 1) Background image
        Image bg = new Image(new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("images/main_bg.jpg")));
        bg.setFillParent(true);
        stage.addActor(bg);

        // 2) UI table on top
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("My Chess Game", skin);
        title.setFontScale(2f);
        title.setAlignment(Align.center);

        TextButton whiteBtn = new TextButton("Play as White", skin);
        whiteBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new GameScreen(game, false));
            }
        });

        TextButton blackBtn = new TextButton("Play as Black", skin);
        blackBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new GameScreen(game, true));
            }
        });

        table.add(title).padBottom(40f).row();
        table.add(whiteBtn).width(200).pad(10).row();
        table.add(blackBtn).width(200).pad(10);
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w,int h)    { stage.getViewport().update(w,h,true); }
    @Override public void pause()                { }
    @Override public void resume()               { }
    @Override public void hide()                 { }
    @Override public void dispose()              { stage.dispose(); skin.dispose(); }
}
