package de.clickism.clickui.elements.input;

import com.mojang.blaze3d.systems.RenderSystem;
import de.clickism.clickui.UiColor;
import de.clickism.clickui.UiElement;
import de.clickism.clickui.layout.Size;
import de.clickism.clickui.render.RenderContext;
import de.clickism.clickui.style.Border;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * A simple checkbox UI element that can be toggled on and off.
 */
public class Checkbox extends UiElement<Checkbox> {
    /**
     * The checkbox texture to render.
     */
    //? if < 1.21 {
    /*public static final ResourceLocation TEXTURE = ResourceLocation.tryBuild(
        ResourceLocation.DEFAULT_NAMESPACE,
        "textures/gui/checkbox.png"
    );
    *///?} elif >= 1.21 {
    private static final ResourceLocation TEXTURE_BOTH = ResourceLocation.withDefaultNamespace("widget/checkbox_selected_highlighted");
    private static final ResourceLocation TEXTURE_SELECTED = ResourceLocation.withDefaultNamespace("widget/checkbox_selected");
    private static final ResourceLocation TEXTURE_HIGHLIGHTED = ResourceLocation.withDefaultNamespace("widget/checkbox_highlighted");
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("widget/checkbox");
    /*private static final ResourceLocation TEXTURE_BOTH = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/checkbox_selected_highlighted.png");
    private static final ResourceLocation TEXTURE_SELECTED = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/checkbox_selected.png");
    private static final ResourceLocation TEXTURE_HIGHLIGHTED = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/checkbox_highlighted.png");
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/checkbox.png");*/

    /**
     * Returns the proper texture for this state
     */
    private ResourceLocation resolve() {
        if (!state().disabled()) {
            if (state().hovered() && checked) {
                return TEXTURE_BOTH;
            } else if (state().hovered()) {
                return TEXTURE_HIGHLIGHTED;
            } else if (checked) {
                return TEXTURE_SELECTED;
            }
        }
        return TEXTURE;
    }
    //?}

    private static final int SIZE = 20;

    private boolean checked = false;

    private Consumer<Boolean> onCheckedChange = state -> {};

    /**
     * Creates a new Checkbox element.
     */
    public Checkbox() {
        this.onClick(event -> {
            event.playSound();
            this.toggle();
            this.onCheckedChange.accept(this.checked);
        });
        this.style(style()
            .borderPosition(Border.Position.INSIDE)
            .borderColor(UiColor.BLACK)
            .whenHovered(style()
                .borderColor(UiColor.WHITE)));
    }

    /**
     * Sets a listener that will be called whenever the checked state changes.
     *
     * @param listener the listener to call when the checked state changes
     * @return this Checkbox instance for method chaining
     */
    public Checkbox onCheckedChange(Consumer<Boolean> listener) {
        this.onCheckedChange = listener;
        return this;
    }

    @Override
    public Size intrinsicSize() {
        return new Size(SIZE, SIZE);
    }

    /**
     * Sets the checked state of the checkbox.
     *
     * @param checked the new checked state
     * @return this Checkbox instance for method chaining
     */
    public Checkbox checked(boolean checked) {
        this.checked = checked;
        return this;
    }

    /**
     * Toggles the checked state of the checkbox.
     *
     * @return this Checkbox instance for method chaining
     */
    public Checkbox toggle() {
        this.checked = !this.checked;
        return this;
    }

    /**
     * Returns the current checked state of the checkbox.
     *
     * @return true if the checkbox is checked, false otherwise
     */
    public boolean checked() {
        return this.checked;
    }

    @Override
    public void render(RenderContext context) {
        var graphics = context.graphics();
        var bounds = this.bounds();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        // Adjust scale to fit in the checkbox size
        graphics.pose().pushPose();
        graphics.pose().translate(bounds.x(), bounds.y(), 0.0F);
        graphics.pose().scale((float) bounds.width() / SIZE, (float) bounds.height() / SIZE, 1.0F);
        graphics.pose().translate(-bounds.x(), -bounds.y(), 0.0F);
        // Render the checkbox texture based on its state (focused and checked)
        //? if < 1.21 {
        /*graphics.blit(
            TEXTURE,
            bounds.x(),
            bounds.y(),
            0.0F,
            checked
                ? 20.0F
                : 0.0F,
            SIZE,
            SIZE,
            64,
            64
        );
        *///?} elif >= 1.21 {
        graphics.blitSprite(resolve(), bounds.x(), bounds.y(), SIZE, SIZE);
        //?}
        graphics.pose().popPose();
    }
}
