package de.clickism.clickui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import de.clickism.clickui.UiColor;
import de.clickism.clickui.UiElement;
import de.clickism.clickui.layout.Padding;
import de.clickism.clickui.layout.Size;
import de.clickism.clickui.render.RenderContext;
import de.clickism.clickui.render.ScaledTextRenderer;
import de.clickism.clickui.style.Border;
import de.clickism.clickui.style.StyleProperty;
import de.clickism.clickui.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

//? if < 1.21
//import static net.minecraft.client.gui.components.AbstractWidget.WIDGETS_LOCATION;
//? if >= 1.21
import net.minecraft.client.gui.components.WidgetSprites;

/**
 * A simple UI element that can be clicked and displays a label.
 */
public class Button extends UiElement<Button> {
    //? if >= 1.21
    private static final WidgetSprites SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("widget/button"), ResourceLocation.withDefaultNamespace("widget/button_disabled"), ResourceLocation.withDefaultNamespace("widget/button_highlighted"));
    private static final int DEFAULT_HEIGHT = 20;
    private static final Padding DEFAULT_PADDING = Padding.create(4, 8);

    /**
     * The label to display on the button.
     */
    private Component label;

    /**
     * Whether to render the button's background. If false, the default background won't be rendered
     */
    private boolean defaultBackground = true;
    private float displayOffsetX = 0;

    /**
     * Creates a new Button element with the specified label.
     *
     * @param label the label to display on the button
     */
    public Button(Component label) {
        this.label = label;
        // Adjust default padding
        // TODO: Fix default pading? why these values
        this.padding(DEFAULT_PADDING);
        this.style(style()
            .whenHovered(style()
                .borderColor(UiColor.WHITE)
                .borderPosition(Border.Position.CENTER)));
        // Play down sound on click
        this.onClick(event -> Util.playDownSound());
    }

    /**
     * Creates a new Button element with the specified label as a String.
     *
     * @param label the label to display on the button
     */
    public Button(String label) {
        this(Component.literal(label));
    }

    /**
     * Sets the label of the button.
     *
     * @param label the label to set
     */
    public void label(Component label) {
        this.label = label;
        this.invalidateLayout();
    }

    /**
     * Sets the button's overlay color.
     *
     * @param color the color to set as the overlay, with 0.4f alpha for semi-transparency
     * @return this button instance for chaining
     */
    public Button buttonColor(UiColor color) {
        this.style(style()
            .overlayColor(color.alpha(0.4f)));
        return this;
    }

    /**
     * Sets whether to render the button's default background.
     *
     * @param defaultBackground true to render the default background, false otherwise
     * @return this button instance for chaining
     */
    public Button defaultBackground(boolean defaultBackground) {
        this.defaultBackground = defaultBackground;
        return this;
    }

    @Override
    public Size intrinsicSize() {
        var height = DEFAULT_HEIGHT - DEFAULT_PADDING.vertical();
        var fontScale = resolvedStyle().get(StyleProperty.FONT_SCALE);
        var width = (int) (Util.font().width(label) * fontScale);
        return new Size(width, height);
    }

    @Override
    public void render(RenderContext context) {
        var bounds = this.bounds();
        // Render default background
        if (defaultBackground) {
            renderBackground(context);
        }
        scrollTextIfNeeded(context);
        // Transform by displayOffsetX for scrolling effect
        var graphics = context.graphics();
        graphics.pose().pushPose();
        var renderBounds = this.renderBounds();
        var scissorPadding = 2;
        graphics.enableScissor(
            renderBounds.x() + scissorPadding,
            renderBounds.y(),
            renderBounds.x() + renderBounds.width() - scissorPadding,
            renderBounds.y() + renderBounds.height()
        );
        graphics.pose().translate(-displayOffsetX, 0, 0);
        // Render label
        var fontScale = resolvedStyle().get(StyleProperty.FONT_SCALE);
        var renderer = new ScaledTextRenderer(context);
        // Center the label vertically and horizontally
        var textWidth = renderer.measureWidth(label, fontScale);
        var textHeight = renderer.measureHeight(fontScale);
        var textX = (int) (bounds.x() + (bounds.width() - textWidth) / 2);
        if (textWidth > bounds.width()) {
            textX = bounds.x() + scissorPadding; // Align to left with padding if text is wider than button
        }
        var textY = (int) (bounds.y() + (bounds.height() - textHeight) / 2);
        textY += 1; // Adjust for better visual alignment
        // Text color
        var color = state().disabled()
            ? 0xFFAAAAAA
            : 0xFFFFFFFF;
        renderer.render(label, textX, textY, fontScale, color);
        graphics.disableScissor();
        // Undo transform
        graphics.pose().popPose();
    }

    private void scrollTextIfNeeded(RenderContext context) {
        var bounds = this.bounds();
        var fontScale = resolvedStyle().get(StyleProperty.FONT_SCALE);
        var renderer = new ScaledTextRenderer(context);

        var textWidth = renderer.measureWidth(label, fontScale);
        var shownWidth = bounds.width() - 4; // Account for scissor padding

        if (textWidth <= shownWidth) {
            displayOffsetX = 0;
            return;
        }

        var scrollWidth = textWidth - shownWidth;

        var time = System.currentTimeMillis() / 1000.0;
        var period = Math.max(scrollWidth * 0.25, 3.0);

        var progress = Math.sin(
            (Math.PI / 2.0) *
            Math.cos((Math.PI * 2.0) * time / period)
        ) / 2.0 + 0.5;

        displayOffsetX = (float) (scrollWidth * progress);
    }

    private void renderBackground(RenderContext context) {
        var graphics = context.graphics();
        var bounds = this.bounds();
        if (bounds.width() < 2 || bounds.height() < 2) {
            return; // Avoid division by zero or rendering issues if bounds are empty
        }
        // Override render to enable blending for semi-transparent textures
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // Render button texture
        try {
            //? if < 1.21 {
            /*graphics.blitNineSliced(
                WIDGETS_LOCATION,
                bounds.x(), bounds.y(), bounds.width(), bounds.height(),
                20, 4, 200, 20, 0, textureY()
            );
            *///?} elif >= 1.21 {
            graphics.blitSprite(
                    SPRITES.get(!state().disabled(), state().focused() || state().hovered()),
                    bounds.x(), bounds.y(), bounds.width(), bounds.height()
            );
            //?}

        } catch (Exception e) {
            // Log the error and continue rendering
            System.err.println("Error rendering button background: " + e.getMessage());
        }
        // Revert blending
        RenderSystem.disableBlend();
    }

    /**
     * Calculates the Y position of the button texture based on the button's state.
     *
     * @return the Y position of the button texture
     */
    private int textureY() {
        int textureY = 46;
        if (!state().disabled()) {
            textureY += 20;
        }
        return textureY;
    }
}
