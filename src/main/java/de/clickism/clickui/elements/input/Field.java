package de.clickism.clickui.elements.input;

import de.clickism.clickui.UiColor;
import de.clickism.clickui.layout.Padding;
import de.clickism.clickui.layout.Size;
import de.clickism.clickui.render.RenderContext;
import de.clickism.clickui.style.Border;
import de.clickism.clickui.style.StyleProperty;
import de.clickism.clickui.util.Util;
//? if < 26.1
//import net.minecraft.client.renderer.RenderType;
//? if >= 26.1
import net.minecraft.client.renderer.RenderPipelines;

/**
 * A simple implementation of a text field with default styling and behavior.
 */
public abstract class Field<S extends Field<S>> extends AbstractField<S> {
    private static final Padding DEFAULT_PADDING = Padding.create(6, 5, 5, 5);
    private static final int DEFAULT_WIDTH = 100;

    private UiColor invalidColor = UiColor.rgb(0xFF5555);
    private boolean highlightInvalid = false;
    private boolean textShadow = true;

    public Field() {
        // Set default props
        // TODO: Better default height
        this.padding(DEFAULT_PADDING);
        // Set style
        this.style(style()
            .backgroundColor(UiColor.BLACK)
            .borderColor(UiColor.rgb(0xA0A0A0))
            .borderPosition(Border.Position.INSIDE)
            .textColor(UiColor.rgb(0xE0E0E0))
            .whenHovered(style()
                .borderColor(UiColor.WHITE))
            .whenFocused(style()
                .borderColor(UiColor.WHITE))
        );
    }

    /**
     * Sets the color to be used when the field is in an invalid state.
     *
     * @param color The color to use for invalid state.
     * @return The current instance of the field for method chaining.
     */
    public S invalidColor(UiColor color) {
        this.invalidColor = color;
        return self();
    }

    /**
     * Sets whether the text should have a shadow effect.
     *
     * @param shadow true to enable text shadow, false to disable
     * @return The current instance of the field for method chaining.
     */
    public S textShadow(boolean shadow) {
        this.textShadow = shadow;
        return self();
    }

    /**
     * Sets whether the field should highlight itself when in an invalid state,
     * instead of changing the text color.
     *
     * @param highlight true to highlight the field when invalid, false to change text color
     * @return The current instance of the field for method chaining.
     */
    public S highlightInvalid(boolean highlight) {
        this.highlightInvalid = highlight;
        return self();
    }

    /**
     * Determines the text color based on the state of the text field.
     *
     * @param placeholder Indicates whether the text being rendered is a placeholder.
     * @return The RGB color value for the text.
     */
    protected int textColor(boolean placeholder) {
        if (invalid() && !highlightInvalid) {
            return invalidColor.color();
        }
        if (placeholder) {
            return UiColor.GRAY.color();
        }
        return resolvedStyle().get(StyleProperty.TEXT_COLOR).color();
    }

    @Override
    public Size intrinsicSize() {
        var height = Util.font().lineHeight;
        return new Size(DEFAULT_WIDTH, height);
    }

    @Override
    protected void renderText(
        RenderContext context,
        String text,
        int x,
        int y,
        boolean placeholder,
        String sugestion
    ) {
        var graphics = context.graphics();
        // Render invalid background if needed
        if (invalid() && highlightInvalid) {
            context.graphics().fill(
                x - 1,
                y - 1,
                x + context.font().width(text),
                y + context.font().lineHeight + 1,
                invalidColor.alpha(0.6f).color()
            );
        }
        // Render text
        var color = textColor(placeholder);
        //? if < 26.1
        //graphics.drawString
        //? if >= 26.1
        graphics.text
                (context.font(), text, x, y, color, textShadow);
        // Render suggestion
        x += context.font().width(text);
        //? if < 26.1
        //graphics.drawString
        //? if >= 26.1
        graphics.text
                (context.font(), sugestion, x, y, UiColor.GRAY.color(), textShadow);
    }

    @Override
    protected void renderCursor(RenderContext context, int x, int y, boolean inline) {
        var color = textColor(false);
        if (inline) {
            // Inline cursor as line
            y -= 1; // Render slightly above the text for better visibility
            var width = 1;
            var height = context.font().lineHeight + 1;
            context.graphics().fill(
                    //? if < 26.1
                    //RenderType.guiOverlay()
                    //? if >= 26.1
                    RenderPipelines.GUI
                    , x, y, x + width, y + height, color);
        } else {
            // Underscore cursor
            //? if < 26.1
            //context.graphics().drawString
            //? if >= 26.1
            context.graphics().text
                    (context.font(), "_", x, y, color, false); // Never shadow
        }
    }

    @Override
    protected void renderHighlight(RenderContext context, int x, int y, int width) {
        // Render highlight rectangle
        context.graphics().fill(
            //? if < 26.1
            //RenderType.guiTextHighlight(),
            //? if >= 26.1
            RenderPipelines.GUI_TEXT_HIGHLIGHT,
            x - 1,
            y - 1,
            x + width,
            y + context.font().lineHeight + 1,
            0xFF0000FF
        );
    }
}
