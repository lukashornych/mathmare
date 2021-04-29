package com.lukashornych.mathmare;

import com.lukashornych.mathmare.physics.BoundingBox;
import com.lukashornych.mathmare.physics.PhysicsWorld;
import lombok.Data;
import lombok.NonNull;
import org.joml.Math;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Actual player which reacts to player's handling. It wraps camera.
 *
 * @author Lukáš Hornych 2021
 */
@Data
public class Player {

    private static final int STEP_LENGTH = 10;

    private final GameManager gameManager;

    private final PhysicsWorld physicsWorld;
    private final Camera camera;

    /**
     * Creates and initializes new player to usable state
     *
     * @param gameManager game manager instance
     * @param initialPosition initial position of player in world
     */
    public Player(@NonNull GameManager gameManager, @NonNull PhysicsWorld physicsWorld, @NonNull Vector3f initialPosition) {
        this.gameManager = gameManager;
        this.physicsWorld = physicsWorld;
        this.camera = new Camera(gameManager, initialPosition, 0f, 0f);
    }

    /**
     * Updates current position by player input
     *
     * @param dt delta time
     */
    public void updatePosition(float dt) {
        final InputManager inputManager = getGameManager().getInputManager();

        camera.addAzimuth((float) (Math.PI * inputManager.getDeltaMouseX()) / gameManager.getWindow().getWidth());
        camera.addZenith((float) (Math.PI * inputManager.getDeltaMouseY()) / gameManager.getWindow().getHeight());

        if (inputManager.isKeyPressed(GLFW_KEY_W)) {
            moveForward(STEP_LENGTH * dt);
        }
        if (inputManager.isKeyPressed(GLFW_KEY_S)) {
            moveForward(-STEP_LENGTH * dt);
        }
        if (inputManager.isKeyPressed(GLFW_KEY_A)) {
            moveSideways(-STEP_LENGTH * dt);
        }
        if (inputManager.isKeyPressed(GLFW_KEY_D)) {
            moveSideways(STEP_LENGTH * dt);
        }
    }

    // todo
    public Vector3f getPosition() {
        return camera.getPosition();
    }

    /**
     * Moves camera position in looking direction but keeps Y coordinates.
     *
     * @param length how much move forward (or backward)
     */
    private void moveForward(float length) {
        final Vector3f direction = new Vector3f(Math.sin(camera.getAzimuth()), 0f, Math.cos(camera.getAzimuth()));

        final Vector3f newPos = new Vector3f(camera.getPosition()).sub(direction.mul(length));
        moveToNewPosition(newPos);
    }

    /**
     * Moves camera position in perpendicular direction to looking direction.
     *
     * @param length how much move sideways
     */
    private void moveSideways(float length) {
        final Vector3f direction = new Vector3f(Math.cos(camera.getAzimuth()), 0f, -Math.sin(camera.getAzimuth()));

        final Vector3f newPos = new Vector3f(camera.getPosition()).add(direction.mul(length));
        moveToNewPosition(newPos);
    }

    /**
     * Moves player to new position if not colliding
     *
     * @param newPos desired new position of player
     */
    private void moveToNewPosition(Vector3f newPos) {
        if (!isCollidingWithWorld(newPos)) {
            camera.setPosition(newPos);
        }
    }

    /**
     * Creates bounding box of player from specific position
     *
     * @return new bounding box
     */
    private BoundingBox createPlayerBoundingBox(Vector3f playerPosition) {
        return new BoundingBox(
                playerPosition.x - 0.5f,
                playerPosition.x + 0.5f,
                -playerPosition.z - 0.5f,
                -playerPosition.z + 0.5f
        );
    }

    /**
     * Checks if player at specific position is colliding with some world object
     *
     * @param playerPosition position of player to check
     * @return true if colliding
     */
    private boolean isCollidingWithWorld(Vector3f playerPosition) {
        final BoundingBox playerBoundingBox = createPlayerBoundingBox(playerPosition);

        for (BoundingBox object : physicsWorld.getObjects()) {
            final boolean colliding = (object.getMinX() <= playerBoundingBox.getMaxX() && object.getMaxX() >= playerBoundingBox.getMinX()) &&
                                      (object.getMinY() <= playerBoundingBox.getMaxY() && object.getMaxY() >= playerBoundingBox.getMinY()) &&
                                      object.isEnabled();
            if (colliding) {
                return true;
            }
        }

        return false;
    }
}
