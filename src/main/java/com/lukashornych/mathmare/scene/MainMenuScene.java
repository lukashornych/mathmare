package com.lukashornych.mathmare.scene;

import com.lukashornych.mathmare.InputManager;
import lombok.Data;
import lwjglutils.OGLTextRenderer;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Main menu scene, initial scene.
 *
 * @author Lukáš Hornych 2021
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

        sceneManager.getGameManager().getInputManager().setMouseMode(InputManager.MouseMode.FREE_MOVING);
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
        defaultTextRenderer.addStr2D(20, 540, "Game by Lukas Hornych 2021");
        defaultTextRenderer.addStr2D(20, 560, "Textures by Jestan");
        defaultTextRenderer.addStr2D(890, 500, "Controls:");
        defaultTextRenderer.addStr2D(620, 520, "Use \"F\" to interact with objects");
        defaultTextRenderer.addStr2D(640, 540, "Use \"ENTER\" to confirm actions");
        defaultTextRenderer.addStr2D(640, 560, "Use \"KEYPAD\" to enter numbers");
    }

    @Override
    public void destroy() {
        sceneManager.getGameManager().getInputManager().setMouseMode(InputManager.MouseMode.INTERACTIVE);
    }
}
