package com.lukashornych.mathmare.world;

import lombok.Data;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Represents single vertex of world object holding all needed metadata.
 *
 * @author Lukáš Hornych 2021
 */
@Data
public class Vertex {

    private final Vector3f position;
    private final Vector2f textCoords;
    private final Vector3f color = new Vector3f(1f, 1f, 1f);
}
