package com.lukashornych.mathmare.scene;

import com.lukashornych.mathmare.Expression;
import com.lukashornych.mathmare.InputManager;
import com.lukashornych.mathmare.Player;
import com.lukashornych.mathmare.maze.MazeDescriptor;
import com.lukashornych.mathmare.maze.MazeGenerator;
import com.lukashornych.mathmare.maze.MazeTile;
import com.lukashornych.mathmare.ui.TextRendererFactory;
import com.lukashornych.mathmare.world.*;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lwjglutils.OGLTextRenderer;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Main scene of game, representing dungeon (maze) in which the player play.
 *
 * @author Lukáš Hornych
 */
@ToString
@EqualsAndHashCode
public class DungeonScene implements Scene {

    private SceneManager sceneManager;

    private final int TIME_FOR_ROOM = 2000;
    private final int EXPRESSION_SOLVED_TIME_BONUS = 5000;
    private final int EXPRESSION_WRONG_TIME_HARM = 1000;

    private World world;
    private WorldRenderer worldRenderer;

    private OGLTextRenderer uiTextRenderer;
    private OGLTextRenderer expressionSolvingTextRenderer;

    private Player player;

    private int timeRemaining;

    private boolean inInstructionsMode = true;
    private int instructionsModeTimeRemaining = 4000;

    private boolean inExpressionSolvingMode = false;
    private Expression solvingExpression = null;
    private String enteredExpressionResult = "";


    @Override
    public void setSceneManager(@NonNull SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @Override
    public void init() {
        final MazeDescriptor mazeDescriptor = MazeGenerator.generateMaze();
        final MazeTile[][] mazeRecipe = mazeDescriptor.getMaze();

        timeRemaining = mazeDescriptor.getRoomsCount() * TIME_FOR_ROOM;

        world = new WorldBuilder().buildWorld(mazeRecipe);

        setupCommonRenderOptions();

        createPlayer(mazeDescriptor);

        prepareUi();

        worldRenderer = new WorldRenderer(world, player.getCamera());
    }

    @Override
    public void update(float dt) {
        if (inInstructionsMode) {
            instructionsModeTimeRemaining -= dt * 1000f;
            if (instructionsModeTimeRemaining <= 0) {
                inInstructionsMode = false;
            }
        } else {
            timeRemaining -= dt * 1000f;
            if (timeRemaining <= 0) {
                sceneManager.switchScene(SceneManager.SceneIdentifier.GAME_OVER_SCENE);
            }
        }

        if (!inExpressionSolvingMode && !inInstructionsMode) {
            player.updatePosition(dt);

            handleDynamicObjectsInteractions();
        }

        if (inExpressionSolvingMode) {
            handleExpressionSolvingUi();
        }

        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_ESCAPE)) {
            sceneManager.switchScene(SceneManager.SceneIdentifier.GAME_OVER_SCENE);
        }

        worldRenderer.renderWorld();

        renderUi();
    }

    @Override
    public void destroy() {
        sceneManager.getGameManager().getInputManager().setMouseMode(InputManager.MouseMode.INTERACTIVE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
    }


    /**
     * Prepares scene to render UI
     */
    private void prepareUi() {
        uiTextRenderer = TextRendererFactory.createTextRenderer(sceneManager.getGameManager().getWindow(), 20f);
        expressionSolvingTextRenderer = TextRendererFactory.createTextRenderer(sceneManager.getGameManager().getWindow(), 40f);
    }

    /**
     * Initializes new player by generated maze
     *
     * @param mazeDescriptor generated maze
     */
    private void createPlayer(MazeDescriptor mazeDescriptor) {
        sceneManager.getGameManager().getInputManager().setMouseMode(InputManager.MouseMode.FREE_MOVING);

        final Vector2i playerStartingPosition = mazeDescriptor.getStartingPosition();
        player = new Player(
                sceneManager.getGameManager(),
                world.getPhysicsWorld(),
                new Vector3f(
                        playerStartingPosition.x * 5f + 2.5f,
                        2.5f,
                        -playerStartingPosition.y * 5f - 2.5f
                )
        );
    }

    /**
     * Setups OpenGL common options
     */
    private void setupCommonRenderOptions() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
    }

    /**
     * Renders whole UI base on current game state
     */
    private void renderUi() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        if (inExpressionSolvingMode) {
            renderExpressionSolvingUi();
        }
        if (inInstructionsMode) {
            renderInstructionsUi();
        }
        if (!inInstructionsMode) {
            renderInfoUi();
        }
    }

    /**
     * Renders UI with basic info for player
     */
    private void renderInfoUi() {
        if (timeRemaining < 15000) {
            uiTextRenderer.setColor(Color.RED);
        } else {
            uiTextRenderer.setColor(Color.WHITE);
        }
        uiTextRenderer.addStr2D(0, 20, "Time remaining: " + ((int) (timeRemaining / 1000f)) + "s");
    }

    /**
     * Renders UI for currently solving expression
     */
    private void renderExpressionSolvingUi() {
        glBegin(GL_TRIANGLE_STRIP);
        glColor3f(0f, 0f, 0f);
        glVertex3f(-0.6f, -0.5f, 0f);

        glColor3f(0f, 0f, 0f);
        glVertex3f(0.6f, -0.5f, 0f);

        glColor3f(0f, 0f, 0f);
        glVertex3f(-0.6f, 0.5f, 0f);

        glColor3f(0f, 0f, 0f);
        glVertex3f(0.6f, 0.5f, 0f);
        glEnd();

        expressionSolvingTextRenderer.setColor(Color.WHITE);
        expressionSolvingTextRenderer.addStr2D(300, 250, "THE DOOR IS LOCKED");
        expressionSolvingTextRenderer.addStr2D(340, 350, solvingExpression.toSolvableString());
        expressionSolvingTextRenderer.addStr2D(570, 350, enteredExpressionResult);
    }

    /**
     * Renders UI for instructions
     */
    private void renderInstructionsUi() {
        glBegin(GL_TRIANGLE_STRIP);
        glColor3f(0f, 0f, 0f);
        glVertex3f(-0.7f, -0.4f, 0f);

        glColor3f(0f, 0f, 0f);
        glVertex3f(0.7f, -0.4f, 0f);

        glColor3f(0f, 0f, 0f);
        glVertex3f(-0.7f, 0.4f, 0f);

        glColor3f(0f, 0f, 0f);
        glVertex3f(0.7f, 0.4f, 0f);
        glEnd();

        expressionSolvingTextRenderer.setColor(Color.WHITE);
        expressionSolvingTextRenderer.addStr2D(290, 275, "Find an exit portal");
        expressionSolvingTextRenderer.addStr2D(230, 335, "to escape this dungeon!");
    }

    /**
     * Handles input for dynamic objects interactions
     */
    private void handleDynamicObjectsInteractions() {
        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_F)) {
            final DynamicObject dynamicObject = world.getDynamicObject(player.getPosition());

            if (dynamicObject != null) {
                if (dynamicObject.getType().equals(DynamicObjectType.DOOR)) {
                    if (!inExpressionSolvingMode) {
                        inExpressionSolvingMode = true;
                        solvingExpression = Expression.generate();
                        enteredExpressionResult = "";
                    }
                }
                if (dynamicObject.getType().equals(DynamicObjectType.EXIT_PORTAL)) {
                    sceneManager.switchScene(SceneManager.SceneIdentifier.ESCAPED_SCENE);
                }
            }
        }
    }

    /**
     * Handles input for expression solving UI
     */
    private void handleExpressionSolvingUi() {
        boolean keyPressed = false;
        if (enteredExpressionResult.length() < 4) {
            if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_KP_0) || sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_0)) {
                keyPressed = true;
                enteredExpressionResult += "0";
            } else if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_KP_1) || sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_1)) {
                keyPressed = true;
                enteredExpressionResult += "1";
            } else if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_KP_2) || sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_2)) {
                keyPressed = true;
                enteredExpressionResult += "2";
            } else if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_KP_3) || sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_3)) {
                keyPressed = true;
                enteredExpressionResult += "3";
            } else if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_KP_4) || sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_4)) {
                keyPressed = true;
                enteredExpressionResult += "4";
            } else if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_KP_5) || sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_5)) {
                keyPressed = true;
                enteredExpressionResult += "5";
            } else if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_KP_6) || sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_6)) {
                keyPressed = true;
                enteredExpressionResult += "6";
            } else if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_KP_7) || sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_7)) {
                keyPressed = true;
                enteredExpressionResult += "7";
            } else if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_KP_8) || sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_8)) {
                keyPressed = true;
                enteredExpressionResult += "8";
            } else if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_KP_9) || sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_9)) {
                keyPressed = true;
                enteredExpressionResult += "9";
            } else if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_MINUS)) {
                keyPressed = true;
                enteredExpressionResult += "9";
            }
        }

        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_BACKSPACE) && enteredExpressionResult.length() > 0) {
            keyPressed = true;
            enteredExpressionResult = enteredExpressionResult.substring(0, enteredExpressionResult.length() - 1);
        }

        if ((sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_ENTER) || sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_KP_ENTER)) && enteredExpressionResult.length() > 0) {
            keyPressed = true;
            final int parsedEnteredResult = Integer.parseInt(enteredExpressionResult);
            if (solvingExpression.isResultCorrect(parsedEnteredResult)) {
                final DynamicObject dynamicObject = world.getDynamicObject(player.getPosition());
                world.getAllDynamicObjects().remove(dynamicObject);
                world.getPhysicsWorld().getObjects().remove(dynamicObject.getBoundingBox());

                inExpressionSolvingMode = false;
                solvingExpression = null;
                timeRemaining += EXPRESSION_SOLVED_TIME_BONUS;
            } else {
                timeRemaining -= EXPRESSION_WRONG_TIME_HARM;
                solvingExpression = Expression.generate();
            }
            enteredExpressionResult = "";
        }

        // reset all key states to disallow unwanted key repeats
        if (keyPressed) {
            sceneManager.getGameManager().getInputManager().resetKeyStates();
        }
    }
}
