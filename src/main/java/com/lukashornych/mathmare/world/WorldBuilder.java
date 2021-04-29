package com.lukashornych.mathmare.world;

import com.lukashornych.mathmare.maze.MazeTile;
import com.lukashornych.mathmare.physics.BoundingBox;
import com.lukashornych.mathmare.physics.PhysicsWorld;
import lwjglutils.OGLTexture2D;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.lukashornych.mathmare.maze.MazeGenerator.MAZE_SIZE;
import static com.lukashornych.mathmare.world.World.TILE_WORLD_SIZE;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Builds {@link World} from maze recipe.
 *
 * @author Lukáš Hornych 2021
 */
public class WorldBuilder {

    private int wallQuadCounter;
    private int floorQuadCounter;

    private final List<Vertex> wallVertexes;
    private final List<Integer> wallVertexIndices;

    private final List<Vertex> floorVertexes;
    private final List<Integer> floorVertexIndices;

    private final List<DynamicObject> allDynamicObjects;
    private final DynamicObject[][] dynamicObjectsInWorld;

    private final PhysicsWorld physicsWorld;

    private final OGLTexture2D wallTexture;
    private final OGLTexture2D floorTexture;
    private final OGLTexture2D doorTexture;
    private final OGLTexture2D exitPortalTexture;

    public WorldBuilder() {
        wallQuadCounter = 0;
        floorQuadCounter = 0;

        wallVertexes = new ArrayList<>();
        wallVertexIndices = new ArrayList<>();

        floorVertexes = new ArrayList<>();
        floorVertexIndices = new ArrayList<>();

        allDynamicObjects = new ArrayList<>();
        dynamicObjectsInWorld = new DynamicObject[MAZE_SIZE][MAZE_SIZE];

        physicsWorld = new PhysicsWorld();

        try {
            wallTexture = new OGLTexture2D("assets/texture/bricks.png");
            floorTexture = new OGLTexture2D("assets/texture/pavement.png");
            doorTexture = new OGLTexture2D("assets/texture/locked-doors.png");
            exitPortalTexture = new OGLTexture2D("assets/texture/portal.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public World buildWorld(MazeTile[][] mazeRecipe) {
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

                buildFloor(x, y);
                floorQuadCounter++;

                buildCeiling(x, y);
                wallQuadCounter++;

                if (leftTile.equals(MazeTile.VOID)) {
                    buildLeftWall(x, y);
                    wallQuadCounter++;
                }

                if (rightTile.equals(MazeTile.VOID)) {
                    buildRightWall(x, y);
                    wallQuadCounter++;
                }

                if (frontTile.equals(MazeTile.VOID)) {
                    buildFrontWall(x, y);
                    wallQuadCounter++;
                }

                if (backTile.equals(MazeTile.VOID)) {
                    buildBackWall(x, y);
                    wallQuadCounter++;
                }
            }
        }

        final int wallVaoId = glGenVertexArrays();
        fillVao(wallVaoId, extractVertexes(wallVertexes));
        final int wallIboId = glGenBuffers();
        fillIbo(wallIboId, wallVertexIndices);

        final int floorVaoId = glGenVertexArrays();
        fillVao(floorVaoId, extractVertexes(floorVertexes));
        final int floorIboId = glGenBuffers();
        fillIbo(floorIboId, floorVertexIndices);

        return new World(
                allDynamicObjects,
                dynamicObjectsInWorld,
                wallVaoId,
                wallIboId,
                wallVertexIndices.size(),
                floorVaoId,
                floorIboId,
                floorVertexIndices.size(),
                physicsWorld,
                wallTexture,
                floorTexture,
                doorTexture,
                exitPortalTexture
        );
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

    private void buildBackWall(int mazeX, int mazeY) {
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE, 0f, -mazeY * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(0f, 1f)));
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE + TILE_WORLD_SIZE, TILE_WORLD_SIZE, -mazeY * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(1f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE, TILE_WORLD_SIZE, -mazeY * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(0f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -mazeY * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(1f, 1f)));

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

    private void buildFrontWall(int mazeX, int mazeY) {
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -mazeY * TILE_WORLD_SIZE), new Vector2f(0f, 1f)));
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE, TILE_WORLD_SIZE, -mazeY * TILE_WORLD_SIZE), new Vector2f(1f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE + TILE_WORLD_SIZE, TILE_WORLD_SIZE, -mazeY * TILE_WORLD_SIZE), new Vector2f(0f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE, 0f, -mazeY * TILE_WORLD_SIZE), new Vector2f(1f, 1f)));

        addQuadVertexIndices(wallVertexIndices, wallQuadCounter);
    }

    private void buildRightWall(int mazeX, int mazeY) {
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -mazeY * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(0f, 1f)));
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE + TILE_WORLD_SIZE, TILE_WORLD_SIZE, -mazeY * TILE_WORLD_SIZE), new Vector2f(1f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE + TILE_WORLD_SIZE, TILE_WORLD_SIZE, -mazeY * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(0f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -mazeY * TILE_WORLD_SIZE), new Vector2f(1f, 1f)));

        addQuadVertexIndices(wallVertexIndices, wallQuadCounter);
    }

    private void buildLeftWall(int mazeX, int mazeY) {
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE, 0f, -mazeY * TILE_WORLD_SIZE), new Vector2f(0f, 1f)));
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE, TILE_WORLD_SIZE, -mazeY * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(1f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE, TILE_WORLD_SIZE, -mazeY * TILE_WORLD_SIZE), new Vector2f(0f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE, 0f, -mazeY * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(1f, 1f)));

        addQuadVertexIndices(wallVertexIndices, wallQuadCounter);
    }

    private void buildCeiling(int mazeX, int mazeY) {
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE, TILE_WORLD_SIZE, -mazeY * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(0f, 1f)));
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE + TILE_WORLD_SIZE, TILE_WORLD_SIZE, -mazeY * TILE_WORLD_SIZE), new Vector2f(1f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE, TILE_WORLD_SIZE, -mazeY * TILE_WORLD_SIZE), new Vector2f(0f, 0f)));
        wallVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE + TILE_WORLD_SIZE, TILE_WORLD_SIZE, -mazeY * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(1f, 1f)));

        addQuadVertexIndices(wallVertexIndices, wallQuadCounter);
    }

    private void buildFloor(int mazeX, int mazeY) {
        floorVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE, 0f, -mazeY * TILE_WORLD_SIZE), new Vector2f(0f, 1f)));
        floorVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -mazeY * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(1f, 0f)));
        floorVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE, 0f, -mazeY * TILE_WORLD_SIZE - TILE_WORLD_SIZE), new Vector2f(0f, 0f)));
        floorVertexes.add(new Vertex(new Vector3f(mazeX * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -mazeY * TILE_WORLD_SIZE), new Vector2f(1f, 1f)));

        addQuadVertexIndices(floorVertexIndices, floorQuadCounter);
    }

    private void buildExitPortal(int mazeX, int mazeY) {
        final int dlIndex = glGenLists(1);

        glNewList(dlIndex, GL_COMPILE);

        // front
        glBegin(GL_TRIANGLE_STRIP);
        glTexCoord2f(0f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * TILE_WORLD_SIZE, 0f, -mazeY * TILE_WORLD_SIZE - 2f);
        glTexCoord2f(0.84f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -mazeY * TILE_WORLD_SIZE - 2f);
        glTexCoord2f(0f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * TILE_WORLD_SIZE, TILE_WORLD_SIZE, -mazeY * TILE_WORLD_SIZE - 2f);
        glTexCoord2f(0.84f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * TILE_WORLD_SIZE + TILE_WORLD_SIZE, TILE_WORLD_SIZE, -mazeY * TILE_WORLD_SIZE - 2f);
        glEnd();

        // back
        glBegin(GL_TRIANGLE_STRIP);
        glTexCoord2f(0.84f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * TILE_WORLD_SIZE + TILE_WORLD_SIZE, 0f, -mazeY * TILE_WORLD_SIZE - 3f);
        glTexCoord2f(0f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * 5f, 0f, -mazeY * 5f - 3f);
        glTexCoord2f(0.84f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * 5f + 5f, 5f, -mazeY * 5f - 3f);
        glTexCoord2f(0f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * 5f, 5f, -mazeY * 5f - 3f);
        glEnd();

        // left side
        glBegin(GL_TRIANGLE_STRIP);
        glTexCoord2f(0.84f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * 5f, 0f, -mazeY * 5f - 3f);
        glTexCoord2f(1f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * 5f, 0f, -mazeY * 5f - 2f);
        glTexCoord2f(0.84f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * 5f, 5f, -mazeY * 5f - 3f);
        glTexCoord2f(1f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * 5f, 5f, -mazeY * 5f - 2f);
        glEnd();

        // right side
        glBegin(GL_TRIANGLE_STRIP);
        glTexCoord2f(0.84f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * 5f + 5f, 0f, -mazeY * 5f - 2f);
        glTexCoord2f(1f, 1f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * 5f + 5f, 0f, -mazeY * 5f - 3f);
        glTexCoord2f(0.84f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * 5f + 5f, 5f, -mazeY * 5f - 2f);
        glTexCoord2f(1f, 0f);
        glColor3f(1f, 1f, 1f);
        glVertex3f(mazeX * 5f + 5, 5f, -mazeY * 5f - 3f);
        glEnd();

        glEndList();

        final BoundingBox boundingBox = new BoundingBox(
                mazeX * 5f,
                mazeX * 5f + 5f,
                mazeY * 5f + 2f,
                mazeY * 5f + 3f
        );
        physicsWorld.getObjects().add(boundingBox);

        final DynamicObject exitPortalObject = new DynamicObject(DynamicObjectType.EXIT_PORTAL, dlIndex, boundingBox, exitPortalTexture);
        allDynamicObjects.add(exitPortalObject);
        dynamicObjectsInWorld[mazeX][mazeY] = exitPortalObject;
    }

    private void buildDoor(int mazeX, int mazeY, MazeTile leftTile, MazeTile rightTile, MazeTile topTile, MazeTile bottomTile) {
        final int dlIndex = glGenLists(1);
        BoundingBox boundingBox = null;

        if ((leftTile.equals(MazeTile.VOID)) && (rightTile.equals(MazeTile.VOID))) {
            glNewList(dlIndex, GL_COMPILE);
            glBegin(GL_TRIANGLE_STRIP);

            glTexCoord2f(0f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f, 0f, -mazeY * 5f - 2.4f);
            glTexCoord2f(1f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f + 5f, 0f, -mazeY * 5f - 2.4f);
            glTexCoord2f(0f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f, 5f, -mazeY * 5f - 2.4f);
            glTexCoord2f(1f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f + 5f, 5f, -mazeY * 5f - 2.4f);

            glEnd();

            glBegin(GL_TRIANGLE_STRIP);

            glTexCoord2f(1f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f + 5f, 0f, -mazeY * 5f - 2.6f);
            glTexCoord2f(0f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f, 0f, -mazeY * 5f - 2.6f);
            glTexCoord2f(1f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f + 5f, 5f, -mazeY * 5f - 2.6f);
            glTexCoord2f(0f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f, 5f, -mazeY * 5f - 2.6f);

            glEnd();
            glEndList();

            boundingBox = new BoundingBox(
                    mazeX * 5f,
                    mazeX * 5f + 5f,
                    mazeY * 5f + 2.4f,
                    mazeY * 5f + 2.6f
            );
            physicsWorld.getObjects().add(boundingBox);
        } else if ((topTile.equals(MazeTile.VOID)) && (bottomTile.equals(MazeTile.VOID))) {
            glNewList(dlIndex, GL_COMPILE);
            glBegin(GL_TRIANGLE_STRIP);

            glTexCoord2f(0f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f + 2.4f, 0f, -mazeY * 5f - 5f);
            glTexCoord2f(1f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f + 2.4f, 0f, -mazeY * 5f);
            glTexCoord2f(0f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f + 2.4f, 5f, -mazeY * 5f - 5f);
            glTexCoord2f(1f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f + 2.4f, 5f, -mazeY * 5f);

            glEnd();

            glBegin(GL_TRIANGLE_STRIP);

            glTexCoord2f(1f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f + 2.6f, 0f, -mazeY * 5f);
            glTexCoord2f(0f, 1f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f + 2.6f, 0f, -mazeY * 5f - 5f);
            glTexCoord2f(1f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f + 2.6f, 5f, -mazeY * 5f);
            glTexCoord2f(0f, 0f);
            glColor3f(1f, 1f, 1f);
            glVertex3f(mazeX * 5f + 2.6f, 5f, -mazeY * 5f - 5f);

            glEnd();
            glEndList();

            boundingBox = new BoundingBox(
                    mazeX * 5f + 2.4f,
                    mazeX * 5f + 2.6f,
                    mazeY * 5f,
                    mazeY * 5f + 5f
            );
            physicsWorld.getObjects().add(boundingBox);
        }

        final DynamicObject doorObject = new DynamicObject(DynamicObjectType.DOOR, dlIndex, boundingBox, doorTexture);
        allDynamicObjects.add(doorObject);
        dynamicObjectsInWorld[mazeX][mazeY] = doorObject;
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
}
