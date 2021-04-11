package com.lukashornych.mathmare;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Represents GLFW window and gathers basic information about that window such as width and height.
 *
 * @author Lukáš Hornych
 */
@Getter
@ToString
@EqualsAndHashCode
public class Window {

    private long id;
    private int width;
    private int height;

    /**
     * Creates new window for game. The {@link #init()} must be called before using.
     *
     * @param width
     * @param height
     */
    public Window(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Initializes whole GLFW window where the game will be rendered.
     */
    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        // todo: start in fullscreen mode

        id = glfwCreateWindow(width, height, "Mathmare", NULL, NULL);
        if (id == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(id, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    id,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }
        glfwSetWindowSizeCallback(id, this::windowSizeCallback);

        glfwMakeContextCurrent(id);
        glfwSwapInterval(1);

        glfwShowWindow(id);

        GL.createCapabilities();
    }

    /**
     * Destroys current GLFW window and frees-up memory
     */
    public void destroy() {
        glfwFreeCallbacks(id);
        glfwDestroyWindow(id);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * GLFW callback for capturing window size changes
     *
     * @param id
     * @param width
     * @param height
     */
    protected void windowSizeCallback(long id, int width, int height) {
        this.width = width;
        this.height = height;
    }
}
