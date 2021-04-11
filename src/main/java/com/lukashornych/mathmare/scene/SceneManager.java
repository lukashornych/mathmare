package com.lukashornych.mathmare.scene;

import com.lukashornych.mathmare.GameManager;
import lombok.*;

/**
 * Manages all existing {@link Scene}s in game like switching between them and so on.
 *
 * @see com.lukashornych.mathmare.GameManager
 * @author Lukáš Hornych
 */
@Getter
@ToString
@EqualsAndHashCode
public class SceneManager {

    private final GameManager gameManager;

    /**
     * Current active scene
     */
    protected Scene currentScene;

    /**
     * Creates uninitialized manager. The {@link #init()} must be called before using
     *
     * @param gameManager
     */
    public SceneManager(@NonNull GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * Initializes this manager. Also sets default scene as active.
     */
    public void init() {
        switchScene(SceneIdentifier.MAZE_SCENE);
    }

    /**
     * Switching current scene to new one
     *
     * @param newScene identifier of new scene
     */
    public void switchScene(@NonNull SceneIdentifier newScene) {
        if (currentScene != null) {
            currentScene.destroy();
        }

        try {
            currentScene = newScene.getSceneClass().getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate scene " + newScene.name());
        }
        currentScene.setSceneManager(this);
        currentScene.init();
    }

    /**
     * Updates current scene
     *
     * @param dt delta time from previous render
     */
    public void update(float dt) {
        if (currentScene == null) {
            return;
        }

        currentScene.update(dt);
    }


    /**
     * List of all available scene in game
     */
    @RequiredArgsConstructor
    public enum SceneIdentifier {
        MAZE_SCENE(MazeScene.class);

        @Getter(AccessLevel.PRIVATE)
        private final Class<? extends Scene> sceneClass;
    }
}
