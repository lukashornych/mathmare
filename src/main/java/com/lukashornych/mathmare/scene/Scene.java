package com.lukashornych.mathmare.scene;

/**
 * Represents single game scene
 *
 * @author Lukáš Hornych
 */
public interface Scene {

    /**
     * Sets scene manager for the scene.
     *
     * @param sceneManager manager which manages this scene
     */
    void setSceneManager(SceneManager sceneManager);

    /**
     * Initializes the scene
     */
    void init();

    /**
     * Scene update
     *
     * @param dt time delta from previous render
     */
    void update(float dt);

    /**
     * Destroys the scene.
     */
    void destroy();
}
