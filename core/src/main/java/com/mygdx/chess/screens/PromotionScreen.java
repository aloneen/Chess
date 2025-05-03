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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.mygdx.chess.ChessGame;
import com.mygdx.chess.actors.ChessPiece;
import com.mygdx.chess.logic.GameLogic;
import com.mygdx.chess.model.IBoardModel;
import com.mygdx.chess.screens.BotGameScreen;
import com.mygdx.chess.screens.GameScreen;
import com.mygdx.chess.view.IChessRenderer;

/**
 * A unified promotion screen that works for both
 * Bot-vs-Human and Human-vs-Human games.
 */
public class PromotionScreen implements Screen {
    private final BotGameScreen  botParent;   // non-null in Bot mode
    private final ChessGame      game;        // non-null in human mode
    private final IBoardModel    model;       // non-null in human mode
    private final IChessRenderer renderer;    // non-null in human mode
    private final ChessPiece     pawn;
    private final int            fx, fy, tx, ty;
    private final Stage          stage;
    private final Skin           skin;

    /**
     * Constructor for Bot-vs-Human promotion.
     */
    public PromotionScreen(
        BotGameScreen parent,
        ChessPiece pawn,
        int fx, int fy,
        int tx, int ty
    ) {
        this.botParent = parent;
        this.game      = null;
        this.model     = null;
        this.renderer  = null;
        this.pawn      = pawn;
        this.fx        = fx;
        this.fy        = fy;
        this.tx        = tx;
        this.ty        = ty;
        this.stage     = new Stage();
        this.skin      = new Skin(Gdx.files.internal("skins/uiskin.json"));
        initUI();
    }

    /**
     * Constructor for Human-vs-Human promotion.
     */
    public PromotionScreen(
        ChessGame game,
        IBoardModel model,
        IChessRenderer renderer,
        ChessPiece pawn,
        int fx, int fy,
        int tx, int ty
    ) {
        this.botParent = null;
        this.game      = game;
        this.model     = model;
        this.renderer  = renderer;
        this.pawn      = pawn;
        this.fx        = fx;
        this.fy        = fy;
        this.tx        = tx;
        this.ty        = ty;
        this.stage     = new Stage();
        this.skin      = new Skin(Gdx.files.internal("skins/uiskin.json"));
        initUI();
    }

    /**
     * Common UI initialization.
     */
    private void initUI() {
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

    /**
     * Creates a listener that handles promotion choice.
     */
    private ClickListener listener(String pieceType) {
        return new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (botParent != null) {
                    // Bot-vs-Human: delegate to BotGameScreen
                    botParent.applyPromotion(pawn, fx, fy, tx, ty, pieceType);
                    botParent.getGame().setScreen(botParent);
                } else {
                    // Human-vs-Human: apply directly
                    // 1) replace pawn
                    model.getPieces().remove(pawn);
                    model.getPieces().add(
                        new ChessPiece(pawn.getColor(), pieceType, tx, ty)
                    );
                    // 2) clear en passant
                    GameLogic logic = model.getGameLogic();
                    logic.clearEnPassantTarget();
                    // 3) toggle turn
                    logic.toggleTurn();
                    // 4) return to regular GameScreen
                    game.setScreen(new GameScreen(
                        game, model, renderer
                    ));
                }
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
    @Override public void pause()       {}
    @Override public void resume()      {}
    @Override public void hide()        {}
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
