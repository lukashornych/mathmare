package com.lukashornych.mathmare.scene;

import lombok.Data;
import lwjglutils.OGLTextRenderer;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Main menu scene, initial scene.
 *
 * @author Lukáš Hornych, netreach.me 2021
 */
@Data
public class MainMenuScene implements Scene {

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
                pixelDigivolveFont.deriveFont(Font.PLAIN, 80f)
        );
    }

    @Override
    public void update(float dt) {
        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_ENTER)) {
            sceneManager.switchScene(SceneManager.SceneIdentifier.DUNGEON_SCENE);
        }
        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_ESCAPE)) {
            glfwSetWindowShouldClose(sceneManager.getGameManager().getWindow().getId(), true);
        }

        headlineTextRenderer.setColor(Color.RED);
        headlineTextRenderer.addStr2D(290, 230, "MATHMARE");

        defaultTextRenderer.setColor(new Color(0xa30000));
        defaultTextRenderer.addStr2D(335, 250, "Will you escape the dungeon?");

        defaultTextRenderer.setColor(Color.WHITE);
        defaultTextRenderer.addStr2D(385, 370, "Press ENTER to try...");

        defaultTextRenderer.setColor(new Color(0x333333));
        defaultTextRenderer.addStr2D(325, 520, "GAME BY LUKAS HORNYCH IN 2021");
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
