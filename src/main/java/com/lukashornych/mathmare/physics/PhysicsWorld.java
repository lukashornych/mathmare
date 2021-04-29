package com.lukashornych.mathmare.physics;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents physics world to calculate collisions between objects
 *
 * @author Lukáš Hornych 2021
 */
@Data
public class PhysicsWorld {

    private final List<BoundingBox> objects = new ArrayList<>();
}
