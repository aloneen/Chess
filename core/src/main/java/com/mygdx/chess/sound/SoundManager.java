package com.mygdx.chess.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;


// Facade class that simplifies complex subsystem interactions
public class SoundManager {
    private static final Sound moveSound = Gdx.audio.newSound(Gdx.files.internal("sounds/move-self.mp3"));
    private static final Sound captureSound = Gdx.audio.newSound(Gdx.files.internal("sounds/capture.mp3"));
    private static final Sound promoteSound = Gdx.audio.newSound(Gdx.files.internal("sounds/promote.mp3"));
    private static final Sound checkSound = Gdx.audio.newSound(Gdx.files.internal("sounds/move-check.mp3"));

    public static void playMove()     { moveSound.play(); }
    public static void playCapture()  { captureSound.play(); }
    public static void playPromote()  { promoteSound.play(); }
    public static void playCheck()    { checkSound.play(); }

    public static void dispose() {
        moveSound.dispose();
        captureSound.dispose();
        promoteSound.dispose();
        checkSound.dispose();
    }
}
