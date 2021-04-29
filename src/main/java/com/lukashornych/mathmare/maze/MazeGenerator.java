package com.lukashornych.mathmare.maze;

import org.joml.Vector2i;

import java.util.Random;

/**
 * Generates random maze recipes to generate actual playable dungeon
 *
 * @author Lukáš Hornych 2021
 */
public class MazeGenerator {

    public static final int ROOM_PLANE_SIZE = 7;
    public static final int ROOM_SIZE = 3;
    public static final int MAZE_SIZE = (ROOM_PLANE_SIZE * ROOM_SIZE) + (ROOM_PLANE_SIZE + 1);

    private static final Random RANDOM = new Random();

    /**
     * Generates random maze constructed of rooms and connections between them
     *
     * @return maze with additional metadata
     */
    public static MazeDescriptor generateMaze() {
        final int[][] roomPlan = new int[ROOM_PLANE_SIZE][ROOM_PLANE_SIZE];
        final int roomsCount = generateRoomPlan(roomPlan, ROOM_PLANE_SIZE / 2, ROOM_PLANE_SIZE / 2);
        placeExitPortal(roomPlan);

        final MazeTile[][] maze = new MazeTile[MAZE_SIZE][MAZE_SIZE];
        generateRooms(roomPlan, maze);
        generateRoomConnections(maze);

        return new MazeDescriptor(maze, new Vector2i(MAZE_SIZE / 2, MAZE_SIZE / 2), roomsCount);
    }

    /**
     * Randomly generates room plan (where rooms will be logically placed in maze)
     *
     * @param roomPlan where the room plan will be generated to
     * @param x starting x in room plan
     * @param y starting y in room plan
     * @return number of rooms generated
     */
    private static int generateRoomPlan(int[][] roomPlan, int x, int y) {
        if ((x < 0) || (y < 0) || (x > ROOM_PLANE_SIZE - 1) || (y > ROOM_PLANE_SIZE - 1) || (roomPlan[x][y] != 0)) {
            return 0;
        }
        roomPlan[x][y] = 1;
        int roomsCount = 1;

        final byte directions = (byte) (RANDOM.nextInt(11) + 1);

        final boolean canGoTop = ((directions & 0b1000) >> 3) == 1;
        if (canGoTop) {
            roomsCount += generateRoomPlan(roomPlan, x, y - 1);
        }

        final boolean canGoRight = ((directions & 0b0100) >> 2) == 1;
        if (canGoRight) {
            roomsCount += generateRoomPlan(roomPlan, x + 1, y);
        }

        final boolean canGoBottom = ((directions & 0b0010) >> 1) == 1;
        if (canGoBottom) {
            roomsCount += generateRoomPlan(roomPlan, x, y + 1);
        }

        final boolean canGoLeft = (directions & 0b0001) == 1;
        if (canGoLeft) {
            roomsCount += generateRoomPlan(roomPlan, x - 1, y);
        }

        return roomsCount;
    }

    /**
     * Places exit portal to one of the rooms in room plan
     *
     * @param roomPlan generated room plan
     */
    private static void placeExitPortal(int[][] roomPlan) {
        int portalX = 0;
        int portalY = 0;
        int furthestRoom = 0;
        for (int x = 0; x < ROOM_PLANE_SIZE; x++) {
            for (int y = 0; y < ROOM_PLANE_SIZE; y++) {
                if ((roomPlan[x][y] == 0) || ((x == ROOM_PLANE_SIZE / 2) && (y == ROOM_PLANE_SIZE / 2))) {
                    continue;
                }

                final int lengthX = Math.abs((ROOM_PLANE_SIZE / 2) - x);
                final int lengthY = Math.abs((ROOM_PLANE_SIZE / 2) - y);
                final int length = lengthX + lengthY;
                if (length > furthestRoom) {
                    furthestRoom = length;
                    portalX = x;
                    portalY = y;
                }
            }
        }
        roomPlan[portalX][portalY] = 2;
    }

    /**
     * Generates rooms in maze by room plan
     *
     * @param roomPlan generated room plan
     * @param maze where the maze will be generated to
     */
    private static void generateRooms(int[][] roomPlan, MazeTile[][] maze) {
        for (int mazeX = 0; mazeX < MAZE_SIZE; mazeX++) {
            for (int mazeY = 0; mazeY < MAZE_SIZE; mazeY++) {
                if (((mazeX % (ROOM_SIZE + 1)) == 0) || ((mazeY % (ROOM_SIZE + 1)) == 0)) {
                    maze[mazeX][mazeY] = MazeTile.VOID;
                    continue;
                }

                final int roomsX = mazeX / (ROOM_SIZE + 1);
                final int roomsY = mazeY / (ROOM_SIZE + 1);
                final int room = roomPlan[roomsX][roomsY];
                if (room == 0) {
                    maze[mazeX][mazeY] = MazeTile.VOID;
                    continue;
                }
                if ((room == 2) && (mazeX % 2 == 0) && (mazeY % 2 == 0)) {
                    maze[mazeX][mazeY] = MazeTile.EXIT_PORTAL;
                    continue;
                }

                maze[mazeX][mazeY] = MazeTile.ROOM;
            }
        }
    }

    /**
     * Generates connections (doors or corridors) between rooms in maze
     *
     * @param maze generated maze
     */
    private static void generateRoomConnections(MazeTile[][] maze) {
        for (int mazeX = 0; mazeX < MAZE_SIZE; mazeX += 2) {
            for (int mazeY = 0; mazeY < MAZE_SIZE; mazeY += 2) {
                if ((mazeX == 0) || (mazeY == 0) || (mazeX == MAZE_SIZE - 1) || (mazeY == MAZE_SIZE - 1)) {
                    continue;
                }

                final MazeTile mt = maze[mazeX][mazeY];
                if (mt != MazeTile.VOID) {
                    continue;
                }

                final boolean leftRoom = maze[mazeX - 1][mazeY].equals(MazeTile.ROOM);
                final boolean rightRoom = maze[mazeX + 1][mazeY].equals(MazeTile.ROOM);
                final boolean topRoom = maze[mazeX][mazeY - 1].equals(MazeTile.ROOM);
                final boolean bottomRoom = maze[mazeX][mazeY + 1].equals(MazeTile.ROOM);
                if ((leftRoom && rightRoom) || (topRoom && bottomRoom)) {
                    final boolean connectionIsDoor = RANDOM.nextInt(10) > 2;
                    if (connectionIsDoor) {
                        maze[mazeX][mazeY] = MazeTile.DOOR;
                        continue;
                    }

                    maze[mazeX][mazeY] = MazeTile.CORRIDOR;
                }
            }
        }
    }
}
