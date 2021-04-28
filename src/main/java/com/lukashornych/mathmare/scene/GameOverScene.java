package com.lukashornych.mathmare.scene;

import lombok.Data;
import lwjglutils.OGLTextRenderer;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Game over scene telling player that he/she lost.
 *
 * @author Lukáš Hornych 2021
 */
@Data
public class GameOverScene implements Scene {

    private SceneManager sceneManager;

    // todo text renderer factory
    private OGLTextRenderer defaultTextRenderer;
    private OGLTextRenderer headlineTextRenderer;

    @Override
    public void init() {
        // todo separate to common text renderer factory
        final Font pixelDigivolveFont;
        try {
            pixelDigivolveFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/assets/font/pixel-digivolve.otf"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        defaultTextRenderer = new OGLTextRenderer(
                sceneManager.getGameManager().getWindow().getWidth(),
                sceneManager.getGameManager().getWindow().getHeight(),
                pixelDigivolveFont.deriveFont(Font.PLAIN, 20f)
        );
        headlineTextRenderer = new OGLTextRenderer(
                sceneManager.getGameManager().getWindow().getWidth(),
                sceneManager.getGameManager().getWindow().getHeight(),
                pixelDigivolveFont.deriveFont(Font.PLAIN, 60f)
        );
    }

    @Override
    public void update(float dt) {
        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_ENTER)) {
            sceneManager.switchScene(SceneManager.SceneIdentifier.DUNGEON_SCENE);
        }
        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_ESCAPE)) {
            sceneManager.switchScene(SceneManager.SceneIdentifier.MAIN_MENU_SCENE);
        }

        headlineTextRenderer.setColor(Color.RED);
        headlineTextRenderer.addStr2D(340, 230, "GAME OVER!");

        defaultTextRenderer.setColor(Color.WHITE);
        defaultTextRenderer.addStr2D(350, 300, "You didn't make it to the exit.");

        defaultTextRenderer.addStr2D(365, 520, "Press ENTER to try again...");
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
