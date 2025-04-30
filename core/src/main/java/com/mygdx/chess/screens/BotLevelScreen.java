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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.chess.ChessGame;

public class BotLevelScreen implements Screen {
    private final ChessGame game;
    private final Stage     stage;
    private final Skin      skin;

    public enum Difficulty { LOW, MEDIUM, STRONG }

    public BotLevelScreen(ChessGame game) {
        this.game  = game;
        this.stage = new Stage(new ScreenViewport());
        this.skin  = new Skin(Gdx.files.internal("skins/uiskin.json"));

        Image bg = new Image(new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("images/main_bg.jpg")));
        bg.setFillParent(true);
        stage.addActor(bg);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("Select Bot Level", skin);
        title.setFontScale(1.8f);

        TextButton lowBtn = new TextButton("Low Level Bot", skin);
        lowBtn.addListener(new ClickListener(){
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new BotColorChoiceScreen(game, Difficulty.LOW));
            }
        });

        TextButton medBtn = new TextButton("Medium Level Bot", skin);
        medBtn.addListener(new ClickListener(){
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new BotColorChoiceScreen(game, Difficulty.MEDIUM));
            }
        });

        TextButton strBtn = new TextButton("Strong Level Bot", skin);
        strBtn.addListener(new ClickListener(){
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new BotColorChoiceScreen(game, Difficulty.STRONG));
            }
        });

        table.add(title).padBottom(30f).row();
        table.add(lowBtn).width(220).pad(8).row();
        table.add(medBtn).width(220).pad(8).row();
        table.add(strBtn).width(220).pad(8);
    }

    @Override public void show()                 { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float dt)       {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(dt);
        stage.draw();
    }
    @Override public void resize(int w,int h)    { stage.getViewport().update(w,h,true); }
    @Override public void pause()                { }
    @Override public void resume()               { }
    @Override public void hide()                 { }
    @Override public void dispose()              { stage.dispose(); skin.dispose(); }
}
