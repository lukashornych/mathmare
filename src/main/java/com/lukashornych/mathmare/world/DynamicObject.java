package com.lukashornych.mathmare.world;

import com.lukashornych.mathmare.physics.BoundingBox;
import lombok.Data;
import lwjglutils.OGLTexture2D;

/**
 * Descriptor of world object that is dynamically changed.
 *
 * @author Lukáš Hornych 2021
 */
@Data
public class DynamicObject {

    private final DynamicObjectType type;
    private final int displayListId;
    private final BoundingBox boundingBox;
    private final OGLTexture2D texture;
}
