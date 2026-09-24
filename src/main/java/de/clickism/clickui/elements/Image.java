package de.clickism.clickui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import de.clickism.clickui.UiElement;
import de.clickism.clickui.layout.Size;
import de.clickism.clickui.render.RenderContext;
import net.minecraft.resources.Identifier;

/**
 * A UI element that displays an image from a specified texture resource.
 */
public class Image extends UiElement<Image> {
    private Identifier texture;
    private final int width;
    private final int height;

    private boolean keepAspectRatio = false;

    /**
     * Creates a new Image element with the specified texture.
     *
     * @param texture the resource location of the texture to display
     */
    public Image(Identifier texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    /**
     * Sets whether to keep the aspect ratio of the image when growing.
     *
     * @param keepAspectRatio true to keep the aspect ratio, false otherwise
     * @return this Image element for method chaining
     */
    public Image keepAspectRatio(boolean keepAspectRatio) {
        this.keepAspectRatio = keepAspectRatio;
        return this;
    }

    /**
     * Sets the texture of the image.
     *
     * @param texture the new texture resource location
     * @return this Image element for method chaining
     */
    public Image texture(Identifier texture) {
        this.texture = texture;
        invalidateLayout();
        return this;
    }

    @Override
    public Size intrinsicSize() {
        if (texture == null) {
            return Size.ZERO;
        }
        if (keepAspectRatio) {
            // Get current width from bounds
            var currentWidth = bounds().width() - padding().horizontal();
            // Calculate height based on aspect ratio
            var aspectRatio = (double) height / width;
            var calculatedHeight = (int) (currentWidth * aspectRatio);
            // Check that height is less than maxHeight
            int maxHeight = effectiveMaxSize().height();
            if (calculatedHeight > maxHeight) {
                calculatedHeight = maxHeight;
                // Recalculate width based on new height
                currentWidth = (int) (calculatedHeight / aspectRatio);
            }
            return new Size(currentWidth, calculatedHeight);
        }
        return new Size(width, height);
    }

    @Override
    public Size defaultMinSize() {
        // Don't shrink by default
        return intrinsicSize();
    }

    @Override
    public void render(RenderContext context) {
        if (texture == null) return;
        var graphics = context.graphics();
        var bounds = bounds();
        // Override render to enable blending for semi-transparent textures
        //? if < 26.1 {
        /*RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        *///?}
        // Render image
        graphics.blit(
            texture,
            bounds.x(),
            bounds.y(),
            0, 0,
            bounds.width(), bounds.height(),
            bounds.width(), bounds.height()
        );
        // Revert blending
        //? if < 26.1
        //RenderSystem.disableBlend();
    }
}
