package de.clickism.clickui.render.style;

import de.clickism.clickui.UiElement;
import de.clickism.clickui.render.RenderContext;
import de.clickism.clickui.style.*;
//? if >= 26.1 {
/*import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.blaze3d.systems.RenderSystem;
*///?}

public class StyleRenderer {
    private final UiElement<?> element;
    private final RenderContext context;

    public StyleRenderer(UiElement<?> element, RenderContext context) {
        this.element = element;
        this.context = context;
    }

    public void renderElement() {
        var style = element.elementStyle().resolve(new StyleContext(element, element.state()));

        // Apply alpha
        //? if < 26.1
        context.graphics().setColor(1.0f, 1.0f, 1.0f, style.get(StyleProperty.ALPHA));

        // Render background
        var background = style.get(StyleProperty.BACKGROUND_COLOR);
        if (background != null) {
            renderBackground(background.color());
        }

        // Render pre-hooks
        style.renderHooks().forEach(hook -> {
            if (hook.type() == RenderHook.Type.PRE) {
                hook.renderer().render(context, element);
            }
        });

        // Render element itself
        element.render(context);

        // Render overlay
        var overlay = style.get(StyleProperty.OVERLAY_COLOR);
        if (overlay != null) {
            renderBackground(overlay.color());
        }

        // Render border
        var borderWidth = style.get(StyleProperty.BORDER_WIDTH);
        var borderPosition = style.get(StyleProperty.BORDER_POSITION);
        var borderColor = style.get(StyleProperty.BORDER_COLOR);

        var border = FourSided.<Border>of(null);
        for (var side : FourSided.Side.values()) {
            border = border.with(side, new Border(
                borderWidth.get(side),
                borderPosition.get(side),
                borderColor.get(side)
            ));
        }

        var borderRenderer = new BorderRenderer(context, element.bounds(), border);
        borderRenderer.render();

        // Render post-hooks
        style.renderHooks().forEach(hook -> {
            if (hook.type() == RenderHook.Type.POST) {
                hook.renderer().render(context, element);
            }
        });

        // Revert alpha
        //? if < 26.1
        context.graphics().setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    protected void renderBackground(int color) {
        context.graphics().fill(
            element.bounds().x(),
            element.bounds().y(),
            element.bounds().x() + element.bounds().width(),
            element.bounds().y() + element.bounds().height(),
            color
        );
    }
}
