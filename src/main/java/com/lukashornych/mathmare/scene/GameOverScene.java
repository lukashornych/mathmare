package com.lukashornych.mathmare.scene;

import com.lukashornych.mathmare.InputManager;
import com.lukashornych.mathmare.ui.TextRendererFactory;
import lombok.Data;
import lwjglutils.OGLTextRenderer;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * Game over scene telling player that he/she lost.
 *
 * @author Lukáš Hornych 2021
 */
@Data
public class GameOverScene implements Scene {

    private SceneManager sceneManager;

    private OGLTextRenderer defaultTextRenderer;
    private OGLTextRenderer headlineTextRenderer;

    @Override
    public void init() {
        defaultTextRenderer = TextRendererFactory.createTextRenderer(sceneManager.getGameManager().getWindow(), 20f);
        headlineTextRenderer = TextRendererFactory.createTextRenderer(sceneManager.getGameManager().getWindow(), 60f);

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

        headlineTextRenderer.setColor(Color.RED);
        headlineTextRenderer.addStr2D(340, 230, "GAME OVER!");

        defaultTextRenderer.setColor(Color.WHITE);
        defaultTextRenderer.addStr2D(350, 300, "You didn't make it to the exit.");

        defaultTextRenderer.addStr2D(365, 520, "Press ENTER to try again...");
    }

    @Override
    public void destroy() {
        sceneManager.getGameManager().getInputManager().setMouseMode(InputManager.MouseMode.INTERACTIVE);
    }
}
