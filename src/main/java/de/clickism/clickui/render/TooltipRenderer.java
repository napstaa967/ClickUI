package de.clickism.clickui.render;

import de.clickism.clickui.UiElement;
import de.clickism.clickui.UiElementTree;
import de.clickism.clickui.UiScreenHandler;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;

/**
 * Renders a tooltip element at the mouse position, ensuring it is displayed above other UI elements.
 */
public class TooltipRenderer {
    private static final int TOOLTIP_Z_INDEX = 1000; // Render tooltips above other elements

    private final UiElementTree tooltip;
    private final RenderContext context;

    /**
     * Creates a new TooltipRenderer for the specified tooltip element and render context.
     * The tooltip should either be already layed out, or be marked as dirty/invalidated.
     *
     * @param tooltip The tooltip element to render.
     * @param context The render context.
     */
    public TooltipRenderer(UiElement<?> tooltip, RenderContext context) {
        this.tooltip = new UiElementTree(tooltip);
        this.context = context;
    }

    /**
     * Renders the tooltip at the current mouse position.
     */
    public void render() {
        var screen = UiScreenHandler.current();
        if (screen == null) return;

        // Prepare the tooltip for rendering
        tooltip.prepareRender(screen.width, screen.height);

        // Render at the mouse position
        var offset = TooltipRenderUtil.MOUSE_OFFSET;
        // Calculate the position of the tooltip
        double tooltipX = context.mouseX() + offset;
        double tooltipY = context.mouseY() + offset;

        // Adjust position if tooltip goes off-screen
        var bounds = tooltip.root().bounds();
        if (tooltipX + bounds.width() + offset > screen.width) {
            tooltipX = screen.width - bounds.width() - offset;
        }
        if (tooltipY + bounds.height() + offset > screen.height) {
            tooltipY = screen.height - bounds.height() - offset;
        }

        // Render the tooltip at the calculated position
        var graphics = context.graphics();
        graphics.pose()
                //$ if < 26.1 '.pushPose();' else '.pushMatrix();'
                .pushPose();
        //? if < 26.1
        graphics.pose().translate(tooltipX, tooltipY, TOOLTIP_Z_INDEX);
        //? if >= 26.1
        //graphics.pose().translate((int) tooltipX, (int) tooltipY);

        // Render background with padding
        //? if < 26.1 {
        TooltipRenderUtil.renderTooltipBackground(
            context.graphics(),
            bounds.x(),
            bounds.y(),
            bounds.width(),
            bounds.height(),
            -1 // Render behind actual tooltip
        );
        //?} elif >= 26.1 {
        /*TooltipRenderUtil.extractTooltipBackground(
                context.graphics(),
                bounds.x(),
                bounds.y(),
                bounds.width(),
                bounds.height(),
                null
        );
        *///?}
        // Render tooltip content
        tooltip.render(context);
        graphics.pose()
                //$ if < 26.1 '.popPose();' else '.popMatrix();'
                .popPose();
    }
}
