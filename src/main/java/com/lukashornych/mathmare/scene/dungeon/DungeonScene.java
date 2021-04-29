package com.lukashornych.mathmare.scene.dungeon;

import com.lukashornych.mathmare.Expression;
import com.lukashornych.mathmare.InputManager;
import com.lukashornych.mathmare.Player;
import com.lukashornych.mathmare.maze.MazeDescriptor;
import com.lukashornych.mathmare.maze.MazeGenerator;
import com.lukashornych.mathmare.maze.MazeTile;
import com.lukashornych.mathmare.physics.BoundingBox;
import com.lukashornych.mathmare.physics.PhysicsWorld;
import com.lukashornych.mathmare.scene.Scene;
import com.lukashornych.mathmare.scene.SceneManager;
import com.lukashornych.mathmare.ui.TextRendererFactory;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLTexture2D;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.lukashornych.mathmare.maze.MazeGenerator.MAZE_SIZE;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Main scene of game, representing dungeon (maze) in which the player play.
 *
 * @author Lukáš Hornych
 */
@ToString
@EqualsAndHashCode
public class DungeonScene implements Scene {

    private SceneManager sceneManager;

    private final float TILE_WORLD_SIZE = 5f;

    private final int TIME_FOR_ROOM = 2000;
    private final int EXPRESSION_SOLVED_TIME_BONUS = 5000;
    private final int EXPRESSION_WRONG_TIME_HARM = 1000;

    private final List<DynamicObject> allDynamicObjects = new ArrayList<>();
    private final DynamicObject[][] dynamicObjectsInWorld = new DynamicObject[MAZE_SIZE][MAZE_SIZE];

    private int wallVaoId;
    private int wallIboId;
    private int wallIndicesCount;

    private int floorVaoId;
    private int floorIboId;
    private int floorIndicesCount;

    private final PhysicsWorld physicsWorld = new PhysicsWorld();

    private Player player;

    private OGLTextRenderer uiTextRenderer;
    private OGLTextRenderer expressionSolvingTextRenderer;

    private int timeRemaining;

    private boolean inInstructionsMode = true;
    private int instructionsModeTimeRemaining = 4000;

    private boolean inSolvingExpressionMode = false;
    private Expression solvingExpression = null;
    private String enteredExpressionResult = "";

    private OGLTexture2D wallTexture;
    private OGLTexture2D floorTexture;
    private OGLTexture2D doorTexture;
    private OGLTexture2D exitPortalTexture;


    @Override
    public void setSceneManager(@NonNull SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @Override
    public void init() {
        final MazeDescriptor mazeDescriptor = MazeGenerator.generateMaze();
        final MazeTile[][] mazeRecipe = mazeDescriptor.getMaze();

        timeRemaining = mazeDescriptor.getRoomsCount() * TIME_FOR_ROOM;

        loadTextures();

        buildWorld(mazeRecipe);

        setupCommonRenderOptions();

        createPlayer(mazeDescriptor);

        prepareUi();
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

        if (!inSolvingExpressionMode && !inInstructionsMode) {
            player.updatePosition(dt);

            if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_F)) {
                final DynamicObject dynamicObject = getDynamicObject(player.getPosition());

                if (dynamicObject != null) {
                    if (dynamicObject.getType().equals(DynamicObjectType.DOOR)) {
                        if (!inSolvingExpressionMode) {
                            inSolvingExpressionMode = true;
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

        if (inSolvingExpressionMode) {
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
                    final DynamicObject dynamicObject = getDynamicObject(player.getPosition());
                    allDynamicObjects.remove(dynamicObject);
                    physicsWorld.getObjects().remove(dynamicObject.getBoundingBox());

                    inSolvingExpressionMode = false;
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

        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_ESCAPE)) {
            sceneManager.switchScene(SceneManager.SceneIdentifier.GAME_OVER_SCENE);
        }

        renderWorld();

        renderUi();
    }

    @Override
    public void destroy() {
        sceneManager.getGameManager().getInputManager().setMouseMode(InputManager.MouseMode.INTERACTIVE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
    }


    private DynamicObject getDynamicObject(Vector3f positionInWorld) {
        final int positionXInMaze = (int) (positionInWorld.x / 5);
        final int positionYInMaze = (int) (-positionInWorld.z / 5);

        return dynamicObjectsInWorld[positionXInMaze][positionYInMaze];
    }


    private void loadTextures() {
        try {
            wallTexture = new OGLTexture2D("assets/texture/bricks.png");
            floorTexture = new OGLTexture2D("assets/texture/pavement.png");
            doorTexture = new OGLTexture2D("assets/texture/locked-doors.png");
            exitPortalTexture = new OGLTexture2D("assets/texture/portal.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FloatBuffer extractVertexes(List<Vertex> vertexes) {
        final float[] extractedVertexes = new float[vertexes.size() * 8];
        for (int i = 0; i < vertexes.size(); i++) {
            final Vertex v = vertexes.get(i);
            extractedVertexes[i * 8] = v.getPosition().x;
            extractedVertexes[i * 8 + 1] = v.getPosition().y;
            extractedVertexes[i * 8 + 2] = v.getPosition().z;
            extractedVertexes[i * 8 + 3] = v.getTextCoords().x;
            extractedVertexes[i * 8 + 4] = v.getTextCoords().y;
            extractedVertexes[i * 8 + 5] = v.getColor().x;
            extractedVertexes[i * 8 + 6] = v.getColor().y;
            extractedVertexes[i * 8 + 7] = v.getColor().z;
        }

        final FloatBuffer buffer = BufferUtils.createFloatBuffer(extractedVertexes.length);
        buffer.put(extractedVertexes).flip();
        return buffer;
    }

    private void buildBackWall(List<Vertex> wallVertexes, List<Integer> wallVertexIndices, int wallQuadCounter, int x, int y) {
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE, 0f, -y * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(0f, 1f)));
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE + TILE_WORLD_SIZE, TILE_WORLD_SIZE, -y * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(1f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE, TILE_WORLD_SIZE, -y * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(0f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -y * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(1f, 1f)));

        addQuadVertexIndices(wallVertexIndices, wallQuadCounter);
    }

    private void addQuadVertexIndices(List<Integer> vertexIndices, int quadCounter) {
        vertexIndices.add(quadCounter * 4);
        vertexIndices.add(quadCounter * 4 + 1);
        vertexIndices.add(quadCounter * 4 + 2);

        vertexIndices.add(quadCounter * 4);
        vertexIndices.add(quadCounter * 4 + 3);
        vertexIndices.add(quadCounter * 4 + 1);
    }

    private void buildFrontWall(List<Vertex> wallVertexes, List<Integer> wallVertexIndices, int wallQuadCounter, int x, int y) {
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -y * TILE_WORLD_SIZE), new Vector2f(0f, 1f)));
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE, TILE_WORLD_SIZE, -y * TILE_WORLD_SIZE), new Vector2f(1f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE + TILE_WORLD_SIZE, TILE_WORLD_SIZE, -y * TILE_WORLD_SIZE), new Vector2f(0f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE, 0f, -y * TILE_WORLD_SIZE), new Vector2f(1f, 1f)));

        addQuadVertexIndices(wallVertexIndices, wallQuadCounter);
    }

    private void buildRightWall(List<Vertex> wallVertexes, List<Integer> wallVertexIndices, int wallQuadCounter, int x, int y) {
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -y * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(0f, 1f)));
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE + TILE_WORLD_SIZE, TILE_WORLD_SIZE, -y * TILE_WORLD_SIZE), new Vector2f(1f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE + TILE_WORLD_SIZE, TILE_WORLD_SIZE, -y * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(0f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -y * TILE_WORLD_SIZE), new Vector2f(1f, 1f)));

        addQuadVertexIndices(wallVertexIndices, wallQuadCounter);
    }

    private void buildLeftWall(List<Vertex> wallVertexes, List<Integer> wallVertexIndices, int wallQuadCounter, int x, int y) {
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE, 0f, -y * TILE_WORLD_SIZE), new Vector2f(0f, 1f)));
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE, TILE_WORLD_SIZE, -y * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(1f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE, TILE_WORLD_SIZE, -y * TILE_WORLD_SIZE), new Vector2f(0f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE, 0f, -y * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(1f, 1f)));

        addQuadVertexIndices(wallVertexIndices, wallQuadCounter);
    }

    private void buildCeiling(List<Vertex> wallVertexes, List<Integer> wallVertexIndices, int wallQuadCounter, int x, int y) {
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE, TILE_WORLD_SIZE, -y * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(0f, 1f)));
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE + TILE_WORLD_SIZE, TILE_WORLD_SIZE, -y * TILE_WORLD_SIZE), new Vector2f(1f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE, TILE_WORLD_SIZE, -y * TILE_WORLD_SIZE), new Vector2f(0f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE + TILE_WORLD_SIZE, TILE_WORLD_SIZE, -y * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(1f, 1f)));

        addQuadVertexIndices(wallVertexIndices, wallQuadCounter);
    }

    private void buildFloor(List<Vertex> floorVertexes, List<Integer> floorVertexIndices, int floorQuadCounter, int x, int y) {
        floorVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE, 0f, -y * TILE_WORLD_SIZE), new Vector2f(0f, 1f)));
        floorVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -y * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(1f, 0f)));
        floorVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE, 0f, -y * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(0f, 0f)));
        floorVertexes.add(new Vertex(new Vector3f(x * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -y * TILE_WORLD_SIZE), new Vector2f(1f, 1f)));

        addQuadVertexIndices(floorVertexIndices, floorQuadCounter);
    }

    private void buildExitPortal(int x, int y) {
        final int dlIndex = glGenLists(1);

        glNewList(dlIndex, GL_COMPILE);

        // front
        glBegin(GL_TRIANGLE_STRIP);
        glTexCoord2f(0f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * TILE_WORLD_SIZE, 0f, -y * TILE_WORLD_SIZE - 2f);
        glTexCoord2f(0.84f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -y * TILE_WORLD_SIZE - 2f);
        glTexCoord2f(0f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * TILE_WORLD_SIZE, TILE_WORLD_SIZE, -y * TILE_WORLD_SIZE - 2f);
        glTexCoord2f(0.84f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * TILE_WORLD_SIZE + TILE_WORLD_SIZE, TILE_WORLD_SIZE, -y * TILE_WORLD_SIZE - 2f);
        glEnd();

        // back
        glBegin(GL_TRIANGLE_STRIP);
        glTexCoord2f(0.84f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -y * TILE_WORLD_SIZE - 3f);
        glTexCoord2f(0f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * 5f, 0f, -y * 5f - 3f);
        glTexCoord2f(0.84f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * 5f + 5f, 5f, -y * 5f - 3f);
        glTexCoord2f(0f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * 5f, 5f, -y * 5f - 3f);
        glEnd();

        // left side
        glBegin(GL_TRIANGLE_STRIP);
        glTexCoord2f(0.84f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * 5f, 0f, -y * 5f - 3f);
        glTexCoord2f(1f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * 5f, 0f, -y * 5f - 2f);
        glTexCoord2f(0.84f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * 5f, 5f, -y * 5f - 3f);
        glTexCoord2f(1f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * 5f, 5f, -y * 5f - 2f);
        glEnd();

        // right side
        glBegin(GL_TRIANGLE_STRIP);
        glTexCoord2f(0.84f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * 5f + 5f, 0f, -y * 5f - 2f);
        glTexCoord2f(1f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * 5f + 5f, 0f, -y * 5f - 3f);
        glTexCoord2f(0.84f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * 5f + 5f, 5f, -y * 5f - 2f);
        glTexCoord2f(1f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(x * 5f + 5, 5f, -y * 5f - 3f);
        glEnd();

        glEndList();

        final BoundingBox boundingBox = new BoundingBox(
                x * 5f,
                x * 5f + 5f,
                y * 5f + 2f,
                y * 5f + 3f
        );
        physicsWorld.getObjects().add(boundingBox);

        final DynamicObject exitPortalObject = new DynamicObject(DynamicObjectType.EXIT_PORTAL, dlIndex, boundingBox, exitPortalTexture);
        allDynamicObjects.add(exitPortalObject);
        dynamicObjectsInWorld[x][y] = exitPortalObject;
    }

    private void buildDoor(int x, int y, MazeTile leftTile, MazeTile rightTile, MazeTile topTile, MazeTile bottomTile) {
        final int dlIndex = glGenLists(1);
        BoundingBox boundingBox = null;

        if ((leftTile.equals(MazeTile.VOID)) && (rightTile.equals(MazeTile.VOID))) {
            glNewList(dlIndex, GL_COMPILE);
            glBegin(GL_TRIANGLE_STRIP);

            glTexCoord2f(0f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f, 0f, -y * 5f - 2.4f);
            glTexCoord2f(1f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f + 5f, 0f, -y * 5f - 2.4f);
            glTexCoord2f(0f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f, 5f, -y * 5f - 2.4f);
            glTexCoord2f(1f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f + 5f, 5f, -y * 5f - 2.4f);

            glEnd();

            glBegin(GL_TRIANGLE_STRIP);

            glTexCoord2f(1f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f + 5f, 0f, -y * 5f - 2.6f);
            glTexCoord2f(0f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f, 0f, -y * 5f - 2.6f);
            glTexCoord2f(1f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f + 5f, 5f, -y * 5f - 2.6f);
            glTexCoord2f(0f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f, 5f, -y * 5f - 2.6f);

            glEnd();
            glEndList();

            boundingBox = new BoundingBox(
                    x * 5f,
                    x * 5f + 5f,
                    y * 5f + 2.4f,
                    y * 5f + 2.6f
            );
            physicsWorld.getObjects().add(boundingBox);
        } else if ((topTile.equals(MazeTile.VOID)) && (bottomTile.equals(MazeTile.VOID))) {
            glNewList(dlIndex, GL_COMPILE);
            glBegin(GL_TRIANGLE_STRIP);

            glTexCoord2f(0f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f + 2.4f, 0f, -y * 5f - 5f);
            glTexCoord2f(1f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f + 2.4f, 0f, -y * 5f);
            glTexCoord2f(0f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f + 2.4f, 5f, -y * 5f - 5f);
            glTexCoord2f(1f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f + 2.4f, 5f, -y * 5f);

            glEnd();

            glBegin(GL_TRIANGLE_STRIP);

            glTexCoord2f(1f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f + 2.6f, 0f, -y * 5f);
            glTexCoord2f(0f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f + 2.6f, 0f, -y * 5f - 5f);
            glTexCoord2f(1f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f + 2.6f, 5f, -y * 5f);
            glTexCoord2f(0f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(x * 5f + 2.6f, 5f, -y * 5f - 5f);

            glEnd();
            glEndList();

            boundingBox = new BoundingBox(
                    x * 5f + 2.4f,
                    x * 5f + 2.6f,
                    y * 5f,
                    y * 5f + 5f
            );
            physicsWorld.getObjects().add(boundingBox);
        }

        final DynamicObject doorObject = new DynamicObject(DynamicObjectType.DOOR, dlIndex, boundingBox, doorTexture);
        allDynamicObjects.add(doorObject);
        dynamicObjectsInWorld[x][y] = doorObject;
    }

    private void fillIbo(int iboId, List<Integer> vertexIndices) {
        final int[] indicesArray = new int[vertexIndices.size()];
        for (int i = 0; i < vertexIndices.size(); i++) {
            indicesArray[i] = vertexIndices.get(i);
        }

        final IntBuffer indexBuffer = BufferUtils.createIntBuffer(vertexIndices.size());
        indexBuffer.put(indicesArray).flip();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
    }

    private void fillVao(int vaoId, FloatBuffer vertexBuffer) {
        glBindVertexArray(vaoId);

        final int vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        glVertexPointer(3, GL_FLOAT, 8 * 4, 0);
        glTexCoordPointer(2, GL_FLOAT, 8 * 4, 3 * 4);
        glColorPointer(3, GL_FLOAT, 8 * 4, 5 * 4);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_INDEX_ARRAY);

        glBindVertexArray(0);
    }

    private void buildWorld(MazeTile[][] mazeRecipe) {
        final List<Vertex> wallVertexes = new ArrayList<>();
        final List<Integer> wallVertexIndices = new ArrayList<>();

        final List<Vertex> floorVertexes = new ArrayList<>();
        final List<Integer> floorVertexIndices = new ArrayList<>();

        int wallQuadCounter = 0;
        int floorQuadCounter = 0;
        for (int x = 0; x < MAZE_SIZE; x++) {
            for (int y = 0; y < MAZE_SIZE; y++) {
                final MazeTile tile = mazeRecipe[x][y];

                // create only bounding box for wall tile
                if (tile.equals(MazeTile.VOID)) {
                    physicsWorld.getObjects().add(new BoundingBox(
                            x * 5f,
                            x * 5f + 5f,
                            y * 5f,
                            y * 5f + 5f
                    ));
                    continue;
                }

                final MazeTile leftTile = mazeRecipe[x - 1][y];
                final MazeTile rightTile = mazeRecipe[x + 1][y];
                final MazeTile frontTile = mazeRecipe[x][y - 1];
                final MazeTile backTile = mazeRecipe[x][y + 1];

                if (tile.equals(MazeTile.DOOR)) {
                    buildDoor(x, y, leftTile, rightTile, frontTile, backTile);
                }
                if (tile.equals(MazeTile.EXIT_PORTAL)) {
                    buildExitPortal(x, y);
                }

                buildFloor(floorVertexes, floorVertexIndices, floorQuadCounter, x, y);
                floorQuadCounter++;

                buildCeiling(wallVertexes, wallVertexIndices, wallQuadCounter, x, y);
                wallQuadCounter++;

                if (leftTile.equals(MazeTile.VOID)) {
                    buildLeftWall(wallVertexes, wallVertexIndices, wallQuadCounter, x, y);
                    wallQuadCounter++;
                }

                if (rightTile.equals(MazeTile.VOID)) {
                    buildRightWall(wallVertexes, wallVertexIndices, wallQuadCounter, x, y);
                    wallQuadCounter++;
                }

                if (frontTile.equals(MazeTile.VOID)) {
                    buildFrontWall(wallVertexes, wallVertexIndices, wallQuadCounter, x, y);
                    wallQuadCounter++;
                }

                if (backTile.equals(MazeTile.VOID)) {
                    buildBackWall(wallVertexes, wallVertexIndices, wallQuadCounter, x, y);
                    wallQuadCounter++;
                }
            }
        }

        wallIndicesCount = wallVertexIndices.size();

        wallVaoId = glGenVertexArrays();
        fillVao(wallVaoId, extractVertexes(wallVertexes));
        wallIboId = glGenBuffers();
        fillIbo(wallIboId, wallVertexIndices);

        floorIndicesCount = floorVertexIndices.size();

        floorVaoId = glGenVertexArrays();
        fillVao(floorVaoId, extractVertexes(floorVertexes));
        floorIboId = glGenBuffers();
        fillIbo(floorIboId, floorVertexIndices);
    }

    private void prepareUi() {
        uiTextRenderer = TextRendererFactory.createTextRenderer(sceneManager.getGameManager().getWindow(), 20f);
        expressionSolvingTextRenderer = TextRendererFactory.createTextRenderer(sceneManager.getGameManager().getWindow(), 40f);
    }

    private void createPlayer(MazeDescriptor mazeDescriptor) {
        sceneManager.getGameManager().getInputManager().setMouseMode(InputManager.MouseMode.FREE_MOVING);

        final Vector2i playerStartingPosition = mazeDescriptor.getStartingPosition();
        player = new Player(
                sceneManager.getGameManager(),
                physicsWorld,
                new Vector3f(
                        playerStartingPosition.x * 5f + 2.5f,
                        2.5f,
                        -playerStartingPosition.y * 5f - 2.5f
                )
        );
    }

    private void setupCommonRenderOptions() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
    }


    /**
     * Renders whole game world (dungeon)
     */
    private void renderWorld() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        final FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);
        player.getCamera().getProjection().get(projectionBuffer);
        glMultMatrixf(projectionBuffer);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        final FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
        player.getCamera().getView().get(viewBuffer);
        glMultMatrixf(viewBuffer);

        // render static walls
        glBindVertexArray(wallVaoId);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, wallIboId);

        wallTexture.bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glDrawElements(GL_TRIANGLES, wallIndicesCount, GL_UNSIGNED_INT, 0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        // render static floors
        glBindVertexArray(floorVaoId);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, floorIboId);

        floorTexture.bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glDrawElements(GL_TRIANGLES, floorIndicesCount, GL_UNSIGNED_INT, 0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        // render dynamic objects
        allDynamicObjects.forEach(object -> {
            object.getTexture().bind();
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glCallList(object.getDisplayListId());
        });
    }

    /**
     * Renders whole UI base on current game state
     */
    private void renderUi() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        if (inSolvingExpressionMode) {
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
}
