package com.lukashornych.mathmare.scene;

import com.lukashornych.mathmare.Camera;
import com.lukashornych.mathmare.InputManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.joml.Math;
import org.joml.Random;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Main scene of game, representing maze in which the player play.
 *
 * @author Lukáš Hornych
 */
@ToString
@EqualsAndHashCode
public class MazeScene implements Scene {

    private final Random rnd = new Random();

    private SceneManager sceneManager;

    /*
    todo: generate colliding boxes when generating walls (be careful about corners) and the aabb should be possible with
    impulse resolution if player will be controller by velocity which maybe should
     */

    private final int MAZE_SIZE = 6;

    private final int[][] mazeRecipe = new int[][]{
            {0, 0, 0, 0, 0, 0},
            {0, 1, 1, 0, 0, 0},
            {0, 0, 1, 2, 1, 0},
            {0, 0, 1, 0, 2, 0},
            {0, 0, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0}
    };

    private final WorldTileDescriptor[][] builtMaze = new WorldTileDescriptor[MAZE_SIZE][MAZE_SIZE];

    private final List<Vertex> staticObjectsVertexes = new ArrayList<>();
    private final List<Integer> staticObjectsIndices = new ArrayList<>();

    private final List<BoundingBox> boundingBoxes = new ArrayList<>();

    private int vaoId;
    private int iboId;

    private final List<Integer> dynamicObjectsDisplayLists = new ArrayList<>();

    private Camera camera;


    @Override
    public void setSceneManager(@NonNull SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @Override
    public void init() {
        // generate walls
        // tile size 5

        int quadCounter = 0;
        for (int x = 0; x < MAZE_SIZE; x++) {
            for (int y = 0; y < MAZE_SIZE; y++) {
                final int tile = mazeRecipe[x][y];

                // create only bounding box for wall tile
                if (tile == 0) {
                    boundingBoxes.add(new BoundingBox(
                            x * 5f,
                            x * 5f + 5f,
                            y * 5f,
                            y * 5f + 5f
                    ));
                    continue;
                }

                final int leftTile = mazeRecipe[x - 1][y];
                final int rightTile = mazeRecipe[x + 1][y];
                final int topTile = mazeRecipe[x][y - 1];
                final int bottomTile = mazeRecipe[x][y + 1];

                // create door
                // todo
                if (tile == 2) {
                    final int dlIndex = glGenLists(1);
                    BoundingBox boundingBox = null;

                    if ((leftTile == 0) && (rightTile == 0)) {
                        glNewList(dlIndex, GL_COMPILE);
                            glBegin(GL_TRIANGLE_STRIP);

                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f, 0f, -y * 5f - 2.4f);
                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f + 5f, 0f, -y * 5f - 2.4f);
                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f, 5f, -y * 5f - 2.4f);
                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f + 5f, 5f, -y * 5f - 2.4f);

                            glEnd();

                            glBegin(GL_TRIANGLE_STRIP);

                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f + 5f, 0f, -y * 5f - 2.6f);
                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f, 0f, -y * 5f - 2.6f);
                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f + 5f, 5f, -y * 5f - 2.6f);
                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f, 5f, -y * 5f - 2.6f);

                            glEnd();
                        glEndList();
                        dynamicObjectsDisplayLists.add(dlIndex);

                        boundingBox = new BoundingBox(
                                x * 5f,
                                x * 5f + 5f,
                                y * 5f + 2.4f,
                                y * 5f + 2.6f
                        );
                        boundingBoxes.add(boundingBox);
                    } else if ((topTile == 0) && (bottomTile == 0)) {
                        glNewList(dlIndex, GL_COMPILE);
                            glBegin(GL_TRIANGLE_STRIP);

                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f + 2.4f, 0f, -y * 5f - 5f);
                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f + 2.4f, 0f, -y * 5f);
                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f + 2.4f, 5f, -y * 5f - 5f);
                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f + 2.4f, 5f, -y * 5f);

                            glEnd();

                            glBegin(GL_TRIANGLE_STRIP);

                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f + 2.6f, 0f, -y * 5f);
                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f + 2.6f, 0f, -y * 5f - 5f);
                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f + 2.6f, 5f, -y * 5f);
                            glColor3f(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
                            glVertex3f(x * 5f + 2.6f, 5f, -y * 5f - 5f);

                            glEnd();
                        glEndList();
                        dynamicObjectsDisplayLists.add(dlIndex);

                        boundingBox = new BoundingBox(
                                x * 5f + 2.4f,
                                x * 5f + 2.6f,
                                y * 5f,
                                y * 5f + 5f
                        );
                        boundingBoxes.add(boundingBox);
                    }

                    builtMaze[x][y] = new DoorTileDescriptor(dlIndex, boundingBox);
                }

                if (tile == 1) {
                    builtMaze[x][y] = new FloorTileDescriptor();
                }

                // floor
                staticObjectsVertexes.add(new Vertex(x * 5f, 0f, -y * 5f));
                staticObjectsVertexes.add(new Vertex(x * 5f + 5f, 0f, -y * 5f - 5f));
                staticObjectsVertexes.add(new Vertex(x * 5f, 0f, -y * 5f - 5f));
                staticObjectsVertexes.add(new Vertex(x * 5f + 5f, 0f, -y * 5f));

                staticObjectsIndices.add(quadCounter * 4);
                staticObjectsIndices.add(quadCounter * 4 + 1);
                staticObjectsIndices.add(quadCounter * 4 + 2);

                staticObjectsIndices.add(quadCounter * 4);
                staticObjectsIndices.add(quadCounter * 4 + 3);
                staticObjectsIndices.add(quadCounter * 4 + 1);

                quadCounter++;

                // ceiling
                staticObjectsVertexes.add(new Vertex(x * 5f, 5f, -y * 5f - 5f));
                staticObjectsVertexes.add(new Vertex(x * 5f + 5f, 5f, -y * 5f));
                staticObjectsVertexes.add(new Vertex(x * 5f, 5f, -y * 5f));
                staticObjectsVertexes.add(new Vertex(x * 5f + 5f, 5f, -y * 5f - 5f));

                staticObjectsIndices.add(quadCounter * 4);
                staticObjectsIndices.add(quadCounter * 4 + 1);
                staticObjectsIndices.add(quadCounter * 4 + 2);

                staticObjectsIndices.add(quadCounter * 4);
                staticObjectsIndices.add(quadCounter * 4 + 3);
                staticObjectsIndices.add(quadCounter * 4 + 1);

                quadCounter++;

                if (leftTile == 0) {
                    staticObjectsVertexes.add(new Vertex(x * 5f, 0f, -y * 5f));
                    staticObjectsVertexes.add(new Vertex(x * 5f, 5f, -y * 5f - 5f));
                    staticObjectsVertexes.add(new Vertex(x * 5f, 5f, -y * 5f));
                    staticObjectsVertexes.add(new Vertex(x * 5f, 0f, -y * 5f - 5f));

                    staticObjectsIndices.add(quadCounter * 4);
                    staticObjectsIndices.add(quadCounter * 4 + 1);
                    staticObjectsIndices.add(quadCounter * 4 + 2);

                    staticObjectsIndices.add(quadCounter * 4);
                    staticObjectsIndices.add(quadCounter * 4 + 3);
                    staticObjectsIndices.add(quadCounter * 4 + 1);

                    quadCounter++;
                }

                if (rightTile == 0) {
                    staticObjectsVertexes.add(new Vertex(x * 5f + 5f, 0f, -y * 5f - 5f));
                    staticObjectsVertexes.add(new Vertex(x * 5f + 5f, 5f, -y * 5f));
                    staticObjectsVertexes.add(new Vertex(x * 5f + 5f, 5f, -y * 5f - 5f));
                    staticObjectsVertexes.add(new Vertex(x * 5f + 5f, 0f, -y * 5f));

                    staticObjectsIndices.add(quadCounter * 4);
                    staticObjectsIndices.add(quadCounter * 4 + 1);
                    staticObjectsIndices.add(quadCounter * 4 + 2);

                    staticObjectsIndices.add(quadCounter * 4);
                    staticObjectsIndices.add(quadCounter * 4 + 3);
                    staticObjectsIndices.add(quadCounter * 4 + 1);

                    quadCounter++;
                }

                if (topTile == 0) {
                    staticObjectsVertexes.add(new Vertex(x * 5f + 5f, 0f, -y * 5f));
                    staticObjectsVertexes.add(new Vertex(x * 5f, 5f, -y * 5f));
                    staticObjectsVertexes.add(new Vertex(x * 5f + 5f, 5f, -y * 5f));
                    staticObjectsVertexes.add(new Vertex(x * 5f, 0f, -y * 5f));

                    staticObjectsIndices.add(quadCounter * 4);
                    staticObjectsIndices.add(quadCounter * 4 + 1);
                    staticObjectsIndices.add(quadCounter * 4 + 2);

                    staticObjectsIndices.add(quadCounter * 4);
                    staticObjectsIndices.add(quadCounter * 4 + 3);
                    staticObjectsIndices.add(quadCounter * 4 + 1);

                    quadCounter++;
                }

                if (bottomTile == 0) {
                    staticObjectsVertexes.add(new Vertex(x * 5f, 0f, -y * 5f - 5f));
                    staticObjectsVertexes.add(new Vertex(x * 5f + 5f, 5f, -y * 5f - 5f));
                    staticObjectsVertexes.add(new Vertex(x * 5f, 5f, -y * 5f - 5f));
                    staticObjectsVertexes.add(new Vertex(x * 5f + 5f, 0f, -y * 5f - 5f));

                    staticObjectsIndices.add(quadCounter * 4);
                    staticObjectsIndices.add(quadCounter * 4 + 1);
                    staticObjectsIndices.add(quadCounter * 4 + 2);

                    staticObjectsIndices.add(quadCounter * 4);
                    staticObjectsIndices.add(quadCounter * 4 + 3);
                    staticObjectsIndices.add(quadCounter * 4 + 1);

                    quadCounter++;
                }
            }
        }

        // ============================================================
        // Generate VAO, VBO, and EBO buffer objects, and send to GPU
        // ============================================================
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        final float[] glvertexes = new float[staticObjectsVertexes.size() * 6];
        for (int i = 0; i < staticObjectsVertexes.size(); i++) {
            final Vertex v = staticObjectsVertexes.get(i);
            glvertexes[i * 6] = v.getX();
            glvertexes[i * 6 + 1] = v.getY();
            glvertexes[i * 6 + 2] = v.getZ();
            glvertexes[i * 6 + 3] = v.getR();
            glvertexes[i * 6 + 4] = v.getG();
            glvertexes[i * 6 + 5] = v.getB();
        }


        // Create a float buffer of vertices
        final FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(glvertexes.length);
        vertexBuffer.put(glvertexes).flip();

        // Create VBO upload the vertex buffer
        final int vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        glVertexPointer(3, GL_FLOAT, 6 * 4, 0);
        glColorPointer(3, GL_FLOAT, 6 * 4, 3 * 4);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnableClientState(GL_INDEX_ARRAY);

        glBindVertexArray(0);


        final int[] glindexes = new int[staticObjectsIndices.size()];
        for (int i = 0; i < staticObjectsIndices.size(); i++) {
            glindexes[i] = staticObjectsIndices.get(i);
        }
        // Create the indices and upload
        final IntBuffer indexBuffer = BufferUtils.createIntBuffer(staticObjectsIndices.size());
        indexBuffer.put(glindexes).flip();

        iboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        sceneManager.getGameManager().getInputManager().setMouseMode(InputManager.MouseMode.FREE_MOVING);
        camera = new Camera(
                sceneManager.getGameManager(),
                new Vector3f(7.5f, 2.5f, -7.5f),
                (float) Math.PI / 2f,
                (float) Math.PI / 2f
        );
    }

    @Override
    public void update(float dt) {
        camera.addAzimuth((float) (Math.PI * sceneManager.getGameManager().getInputManager().getDeltaMouseX()) / sceneManager.getGameManager().getWindow().getWidth());
        camera.addZenith((float) (Math.PI * sceneManager.getGameManager().getInputManager().getDeltaMouseY()) / sceneManager.getGameManager().getWindow().getHeight());

        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_W)) {
            final Vector3f direction = new Vector3f(Math.sin(camera.getAzimuth()), 0f, Math.cos(camera.getAzimuth()));
            final Vector3f newPos = new Vector3f(camera.getPosition()).sub(direction.mul(8 * dt));

                if (!isCollidingWithWorld(createPlayerBoundingBox(newPos))) {
                camera.setPosition(newPos);
            }
//            camera.moveForward(6 * dt);
        }
        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_S)) {
            final Vector3f direction = new Vector3f(Math.sin(camera.getAzimuth()), 0f, Math.cos(camera.getAzimuth()));
            final Vector3f newPos = new Vector3f(camera.getPosition()).add(direction.mul(8 * dt));
            if (!isCollidingWithWorld(createPlayerBoundingBox(newPos))) {
                camera.setPosition(newPos);
            }
//            camera.moveForward(-6 * dt);
        }
        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_A)) {
            final Vector3f direction = new Vector3f(Math.cos(camera.getAzimuth()), 0f, -Math.sin(camera.getAzimuth()));
            final Vector3f newPos = new Vector3f(camera.getPosition()).sub(direction.mul(8 * dt));
            if (!isCollidingWithWorld(createPlayerBoundingBox(newPos))) {
                camera.setPosition(newPos);
            }
//            camera.moveSideways(-6 * dt);
        }
        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_D)) {
            final Vector3f direction = new Vector3f(Math.cos(camera.getAzimuth()), 0f, -Math.sin(camera.getAzimuth()));
            final Vector3f newPos = new Vector3f(camera.getPosition()).add(direction.mul(8 * dt));
            if (!isCollidingWithWorld(createPlayerBoundingBox(newPos))) {
                camera.setPosition(newPos);
            }
//            camera.moveSideways(6 * dt);
        }

        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_F)) {
            final WorldTileDescriptor tileDescriptorOfPlayer = getWorldTileDescriptor(camera.getPosition());
            if (tileDescriptorOfPlayer instanceof DoorTileDescriptor) {
                dynamicObjectsDisplayLists.remove((Object) ((DoorTileDescriptor) tileDescriptorOfPlayer).getDisplayList());
                boundingBoxes.remove(((DoorTileDescriptor) tileDescriptorOfPlayer).getBoundingBox());
            }
        }

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        final FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);
        camera.getProjection().get(projectionBuffer);
        glMultMatrixf(projectionBuffer);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        final FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
        camera.getView().get(viewBuffer);
        glMultMatrixf(viewBuffer);

        glBindVertexArray(vaoId);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboId);

        glDrawElements(GL_TRIANGLES, staticObjectsIndices.size(), GL_UNSIGNED_INT, 0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        dynamicObjectsDisplayLists.forEach(GL11::glCallList);
    }

    @Override
    public void destroy() {
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
    }

    private BoundingBox createPlayerBoundingBox(Vector3f pos) {
        return new BoundingBox(
                pos.x - 0.5f,
                pos.x + 0.5f,
                -pos.z - 0.5f,
                -pos.z + 0.5f
        );
    }

    private boolean isCollidingWithWorld(BoundingBox boundingBox) {
        for (BoundingBox worldBoundingBox : boundingBoxes) {
            final boolean colliding = (worldBoundingBox.getMinX() <= boundingBox.getMaxX() && worldBoundingBox.getMaxX() >= boundingBox.getMinX()) &&
                                      (worldBoundingBox.getMinY() <= boundingBox.getMaxY() && worldBoundingBox.getMaxY() >= boundingBox.getMinY()) &&
                                      worldBoundingBox.isEnabled();
            if (colliding) {
                return true;
            }
        }

        return false;
    }

    private WorldTileDescriptor getWorldTileDescriptor(Vector3f position) {
        final int positionXInMaze = (int) (position.x / 5);
        final int positionYInMaze = (int) (-position.z / 5);

        return builtMaze[positionXInMaze][positionYInMaze];
    }

    @Data
    private class Vertex {
        private final float x;
        private final float y;
        private final float z;
        private final float r = rnd.nextFloat();
        private final float g = rnd.nextFloat();
        private final float b = rnd.nextFloat();
    }

    @Data
    private static class BoundingBox {
        private final float minX;
        private final float maxX;
        private final float minY;
        private final float maxY;
        private final boolean enabled = true;
    }

    @Data
    private static class WorldTileDescriptor {
    }

    private static class FloorTileDescriptor extends WorldTileDescriptor {

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static class DoorTileDescriptor extends WorldTileDescriptor {
        private final int displayList;
        private final BoundingBox boundingBox;
    }
}
