package com.lukashornych.mathmare.scene;

import com.lukashornych.mathmare.InputManager;
import lombok.Data;
import lwjglutils.OGLTextRenderer;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * Scene telling player that he/she successfully escaped maze.
 *
 * @author Lukáš Hornych 2021
 */
@Data
public class EscapedScene implements Scene {

    public static final String DUNGEON_ESCAPED_IN_PARAM = "dungeonEscapedIn";

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

        sceneManager.getGameManager().getInputManager().setMouseMode(InputManager.MouseMode.FREE_MOVING);
    }

    @Override
    public void update(float dt) {
        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_ENTER)) {
            sceneManager.switchScene(SceneManager.SceneIdentifier.DUNGEON_SCENE);
        }
        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_ESCAPE)) {
            sceneManager.switchScene(SceneManager.SceneIdentifier.MAIN_MENU_SCENE);
        }

        headlineTextRenderer.setColor(Color.GREEN);
        headlineTextRenderer.addStr2D(295, 230, "YOU ESCAPED!");

        defaultTextRenderer.setColor(Color.WHITE);
        defaultTextRenderer.addStr2D(225, 300, "You did it! You successfully escaped the dungeon.");

        defaultTextRenderer.addStr2D(310, 520, "Press ENTER to try again better...");
    }

    @Override
    public void destroy() {
        sceneManager.getGameManager().getInputManager().setMouseMode(InputManager.MouseMode.INTERACTIVE);
    }
}
