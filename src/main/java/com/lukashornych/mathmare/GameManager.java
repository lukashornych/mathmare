package com.lukashornych.mathmare;

import com.lukashornych.mathmare.scene.SceneManager;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

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
     *
     * @param window
     */
    public GameManager(@NonNull Window window) {
        this.window = window;

        this.inputManager = new InputManager(this);
        this.sceneManager = new SceneManager(this);
    }

    /**
     * Initialize this manager as well as all underlying managers to usable state.
     */
    public void init() {
        inputManager.init();
        sceneManager.init();
    }

    /**
     * Updates game
     *
     * @param dt delta time from previous render
     */
    public void update(float dt) {
        inputManager.update();
        sceneManager.update(dt);
    }
}
