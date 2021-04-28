package com.lukashornych.mathmare.maze;

import lombok.Data;
import org.joml.Vector2i;

/**
 * Holds generated maze as well some metadata about the maze
 *
 * @author Lukáš Hornych 2021
 */
@Data
public class MazeDescriptor {

    private final MazeTile[][] maze;
    private final Vector2i startingPosition;
    private final int roomsCount;
}
