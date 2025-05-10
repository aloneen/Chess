package com.mygdx.chess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.chess.ChessGame;
import com.mygdx.chess.db.GameResult;
import com.mygdx.chess.db.GameResultDAO;

import java.util.List;

public class GameHistoryScreen implements Screen {
    private final ChessGame game;
    private final Stage stage;
    private final Skin skin;
    private final Texture bg;

    public GameHistoryScreen(ChessGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        this.skin = new Skin(Gdx.files.internal("skins/uiskin.json"));
        this.bg = new Texture(Gdx.files.internal("images/main_bg.jpg")); // same as main menu

        Table outer = new Table();
        outer.setFillParent(true);
        stage.addActor(outer);

        // Title
        Label title = new Label("Game History", skin);
        title.setFontScale(2.2f);
        title.setAlignment(Align.center);

        // Table with data
        Table content = new Table(skin);
        content.top().pad(10).defaults().pad(5);

        content.add(header("Winner")).left();
        content.add(header("Bot Side")).left();
        content.add(header("Mode")).left();
        content.add(header("Time")).left().row();

        List<GameResult> results = GameResultDAO.fetchAll();
        for (GameResult r : results) {
            content.add(label(r.getWinner()));
            content.add(label(r.getBotSide()));
            content.add(label(r.getGameMode()));
            content.add(label(r.getTimestamp().toString())).row();
        }

        // Scroll inside fixed-size box (same width as Main Menu area)
        ScrollPane scrollPane = new ScrollPane(content, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // only vertical

        // Container for everything (title + scroll + button)
        Table container = new Table(skin);
        container.setBackground("default-rect");
        container.pad(20);
        container.add(title).colspan(4).padBottom(20f).center().row();
        container.add(scrollPane).width(550).height(300).colspan(4).center().row(); // ← Main menu-like size

        TextButton backBtn = new TextButton("← Back to Menu", skin);
        backBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        container.add(backBtn).colspan(4).padTop(20f).center();

        outer.add(container).center();
    }

    private Label label(String text) {
        Label l = new Label(text, skin);
        l.setColor(Color.LIGHT_GRAY);
        return l;
    }

    private Label header(String text) {
        Label l = new Label(text, skin);
        l.setFontScale(1.1f);
        l.setColor(Color.GOLD);
        return l;
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getBatch().begin();
        stage.getBatch().draw(bg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.getBatch().end();

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
