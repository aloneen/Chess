package com.mygdx.chess.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ChessPiece {
    private String color;
    private String type;
    private int xPos;  // board column (0–7)
    private int yPos;  // board row (0–7)
    private Texture texture;
    private boolean hasMoved;

    public ChessPiece(String color, String type, int xPos, int yPos) {
        this.color = color;
        this.type = type;
        this.xPos = xPos;
        this.yPos = yPos;
        this.hasMoved = false;
        texture = new Texture(Gdx.files.internal("images/" + color + "_" + type + ".png"));
    }

    public void render(SpriteBatch batch, int squareSize) {
        float pieceSize = squareSize * 0.8f;
        float offset = (squareSize - pieceSize) / 2f;
        float pixelX = xPos * squareSize + offset;
        float pixelY = yPos * squareSize + offset;
        batch.draw(texture, pixelX, pixelY, pieceSize, pieceSize);
    }

    public void dispose() {
        texture.dispose();
    }

    // When the position changes, mark the piece as having moved.
    public void setPosition(int newX, int newY) {
        if (newX != xPos || newY != yPos) {
            xPos = newX;
            yPos = newY;
            hasMoved = true;
        }
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public String getColor() {
        return color;
    }

    public String getType() {
        return type;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean moved) {
        hasMoved = moved;
    }
}
