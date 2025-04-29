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
import com.mygdx.chess.actors.ChessBoard;
import com.mygdx.chess.actors.ChessPiece;

public class PromotionScreen implements Screen {

    private final ChessGame game;
    private final ChessBoard board;
    private final ChessPiece pawn;
    private final Stage stage;
    private final Skin skin;


    public PromotionScreen(ChessGame game, ChessBoard board, ChessPiece pawn) {
        this.game = game;
        this.board = board;
        this.pawn = pawn;
        stage = new Stage();
        skin = new Skin(Gdx.files.internal("skins/uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Create buttons for promotion options.
        TextButton queenButton = new TextButton("Queen", skin);
        TextButton rookButton = new TextButton("Rook", skin);
        TextButton bishopButton = new TextButton("Bishop", skin);
        TextButton knightButton = new TextButton("Knight", skin);

        queenButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                promote("queen");
            }
        });
        rookButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                promote("rook");
            }
        });
        bishopButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                promote("bishop");
            }
        });
        knightButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                promote("knight");
            }
        });

        table.add(queenButton).pad(10);
        table.row();
        table.add(rookButton).pad(10);
        table.row();
        table.add(bishopButton).pad(10);
        table.row();
        table.add(knightButton).pad(10);
    }

    /**
     * Called when the player selects a promotion option.
     * This method replaces the pawn with a new piece of the selected type and toggles turn.
     */
    private void promote(String newType) {
        // Get pawn information.
        int x = pawn.getXPos();
        int y = pawn.getYPos();
        String color = pawn.getColor();

        // Create a new ChessPiece representing the promoted piece.
        ChessPiece promotedPiece = new ChessPiece(color, newType, x, y);

        // Replace the pawn in the board's pieces list.
        for (int i = 0; i < board.getPieces().size(); i++) {
            ChessPiece p = board.getPieces().get(i);
            if (p.equals(pawn)) {
                board.getPieces().set(i, promotedPiece);
                break;
            }
        }

        // Toggle turn now that the move is complete.
        board.getGameLogic().toggleTurn();

        // Return to the game screen with the updated board.
        game.setScreen(new GameScreen(game, board));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }
    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }




}
