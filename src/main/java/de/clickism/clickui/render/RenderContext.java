package de.clickism.clickui.render;

import de.clickism.clickui.UiElement;
import de.clickism.clickui.UiScreenHandler;
import de.clickism.clickui.layout.Rect;
import de.clickism.clickui.util.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Represents the render context when rendering a UI element.
 *
 * @param graphics The graphics context used for rendering.
 * @param mouseX   The current X position of the mouse cursor.
 * @param mouseY   The current Y position of the mouse cursor.
 * @param delta    The time delta since the last frame.
 * @param screen   The screen handler associated with the current UI screen.
 * @param debug    Whether debug mode is enabled.
 */
public record RenderContext(
    GuiGraphicsExtractor graphics,
    int mouseX,
    int mouseY,
    float delta,
    UiScreenHandler screen,
    boolean debug
) {
    /**
     * Returns the font renderer used for rendering text.
     *
     * @return The font renderer.
     */
    public Font font() {
        return Util.font();
    }

    /**
     * Returns the width of the screen.
     *
     * @return The width of the screen.
     */
    public int screenWidth() {
        return screen.width;
    }

    /**
     * Returns the height of the screen.
     *
     * @return The height of the screen.
     */
    public int screenHeight() {
        return screen.height;
    }

    /**
     * Returns a new RenderContext with the specified debug flag.
     *
     * @param debug Whether to enable debug mode.
     * @return A new RenderContext with the specified debug flag.
     */
    public RenderContext withDebug(boolean debug) {
        return new RenderContext(graphics, mouseX, mouseY, delta, screen, debug);
    }

    public void withScissor(Rect screenBounds, Runnable render) {
        graphics.enableScissor(
            screenBounds.x(),
            screenBounds.y(),
            screenBounds.x() + screenBounds.width(),
            screenBounds.y() + screenBounds.height()
        );
        render.run();
        graphics.disableScissor();
    }
}
