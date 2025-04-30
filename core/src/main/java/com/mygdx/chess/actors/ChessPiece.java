// File: ChessPiece.java
package com.mygdx.chess.actors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.Objects;

/**
 * Represents a single chess piece on the board.
 */
public class ChessPiece implements Cloneable {
    private final String color;
    private final String type;
    private int xPos;
    private int yPos;
    private final Texture texture;
    private boolean hasMoved;

    public ChessPiece(String color, String type, int xPos, int yPos) {
        this.color = color;
        this.type = type;
        this.xPos = xPos;
        this.yPos = yPos;
        this.hasMoved = false;
        this.texture = new Texture(
            com.badlogic.gdx.Gdx.files.internal("images/" + color + "_" + type + ".png")
        );
    }

    /**
     * Draws this piece centered in its board square.
     *
     * @param batch      the SpriteBatch
     * @param squareSize the full size of one chessboard square
     * @param pieceSize  the actual size you want the sprite to occupy
     * @param offset     (squareSize - pieceSize)/2
     * @param flip       whether to flip the board vertically
     */
    public void render(SpriteBatch batch,
                       float squareSize,
                       float pieceSize,
                       float offset,
                       boolean flip) {
        int dx = flip ? 7 - xPos : xPos;
        int dy = flip ? 7 - yPos : yPos;

        // Position by squareSize, not pieceSize:
        float posX = dx * squareSize + offset;
        float posY = dy * squareSize + offset;

        batch.draw(texture, posX, posY, pieceSize, pieceSize);
    }

    public void dispose() {
        texture.dispose();
    }

    @Override
    public ChessPiece clone() {
        try {
            ChessPiece copy = (ChessPiece) super.clone();
            // texture is shared/disposed by actor
            copy.hasMoved = this.hasMoved;
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessPiece)) return false;
        ChessPiece that = (ChessPiece) o;
        return xPos == that.xPos && yPos == that.yPos
            && Objects.equals(color, that.color)
            && Objects.equals(type, that.type);
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

    public String getColor() { return color; }
    public String getType()  { return type;  }
    public int getXPos()     { return xPos;  }
    public int getYPos()     { return yPos;  }
    public boolean hasMoved(){ return hasMoved; }
    public void setHasMoved(boolean moved) { hasMoved = moved; }
}
