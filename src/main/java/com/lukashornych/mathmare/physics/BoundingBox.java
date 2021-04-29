package com.lukashornych.mathmare.physics;

import lombok.Data;

/**
 * Defines collision bounding box for single object.
 *
 * @author Lukáš Hornych 2021
 */
@Data
public class BoundingBox {
    private final float minX;
    private final float maxX;
    private final float minY;
    private final float maxY;
    private final boolean enabled = true;
}
