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
import lombok.*;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLTexture2D;
import org.joml.Random;
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

    private final int TIME_FOR_ROOM = 2000;
    private final int EXPRESSION_SOLVED_TIME_BONUS = 5000;
    private final int EXPRESSION_WRONG_TIME_HARM = 1000;

    private final WorldTileDescriptor[][] builtMaze = new WorldTileDescriptor[MAZE_SIZE][MAZE_SIZE];

    private final List<Vertex> wallVertexes = new ArrayList<>();
    private final List<Integer> wallVertexIndices = new ArrayList<>();
    private final List<Vertex> floorVertexes = new ArrayList<>();
    private final List<Integer> floorVertexIndices = new ArrayList<>();

    private final PhysicsWorld physicsWorld = new PhysicsWorld();

    private int wallVaoId;
    private int wallIboId;
    private int floorVaoId;
    private int floorIboId;

    private final List<WorldTileDescriptor> dynamicTiles = new ArrayList<>();

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
        try {
            wallTexture = new OGLTexture2D("assets/texture/bricks.png");
            floorTexture = new OGLTexture2D("assets/texture/pavement.png");
            doorTexture = new OGLTexture2D("assets/texture/locked-doors.png");
            exitPortalTexture = new OGLTexture2D("assets/texture/portal.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final MazeDescriptor mazeDescriptor = MazeGenerator.generateMaze();
        final MazeTile[][] mazeRecipe = mazeDescriptor.getMaze();

        timeRemaining = mazeDescriptor.getRoomsCount() * TIME_FOR_ROOM;

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
                final MazeTile topTile = mazeRecipe[x][y - 1];
                final MazeTile bottomTile = mazeRecipe[x][y + 1];

                // create door
                // todo
                if (tile.equals(MazeTile.DOOR)) {
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

                    final DoorTileDescriptor tileDescriptor = new DoorTileDescriptor(dlIndex, boundingBox, doorTexture);
                    dynamicTiles.add(tileDescriptor);
                    builtMaze[x][y] = tileDescriptor;
                }

                // todo
                if (tile.equals(MazeTile.EXIT_PORTAL)) {
                    final int dlIndex = glGenLists(1);

                    glNewList(dlIndex, GL_COMPILE);

                    // front
                    glBegin(GL_TRIANGLE_STRIP);
                        glTexCoord2f(0f, 1f);
                        glColor3f(1f, 1f, 1f);
                        glVertex3f(x * 5f, 0f, -y * 5f - 2f);
                        glTexCoord2f(0.84f, 1f);
                        glColor3f(1f, 1f, 1f);
                        glVertex3f(x * 5f + 5f, 0f, -y * 5f - 2f);
                        glTexCoord2f(0f, 0f);
                        glColor3f(1f, 1f, 1f);
                        glVertex3f(x * 5f, 5f, -y * 5f - 2f);
                        glTexCoord2f(0.84f, 0f);
                        glColor3f(1f, 1f, 1f);
                        glVertex3f(x * 5f + 5f, 5f, -y * 5f - 2f);
                    glEnd();

                    // back
                    glBegin(GL_TRIANGLE_STRIP);
                        glTexCoord2f(0.84f, 1f);
                        glColor3f(1f, 1f, 1f);
                        glVertex3f(x * 5f + 5f, 0f, -y * 5f - 3f);
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

                    final ExitTileDescriptor exitTileDescriptor = new ExitTileDescriptor(dlIndex, boundingBox, exitPortalTexture);
                    dynamicTiles.add(exitTileDescriptor);
                    builtMaze[x][y] = exitTileDescriptor;
                }

                if (tile.equals(MazeTile.ROOM)) {
                    builtMaze[x][y] = new FloorTileDescriptor(floorTexture);
                }

                // floor
                floorVertexes.add(new Vertex(x * 5f, 0f, -y * 5f, 0f, 1f));
                floorVertexes.add(new Vertex(x * 5f + 5f, 0f, -y * 5f - 5f, 1f, 0f));
                floorVertexes.add(new Vertex(x * 5f, 0f, -y * 5f - 5f, 0f, 0f));
                floorVertexes.add(new Vertex(x * 5f + 5f, 0f, -y * 5f, 1f, 1f));

                floorVertexIndices.add(floorQuadCounter * 4);
                floorVertexIndices.add(floorQuadCounter * 4 + 1);
                floorVertexIndices.add(floorQuadCounter * 4 + 2);

                floorVertexIndices.add(floorQuadCounter * 4);
                floorVertexIndices.add(floorQuadCounter * 4 + 3);
                floorVertexIndices.add(floorQuadCounter * 4 + 1);

                floorQuadCounter++;

                // ceiling
                wallVertexes.add(new Vertex(x * 5f, 5f, -y * 5f - 5f, 0f, 1f));
                wallVertexes.add(new Vertex(x * 5f + 5f, 5f, -y * 5f, 1f, 0f));
                wallVertexes.add(new Vertex(x * 5f, 5f, -y * 5f, 0f, 0f));
                wallVertexes.add(new Vertex(x * 5f + 5f, 5f, -y * 5f - 5f, 1f, 1f));

                wallVertexIndices.add(wallQuadCounter * 4);
                wallVertexIndices.add(wallQuadCounter * 4 + 1);
                wallVertexIndices.add(wallQuadCounter * 4 + 2);

                wallVertexIndices.add(wallQuadCounter * 4);
                wallVertexIndices.add(wallQuadCounter * 4 + 3);
                wallVertexIndices.add(wallQuadCounter * 4 + 1);

                wallQuadCounter++;

                if (leftTile.equals(MazeTile.VOID)) {
                    wallVertexes.add(new Vertex(x * 5f, 0f, -y * 5f, 0f, 1f));
                    wallVertexes.add(new Vertex(x * 5f, 5f, -y * 5f - 5f, 1f, 0f));
                    wallVertexes.add(new Vertex(x * 5f, 5f, -y * 5f, 0f, 0f));
                    wallVertexes.add(new Vertex(x * 5f, 0f, -y * 5f - 5f, 1f, 1f));

                    wallVertexIndices.add(wallQuadCounter * 4);
                    wallVertexIndices.add(wallQuadCounter * 4 + 1);
                    wallVertexIndices.add(wallQuadCounter * 4 + 2);

                    wallVertexIndices.add(wallQuadCounter * 4);
                    wallVertexIndices.add(wallQuadCounter * 4 + 3);
                    wallVertexIndices.add(wallQuadCounter * 4 + 1);

                    wallQuadCounter++;
                }

                if (rightTile.equals(MazeTile.VOID)) {
                    wallVertexes.add(new Vertex(x * 5f + 5f, 0f, -y * 5f - 5f, 0f, 1f));
                    wallVertexes.add(new Vertex(x * 5f + 5f, 5f, -y * 5f, 1f, 0f));
                    wallVertexes.add(new Vertex(x * 5f + 5f, 5f, -y * 5f - 5f, 0f, 0f));
                    wallVertexes.add(new Vertex(x * 5f + 5f, 0f, -y * 5f, 1f, 1f));

                    wallVertexIndices.add(wallQuadCounter * 4);
                    wallVertexIndices.add(wallQuadCounter * 4 + 1);
                    wallVertexIndices.add(wallQuadCounter * 4 + 2);

                    wallVertexIndices.add(wallQuadCounter * 4);
                    wallVertexIndices.add(wallQuadCounter * 4 + 3);
                    wallVertexIndices.add(wallQuadCounter * 4 + 1);

                    wallQuadCounter++;
                }

                if (topTile.equals(MazeTile.VOID)) {
                    wallVertexes.add(new Vertex(x * 5f + 5f, 0f, -y * 5f, 0f, 1f));
                    wallVertexes.add(new Vertex(x * 5f, 5f, -y * 5f, 1f, 0f));
                    wallVertexes.add(new Vertex(x * 5f + 5f, 5f, -y * 5f, 0f, 0f));
                    wallVertexes.add(new Vertex(x * 5f, 0f, -y * 5f, 1f, 1f));

                    wallVertexIndices.add(wallQuadCounter * 4);
                    wallVertexIndices.add(wallQuadCounter * 4 + 1);
                    wallVertexIndices.add(wallQuadCounter * 4 + 2);

                    wallVertexIndices.add(wallQuadCounter * 4);
                    wallVertexIndices.add(wallQuadCounter * 4 + 3);
                    wallVertexIndices.add(wallQuadCounter * 4 + 1);

                    wallQuadCounter++;
                }

                if (bottomTile.equals(MazeTile.VOID)) {
                    wallVertexes.add(new Vertex(x * 5f, 0f, -y * 5f - 5f, 0f, 1f));
                    wallVertexes.add(new Vertex(x * 5f + 5f, 5f, -y * 5f - 5f, 1f, 0f));
                    wallVertexes.add(new Vertex(x * 5f, 5f, -y * 5f - 5f, 0f, 0f));
                    wallVertexes.add(new Vertex(x * 5f + 5f, 0f, -y * 5f - 5f, 1f, 1f));

                    wallVertexIndices.add(wallQuadCounter * 4);
                    wallVertexIndices.add(wallQuadCounter * 4 + 1);
                    wallVertexIndices.add(wallQuadCounter * 4 + 2);

                    wallVertexIndices.add(wallQuadCounter * 4);
                    wallVertexIndices.add(wallQuadCounter * 4 + 3);
                    wallVertexIndices.add(wallQuadCounter * 4 + 1);

                    wallQuadCounter++;
                }
            }
        }

        // ============================================================
        // Generate VAO, VBO, and EBO buffer objects, and send to GPU
        // ============================================================
        wallVaoId = glGenVertexArrays();
        glBindVertexArray(wallVaoId);

        final float[] wallglvertexes = new float[wallVertexes.size() * 8];
        for (int i = 0; i < wallVertexes.size(); i++) {
            final Vertex v = wallVertexes.get(i);
            wallglvertexes[i * 8] = v.getX();
            wallglvertexes[i * 8 + 1] = v.getY();
            wallglvertexes[i * 8 + 2] = v.getZ();
            wallglvertexes[i * 8 + 3] = v.getTexX();
            wallglvertexes[i * 8 + 4] = v.getTexY();
            wallglvertexes[i * 8 + 5] = v.getR();
            wallglvertexes[i * 8 + 6] = v.getG();
            wallglvertexes[i * 8 + 7] = v.getB();
        }


        // Create a float buffer of vertices
        final FloatBuffer wallVertexBuffer = BufferUtils.createFloatBuffer(wallglvertexes.length);
        wallVertexBuffer.put(wallglvertexes).flip();

        // Create VBO upload the vertex buffer
        final int wallVboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, wallVboId);
        glBufferData(GL_ARRAY_BUFFER, wallVertexBuffer, GL_STATIC_DRAW);

        glVertexPointer(3, GL_FLOAT, 8 * 4, 0);
        glTexCoordPointer(2, GL_FLOAT, 8 * 4, 3 * 4);
        glColorPointer(3, GL_FLOAT, 8 * 4, 5 * 4);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_INDEX_ARRAY);

        glBindVertexArray(0);


        final int[] wallglindexes = new int[wallVertexIndices.size()];
        for (int i = 0; i < wallVertexIndices.size(); i++) {
            wallglindexes[i] = wallVertexIndices.get(i);
        }
        // Create the indices and upload
        final IntBuffer wallIndexBuffer = BufferUtils.createIntBuffer(wallVertexIndices.size());
        wallIndexBuffer.put(wallglindexes).flip();

        wallIboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, wallIboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, wallIndexBuffer, GL_STATIC_DRAW);


        floorVaoId = glGenVertexArrays();
        glBindVertexArray(floorVaoId);

        final float[] floorglvertexes = new float[floorVertexes.size() * 8];
        for (int i = 0; i < floorVertexes.size(); i++) {
            final Vertex v = floorVertexes.get(i);
            floorglvertexes[i * 8] = v.getX();
            floorglvertexes[i * 8 + 1] = v.getY();
            floorglvertexes[i * 8 + 2] = v.getZ();
            floorglvertexes[i * 8 + 3] = v.getTexX();
            floorglvertexes[i * 8 + 4] = v.getTexY();
            floorglvertexes[i * 8 + 5] = v.getR();
            floorglvertexes[i * 8 + 6] = v.getG();
            floorglvertexes[i * 8 + 7] = v.getB();
        }


        // Create a float buffer of vertices
        final FloatBuffer floorVertexBuffer = BufferUtils.createFloatBuffer(floorglvertexes.length);
        floorVertexBuffer.put(floorglvertexes).flip();

        // Create VBO upload the vertex buffer
        final int floorVboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, floorVboId);
        glBufferData(GL_ARRAY_BUFFER, floorVertexBuffer, GL_STATIC_DRAW);

        glVertexPointer(3, GL_FLOAT, 8 * 4, 0);
        glTexCoordPointer(2, GL_FLOAT, 8 * 4, 3 * 4);
        glColorPointer(3, GL_FLOAT, 8 * 4, 5 * 4);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_INDEX_ARRAY);

        glBindVertexArray(0);


        final int[] floorglindexes = new int[floorVertexIndices.size()];
        for (int i = 0; i < floorVertexIndices.size(); i++) {
            floorglindexes[i] = wallVertexIndices.get(i);
        }
        // Create the indices and upload
        final IntBuffer floorIndexBuffer = BufferUtils.createIntBuffer(floorVertexIndices.size());
        floorIndexBuffer.put(floorglindexes).flip();

        floorIboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, floorIboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, floorIndexBuffer, GL_STATIC_DRAW);



        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

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

        uiTextRenderer = TextRendererFactory.createTextRenderer(sceneManager.getGameManager().getWindow(), 20f);
        expressionSolvingTextRenderer = TextRendererFactory.createTextRenderer(sceneManager.getGameManager().getWindow(), 40f);
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
                final WorldTileDescriptor tileDescriptorOfPlayer = getWorldTileDescriptor(player.getPosition());
                if (tileDescriptorOfPlayer instanceof DoorTileDescriptor) {
                    if (!inSolvingExpressionMode) {
                        inSolvingExpressionMode = true;
                        solvingExpression = Expression.generate();
                        enteredExpressionResult = "";
                    }
                }
                if (tileDescriptorOfPlayer instanceof ExitTileDescriptor) {
                    sceneManager.switchScene(SceneManager.SceneIdentifier.ESCAPED_SCENE);
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
                    final WorldTileDescriptor tileDescriptorOfPlayer = getWorldTileDescriptor(player.getPosition());
                    dynamicTiles.remove(tileDescriptorOfPlayer);
                    physicsWorld.getObjects().remove(((DoorTileDescriptor) tileDescriptorOfPlayer).getBoundingBox());

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


    private WorldTileDescriptor getWorldTileDescriptor(Vector3f position) {
        final int positionXInMaze = (int) (position.x / 5);
        final int positionYInMaze = (int) (-position.z / 5);

        return builtMaze[positionXInMaze][positionYInMaze];
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
        glDrawElements(GL_TRIANGLES, wallVertexIndices.size(), GL_UNSIGNED_INT, 0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        // render static floors
        glBindVertexArray(floorVaoId);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, floorIboId);

        floorTexture.bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glDrawElements(GL_TRIANGLES, floorVertexIndices.size(), GL_UNSIGNED_INT, 0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        // render dynamic objects
        // todo: separate dynamic objects??
        dynamicTiles.forEach(tile -> {
            if (!(tile instanceof DoorTileDescriptor)) {
                return;
            }

            tile.getTexture().bind();
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glCallList(((DoorTileDescriptor) tile).getDisplayList());
        });
    }

    /**
     * Renders whole UI base on current game state
     */
    private void renderUi() {
        if (!inInstructionsMode) {
            renderInfoUi();
        }
        if (inInstructionsMode) {
            renderInstructionsUi();
        }
        if (inSolvingExpressionMode) {
            renderExpressionSolvingUi();
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
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

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
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

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

    @Data
    private class Vertex {
        private final float x;
        private final float y;
        private final float z;
        private final float texX;
        private final float texY;
        private final float r = 1f;
        private final float g = 1f;
        private final float b = 1f;
    }

    @Data
    @RequiredArgsConstructor
    private static class WorldTileDescriptor {
        private final OGLTexture2D texture;

        private WorldTileDescriptor() {
            this.texture = null;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static class FloorTileDescriptor extends WorldTileDescriptor {

        public FloorTileDescriptor(OGLTexture2D texture) {
            super(texture);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static class DoorTileDescriptor extends WorldTileDescriptor {
        private final int displayList;
        private final BoundingBox boundingBox;

        public DoorTileDescriptor() {
            throw new RuntimeException();
        }

        public DoorTileDescriptor(int displayList, BoundingBox boundingBox, OGLTexture2D texture) {
            super(texture);
            this.displayList = displayList;
            this.boundingBox = boundingBox;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static class ExitTileDescriptor extends DoorTileDescriptor {

        public ExitTileDescriptor(int displayList, BoundingBox boundingBox, OGLTexture2D texture) {
            super(displayList, boundingBox, texture);
        }
    }
}
