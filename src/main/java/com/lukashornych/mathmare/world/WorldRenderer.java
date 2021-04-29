package com.lukashornych.mathmare.world;

import com.lukashornych.mathmare.Camera;
import lombok.Data;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

/**
 * Handles rendering of built {@link World} descriptor.
 *
 * @author Lukáš Hornych 2021
 */
@Data
public class WorldRenderer {

    private final World world;
    private final Camera camera;

    public void renderWorld() {
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

        renderStaticWalls();
        renderStaticFloor();
        renderDynamicObjects();
    }

    private void renderStaticWalls() {
        glBindVertexArray(world.getWallVaoId());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, world.getWallIboId());

        world.getWallTexture().bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glDrawElements(GL_TRIANGLES, world.getWallIndicesCount(), GL_UNSIGNED_INT, 0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void renderStaticFloor() {
        glBindVertexArray(world.getFloorVaoId());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, world.getFloorIboId());

        world.getFloorTexture().bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glDrawElements(GL_TRIANGLES, world.getFloorIndicesCount(), GL_UNSIGNED_INT, 0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void renderDynamicObjects() {
        world.getAllDynamicObjects().forEach(object -> {
            object.getTexture().bind();
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glCallList(object.getDisplayListId());
        });
    }
}
