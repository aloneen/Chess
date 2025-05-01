package com.mygdx.chess.proxy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;

import java.util.HashMap;
import java.util.Map;

/**
 * Proxy for managing texture loading and reuse.
 */
public class TextureProxy {
    private static final Map<String, Texture> cache = new HashMap<>();

    public static Texture get(String path) {
        if (!cache.containsKey(path)) {
            cache.put(path, new Texture(Gdx.files.internal(path)));
        }
        return cache.get(path);
    }

    public static void disposeAll() {
        for (Texture texture : cache.values()) {
            texture.dispose();
        }
        cache.clear();
    }
}
