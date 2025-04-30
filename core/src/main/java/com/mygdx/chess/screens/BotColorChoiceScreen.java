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
import static com.mygdx.chess.screens.BotLevelScreen.Difficulty;

public class BotColorChoiceScreen implements Screen {
    private final ChessGame game;
    private final Stage     stage;
    private final Skin      skin;
    private final Difficulty difficulty;

    public BotColorChoiceScreen(ChessGame game, Difficulty difficulty) {
        this.game       = game;
        this.difficulty = difficulty;
        this.stage      = new Stage(new ScreenViewport());
        this.skin       = new Skin(Gdx.files.internal("skins/uiskin.json"));

        Image bg = new Image(new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("images/main_bg.jpg")));
        bg.setFillParent(true);
        stage.addActor(bg);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("Choose Your Side", skin);
        title.setFontScale(1.8f);

        TextButton whiteBtn = new TextButton("You Play White", skin);
        whiteBtn.addListener(new ClickListener(){
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new BotGameScreen(game, difficulty, true));
            }
        });

        TextButton blackBtn = new TextButton("You Play Black", skin);
        blackBtn.addListener(new ClickListener(){
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new BotGameScreen(game, difficulty, false));
            }
        });

        table.add(title).padBottom(30f).row();
        table.add(whiteBtn).width(200).pad(10).row();
        table.add(blackBtn).width(200).pad(10);
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
