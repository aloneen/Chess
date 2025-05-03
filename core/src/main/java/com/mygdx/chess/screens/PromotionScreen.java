// src/main/java/com/mygdx/chess/screens/PromotionScreen.java
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
import com.mygdx.chess.actors.ChessPiece;

public class PromotionScreen implements Screen {
    private final BotGameScreen parent;
    private final ChessPiece    pawn;
    private final int           fx, fy, tx, ty;
    private final Stage         stage;
    private final Skin          skin;

    /** fx,fy = origin; tx,ty = target promotion square */
    public PromotionScreen(
        BotGameScreen parent,
        ChessPiece pawn,
        int fx, int fy,
        int tx, int ty
    ) {
        this.parent = parent;
        this.pawn   = pawn;
        this.fx     = fx;
        this.fy     = fy;
        this.tx     = tx;
        this.ty     = ty;

        stage = new Stage();
        skin  = new Skin(Gdx.files.internal("skins/uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        TextButton queen  = new TextButton("Queen",  skin);
        TextButton rook   = new TextButton("Rook",   skin);
        TextButton bishop = new TextButton("Bishop", skin);
        TextButton knight = new TextButton("Knight", skin);

        queen.addListener(listener("queen"));
        rook.addListener(listener("rook"));
        bishop.addListener(listener("bishop"));
        knight.addListener(listener("knight"));

        table.add(queen).pad(10).row();
        table.add(rook).pad(10).row();
        table.add(bishop).pad(10).row();
        table.add(knight).pad(10);
    }

    private ClickListener listener(String pieceType) {
        return new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                parent.applyPromotion(pawn, fx, fy, tx, ty, pieceType);
                parent.getGame().setScreen(parent);
            }
        };
    }

    @Override public void show()    { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float dt) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(dt);
        stage.draw();
    }
    @Override public void resize(int w, int h) { stage.getViewport().update(w,h,true); }
    @Override public void pause()   {}
    @Override public void resume()  {}
    @Override public void hide()    {}
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
