// ChessPiece.java
package com.mygdx.chess.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Objects;

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

    // Expose the texture so ChessBoard can draw at flipped coords
    public Texture getTexture() {
        return texture;
    }

    public void dispose() {
        texture.dispose();
    }

    @Override
    public ChessPiece clone() {
        ChessPiece copy = new ChessPiece(this.getColor(), this.getType(), this.getXPos(), this.getYPos());
        copy.setHasMoved(this.hasMoved());
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChessPiece)) return false;
        ChessPiece other = (ChessPiece) obj;
        return this.color.equals(other.color)
            && this.type.equalsIgnoreCase(other.type)
            && this.xPos == other.xPos
            && this.yPos == other.yPos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type, xPos, yPos, hasMoved);
    }

    public void setPosition(int newX, int newY) {
        if (newX != xPos || newY != yPos) {
            xPos = newX;
            yPos = newY;
            hasMoved = true;
        }
    }

    public int getXPos() { return xPos; }
    public int getYPos() { return yPos; }
    public String getColor() { return color; }
    public String getType() { return type; }
    public boolean hasMoved() { return hasMoved; }
    public void setHasMoved(boolean moved) { hasMoved = moved; }
}
