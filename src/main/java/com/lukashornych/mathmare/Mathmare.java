package com.lukashornych.mathmare;

/**
 * Application which start the whole game.
 *
 * @author Lukáš Hornych
 */
public class Mathmare {


    public static void main(String... args) {
        new Mathmare().run();
    }

    public void run() {
        final GameManager gameManager = new GameManager();
        gameManager.init();
        gameManager.run();
        gameManager.destroy();
    }
}
