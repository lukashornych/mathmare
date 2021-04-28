package com.lukashornych.mathmare;

import lwjglutils.OGLUtils;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Application which start the whole game.
 *
 * @author Lukáš Hornych
 */
public class Mathmare {

    private Window window;
    private GameManager gameManager;

    public static void main(String... args) {
        new Mathmare().run();
    }

    public void run() {
        window = new Window(1024, 576);
        window.init();

        gameManager = new GameManager(window);
        gameManager.init();

        loop();

        window.destroy();
    }

    // todo refactor? to gamemanager?
    private void loop() {
        long prevTime = System.currentTimeMillis();
        float deltaTime = -1.0f;

        while (!glfwWindowShouldClose(window.getId())) {
            glViewport(0, 0, window.getWidth(), window.getHeight());

            glfwPollEvents();

            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (deltaTime > 0) {
                gameManager.update(deltaTime);
            }

            glfwSwapBuffers(window.getId());

            final long currentTime = System.currentTimeMillis();
            deltaTime = (currentTime - prevTime) / 1000.0f;
            prevTime = currentTime;
        }
    }
}
