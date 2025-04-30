package com.mygdx.chess.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.chess.ChessGame;
import com.mygdx.chess.actors.ChessPiece;
import com.mygdx.chess.input.ChessInputProcessor;
import com.mygdx.chess.logic.GameLogic;
import com.mygdx.chess.model.IBoardModel;
import com.mygdx.chess.view.IChessRenderer;

public class PromotionScreen implements Screen {
    private final ChessGame    game;
    private final IBoardModel   model;
    private final IChessRenderer renderer;
    private final ChessPiece    pawn;
    private final Stage         stage;
    private final Skin          skin;

    public PromotionScreen(
        ChessGame game,
        IBoardModel model,
        IChessRenderer renderer,
        ChessPiece pawn
    ) {
        this.game     = game;
        this.model    = model;
        this.renderer = renderer;
        this.pawn     = pawn;
        this.stage    = new Stage();
        this.skin     = new Skin(Gdx.files.internal("skins/uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        TextButton queen  = new TextButton("Queen",  skin);
        TextButton rook   = new TextButton("Rook",   skin);
        TextButton bishop = new TextButton("Bishop", skin);
        TextButton knight = new TextButton("Knight", skin);

        queen.addListener(  new ClickListener(){ public void clicked(InputEvent e,float x,float y){promote("queen");}});
        rook.addListener(   new ClickListener(){ public void clicked(InputEvent e,float x,float y){promote("rook");}});
        bishop.addListener( new ClickListener(){ public void clicked(InputEvent e,float x,float y){promote("bishop");}});
        knight.addListener( new ClickListener(){ public void clicked(InputEvent e,float x,float y){promote("knight");}});

        table.add(queen).pad(10).row();
        table.add(rook).pad(10).row();
        table.add(bishop).pad(10).row();
        table.add(knight).pad(10);
    }

    private void promote(String newType) {
        // 1) Replace pawn in the model
        int x = pawn.getXPos(), y = pawn.getYPos();
        model.getPieces().remove(pawn);
        model.getPieces().add(new ChessPiece(pawn.getColor(), newType, x, y));

        // 2) Toggle turn
        GameLogic logic = model.getGameLogic();
        logic.toggleTurn();

        // 3) Return to the right screen
        if (game.getScreen() instanceof BotGameScreen) {
            BotGameScreen prev = (BotGameScreen)game.getScreen();
            // you’ll need a getter for difficulty:
            game.setScreen(new BotGameScreen(
                game,
                prev.getDifficulty(),    // add this getter
                prev.isHumanWhite()
            ));
        } else {
            game.setScreen(new GameScreen(
                game,
                model,
                renderer
            ));
        }
    }

    @Override public void show()    { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float dt) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(dt); stage.draw();
    }
    @Override public void resize(int w,int h){ stage.getViewport().update(w,h,true); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}
    @Override public void dispose(){
        stage.dispose();
        skin.dispose();
    }
}
