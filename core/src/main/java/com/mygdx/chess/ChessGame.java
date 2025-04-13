package com.mygdx.chess;

import com.badlogic.gdx.Game;
import com.mygdx.chess.screens.MainMenuScreen;

public class ChessGame extends Game {
    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }
}
