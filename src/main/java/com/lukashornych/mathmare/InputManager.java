package com.lukashornych.mathmare;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.lwjgl.BufferUtils;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Manages all GLFW inputs into suitable form for this game. It manages both keyboard input and mouse input and should
 * be main source of getting current input information as it is holds current input states.
 *
 * @see GameManager
 * @author Lukáš Hornych
 */
@ToString
@EqualsAndHashCode
public class InputManager {

    @Getter
    private final GameManager gameManager;


    protected boolean[] keysPressed = new boolean[349];

    protected double previousMouseX;
    protected double previousMouseY;
    @Getter
    protected double currentMouseX;
    @Getter
    protected double currentMouseY;
    @Getter
    protected double deltaMouseX;
    @Getter
    protected double deltaMouseY;

    protected boolean[] mouseButtonsPressed = new boolean[8];

    /**
     * Creates uninitialized input manager. The {@link #init()} must be called before using.
     *
     * @param gameManager
     */
    public InputManager(@NonNull GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * Initializes manager to usable state. Registers all needed callbacks for inputs and sets mouse mode to
     * {@link MouseMode#INTERACTIVE}.
     */
    public void init() {
        glfwSetKeyCallback(gameManager.getWindow().getId(), this::keyboardCallback);

        glfwSetMouseButtonCallback(gameManager.getWindow().getId(), this::mouseButtonsCallback);
        setMouseMode(MouseMode.INTERACTIVE);
    }

    /**
     * Updates current input data
     */
    public void update() {
        updateMouserPosition();
    }

    /**
     * Checks if the key is pressed at the moment.
     *
     * @param key GLFW key
     * @return true if pressed, false if not pressed or key not found
     */
    public boolean isKeyPressed(int key) {
        if ((key < 0) || (key >= keysPressed.length)) {
            return false;
        }

        return keysPressed[key];
    }

    /**
     * Resets states of all keys
     */
    public void resetKeyStates() {
        keysPressed = new boolean[349];
    }

    /**
     * Checks if the mouse button is pressed at the moment
     *
     * @param mouseButton GLFW mouse button
     * @return true if pressed, false if not pressed or mouse button not found
     */
    public boolean isMouseButtonPressed(int mouseButton) {
        if ((mouseButton < 0) || (mouseButton >= mouseButtonsPressed.length)) {
            return false;
        }

        return mouseButtonsPressed[mouseButton];
    }

    /**
     * Changes current mouse mode
     *
     * @param mouseMode new mouse mode
     */
    public void setMouseMode(@NonNull MouseMode mouseMode) {
        if (mouseMode.equals(MouseMode.INTERACTIVE)) {
            glfwSetInputMode(gameManager.getWindow().getId(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            if (glfwRawMouseMotionSupported()) {
                glfwSetInputMode(gameManager.getWindow().getId(), GLFW_RAW_MOUSE_MOTION, GLFW_FALSE);
            }
        } else if (mouseMode.equals(MouseMode.FREE_MOVING)) {
            glfwSetInputMode(gameManager.getWindow().getId(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            if (glfwRawMouseMotionSupported()) {
                glfwSetInputMode(gameManager.getWindow().getId(), GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
            }
        }
    }

    /**
     * Updates mouse position data with current window mouse position data
     */
    protected void updateMouserPosition() {
        previousMouseX = currentMouseX;
        previousMouseY = currentMouseY;

        final DoubleBuffer currentMouseXBuffer = BufferUtils.createDoubleBuffer(1);
        final DoubleBuffer currentMouseYBuffer = BufferUtils.createDoubleBuffer(1);
        glfwGetCursorPos(gameManager.getWindow().getId(), currentMouseXBuffer, currentMouseYBuffer);
        currentMouseX = currentMouseXBuffer.get(0);
        currentMouseY = currentMouseYBuffer.get(0);

        deltaMouseX = previousMouseX - currentMouseX;
        deltaMouseY = previousMouseY - currentMouseY;
    }

    /**
     * GLFW callback for managing keyboard input
     *
     * @param window
     * @param key
     * @param scancode
     * @param action
     * @param mods
     */
    protected void keyboardCallback(long window, int key, int scancode, int action, int mods) {
        try {
            if (action == GLFW_PRESS) {
                keysPressed[key] = true;
            } else if (action == GLFW_RELEASE) {
                keysPressed[key] = false;
            }
        } catch (Exception e) {
            // unknown key, do nothing
        }
    }

    /**
     * GLFW callback for managing mouse buttons
     *
     * @param window
     * @param button
     * @param action
     * @param mods
     */
    protected void mouseButtonsCallback(long window, int button, int action, int mods) {
        try {
            if (action == GLFW_PRESS) {
                mouseButtonsPressed[button] = true;
            } else if (action == GLFW_RELEASE) {
                mouseButtonsPressed[button] = false;
            }
        } catch (Exception e) {
            // unknown key, do nothing
        }
    }

    /**
     * Mouse mode, changes how mouse and cursor will behave.
     */
    public enum MouseMode {
        /**
         * Shows cursor for navigating through UI
         */
        INTERACTIVE,

        /**
         * Hides cursor for camera handling
         */
        FREE_MOVING
    }
}
