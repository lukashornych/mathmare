package com.lukashornych.mathmare;

import com.lukashornych.mathmare.scene.SceneManager;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Manager of entire game instance. It manages flow of the game using underlying managers and should be also
 * passed to them to be accessible from anywhere.
 *
 * @author Lukáš Hornych
 */
@Getter
@ToString
@EqualsAndHashCode
public class GameManager {

    protected Window window;

    protected InputManager inputManager;
    protected SceneManager sceneManager;

    /**
     * Creates new game manager for current GLFW window. This manager should only one in game instance.
     */
    public GameManager() {
        this.inputManager = new InputManager(this);
        this.sceneManager = new SceneManager(this);
    }

    /**
     * Initialize this manager as well as all underlying managers to usable state.
     */
    public void init() {
        window = new Window(1024, 576);
        window.init();

        inputManager.init();
        sceneManager.init();
    }

    /**
     * Updates game
     */
    public void run() {
        long prevTime = System.currentTimeMillis();
        float deltaTime = -1.0f;

        while (!glfwWindowShouldClose(window.getId())) {
            glViewport(0, 0, window.getWidth(), window.getHeight());

            glfwPollEvents();

            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (deltaTime > 0) {
                inputManager.update();
                sceneManager.update(deltaTime);
            }

            glfwSwapBuffers(window.getId());

            final long currentTime = System.currentTimeMillis();
            deltaTime = (currentTime - prevTime) / 1000.0f;
            prevTime = currentTime;
        }
    }

    public void destroy() {
        window.destroy();
    }
}
