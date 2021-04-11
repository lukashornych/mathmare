package com.lukashornych.mathmare.scene;

import com.lukashornych.mathmare.Camera;
import com.lukashornych.mathmare.InputManager;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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

    private SceneManager sceneManager;

    private final float[] vertexes = {
            // position               // color
            10f, -10f, 0.0f,       1.0f, 1.0f, 1.0f, // Bottom right 0
            -10f,  10f, 0f,       0.0f, 1.0f, 0.0f, // Top left     1
            10f,  10f, 0f ,      1.0f, 0.0f, 1.0f, // Top right    2
            -10f, -10f, 0.0f,       1.0f, 1.0f, 1.0f, // Bottom left  3
            5f, -5f, -5f, 0.5f, 0.5f, 0.5f, // 4
            -5f, 5f, 5f, 0f, 1f, 0f, // 5
            5f, 5f, -5f, 0f, 1f, 0f, // 6
            -5f, -5f, 5f, 0.5f, 0.5f, 0.5f, // 7
    };

    // IMPORTANT: Must be in counter-clockwise order
    private final int[] indexes = {
            /*
                    x        x
                    x        x
             */
            2, 1, 0, // Top right triangle
            0, 1, 3, // bottom left triangle
            6, 5, 4,
            4, 5, 7
    };

    private int vaoId;
    private int iboId;

    private Camera camera;


    @Override
    public void setSceneManager(@NonNull SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @Override
    public void init() {
        // ============================================================
        // Generate VAO, VBO, and EBO buffer objects, and send to GPU
        // ============================================================
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Create a float buffer of vertices
        final FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexes.length);
        vertexBuffer.put(vertexes).flip();

        // Create VBO upload the vertex buffer
        final int vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        glVertexPointer(3, GL_FLOAT, 6 * 4, 0);
        glColorPointer(3, GL_FLOAT, 6 * 4, 3 * 4);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        // Create the indices and upload
        final IntBuffer indexBuffer = BufferUtils.createIntBuffer(indexes.length);
        indexBuffer.put(indexes).flip();

        iboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        glEnable(GL_DEPTH_TEST);

        sceneManager.getGameManager().getInputManager().setMouseMode(InputManager.MouseMode.FREE_MOVING);
        camera = new Camera(
                sceneManager.getGameManager(),
                new Vector3f(0f, 1f, 30f),
                (float) Math.PI / 2f,
                (float) Math.PI / 2f
        );
    }

    @Override
    public void update(float dt) {
        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_W)) {
            camera.moveForward(6 * dt);
        }
        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_S)) {
            camera.moveForward(-6 * dt);
        }
        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_A)) {
            camera.moveSideways(-6 * dt);
        }
        if (sceneManager.getGameManager().getInputManager().isKeyPressed(GLFW_KEY_D)) {
            camera.moveSideways(6 * dt);
        }
        camera.addAzimuth((float) (Math.PI * sceneManager.getGameManager().getInputManager().getDeltaMouseX()) / sceneManager.getGameManager().getWindow().getWidth());
        camera.addZenith((float) (Math.PI * sceneManager.getGameManager().getInputManager().getDeltaMouseY()) / sceneManager.getGameManager().getWindow().getHeight());

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
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnableClientState(GL_INDEX_ARRAY);

        glDrawElements(GL_TRIANGLES, indexes.length, GL_UNSIGNED_INT, 0);

        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_INDEX_ARRAY);
        glBindVertexArray(0);
    }

    @Override
    public void destroy() {
        glDisable(GL_DEPTH_TEST);
    }
}
