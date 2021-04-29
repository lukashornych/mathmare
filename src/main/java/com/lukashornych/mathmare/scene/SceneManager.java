package com.lukashornych.mathmare.scene;

import com.lukashornych.mathmare.GameManager;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

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
     * Scenes context. Can be used freely by any scene to pass data between several scenes.
     */
    protected Map<String, Object> context;


    /**
     * Creates uninitialized manager. The {@link #init()} must be called before using
     *
     * @param gameManager
     */
    public SceneManager(@NonNull GameManager gameManager) {
        this.gameManager = gameManager;
        this.context = new HashMap<>();
    }

    /**
     * Initializes this manager. Also sets default scene as active.
     */
    public void init() {
        switchScene(SceneIdentifier.MAIN_MENU_SCENE);
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

        gameManager.getInputManager().resetKeyStates();

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
        MAIN_MENU_SCENE(MainMenuScene.class),
        DUNGEON_SCENE(DungeonScene.class),
        ESCAPED_SCENE(EscapedScene.class),
        GAME_OVER_SCENE(GameOverScene.class);

        @Getter(AccessLevel.PRIVATE)
        private final Class<? extends Scene> sceneClass;
    }
}
