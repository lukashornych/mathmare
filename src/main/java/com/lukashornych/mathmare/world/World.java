package com.lukashornych.mathmare.world;

import com.lukashornych.mathmare.physics.PhysicsWorld;
import lombok.Data;
import lwjglutils.OGLTexture2D;
import org.joml.Vector3f;

import java.util.List;

/**
 * Dungeon world descriptor. Holds static and dynamic objects and its metadata.
 *
 * @author Lukáš Hornych 2021
 */
@Data
public class World {

    public static final float TILE_WORLD_SIZE = 5f;

    private final List<DynamicObject> allDynamicObjects;
    private final DynamicObject[][] dynamicObjectsInWorld;

    private final int wallVaoId;
    private final int wallIboId;
    private final int wallIndicesCount;

    private final int floorVaoId;
    private final int floorIboId;
    private final int floorIndicesCount;

    private final PhysicsWorld physicsWorld;

    private final OGLTexture2D wallTexture;
    private final OGLTexture2D floorTexture;
    private final OGLTexture2D doorTexture;
    private final OGLTexture2D exitPortalTexture;

    public DynamicObject getDynamicObject(Vector3f positionInWorld) {
        final int positionXInMaze = (int) (positionInWorld.x / 5);
        final int positionYInMaze = (int) (-positionInWorld.z / 5);

        return dynamicObjectsInWorld[positionXInMaze][positionYInMaze];
    }
}
