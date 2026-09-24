package de.clickism.clickui;

import de.clickism.clickui.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * A screen that handles a UI tree and manages rendering, layout, and events.
 */
public class UiScreenHandler extends UiEventHandler implements UiScreenControls {
    /**
     * The parent screen of this UiScreenHandler, if any.
     * This can be used to navigate back to the previous screen.
     */
    private @Nullable Screen parentScreen;

    /**
     * The element tree of the UI.
     */
    private final UiElementTree tree;

    /**
     * Creates a new UiScreenHandler with the specified root element.
     *
     * @param root the root element of the UI tree
     */
    public UiScreenHandler(UiElement<?> root) {
        super(Component.empty(), root);
        this.tree = new UiElementTree(root);
    }

    @Override
    protected void init() {
        super.init();
        // Called when size changes or screen is initialized
        tree.invalidateLayout();
    }

    @Override
    //? if < 26.1 {
    /*public void render
    *///?} elif >= 26.1 {
    public void extractRenderState
    //?}
            (GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // Prepare the tree for render
        tree.prepareRender(this.width, this.height);
        // Call event handler
        //? if < 26.1
        /*super.render(graphics, mouseX, mouseY, delta);*/
        //? if >= 26.1
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        // Render the tree
        var context = new RenderContext(graphics, mouseX, mouseY, delta, this, false);
        tree.render(context);
        // Render tooltips
        tree.renderTooltips(context);
    }

    @Override
    public void tick() {
        super.tick();
        // Tick the tree
        tree.tick();
    }

    @Override
    public void onClose() {
        // Call onClose on the root if it is a UiScreen
        if (tree.root() instanceof UiScreen<?> screen) {
            screen.handleClose();
        } else {
            this.close();
        }
    }

    @Override
    public Screen screenToOpen() {
        return this;
    }

    /**
     * Returns the parent screen of this UiScreenHandler, if any.
     *
     * @return the parent screen, or null if there is no parent
     */
    public @Nullable Screen parentScreen() {
        return this.parentScreen;
    }

    /**
     * Sets the parent screen of this UiScreenHandler.
     *
     * @param screen the parent screen to set, or null if there is no parent
     */
    public void parentScreen(@Nullable Screen screen) {
        this.parentScreen = screen;
    }

    /**
     * Returns the current UiScreenHandler, if it exists.
     *
     * @return the current UiScreenHandler, or null if the open screen is not a UiScreenHandler
     */
    public static @Nullable UiScreenHandler current() {
        var screen = Minecraft.getInstance().screen;
        if (screen instanceof UiScreenHandler handler) {
            return handler;
        }
        return null;
    }
}
