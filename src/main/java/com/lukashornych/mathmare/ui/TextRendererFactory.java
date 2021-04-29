package com.lukashornych.mathmare.ui;

import com.lukashornych.mathmare.Window;
import lombok.NonNull;
import lwjglutils.OGLTextRenderer;

import java.awt.*;

/**
 * Handles easy creating of text renderers
 *
 * @author Lukáš Hornych 2021
 */
public class TextRendererFactory {

    private static Font baseFont = null;

    /**
     * Returns cached font. If missing loads it.
     *
     * @return base font instance
     */
    private static Font getBaseFont() {
        if (baseFont == null) {
            try {
                baseFont = Font.createFont(
                        Font.TRUETYPE_FONT,
                        TextRendererFactory.class.getResourceAsStream("/assets/font/pixel-digivolve.otf")
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return baseFont;
    }

    /**
     * Creates new configured text renderer with specified size
     *
     * @param window game window
     * @param size size of font
     * @return new renderer
     */
    public static OGLTextRenderer createTextRenderer(@NonNull Window window, float size) {
        return new OGLTextRenderer(window.getWidth(), window.getHeight(), getBaseFont().deriveFont(Font.PLAIN, size));
    }
}
