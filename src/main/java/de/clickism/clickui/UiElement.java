package de.clickism.clickui;

import de.clickism.clickui.event.Event;
import de.clickism.clickui.event.EventManager;
import de.clickism.clickui.event.EventTarget;
import de.clickism.clickui.layout.*;
import de.clickism.clickui.render.RenderContext;
import de.clickism.clickui.render.ScaledTextRenderer;
import de.clickism.clickui.render.style.StyleRenderer;
import de.clickism.clickui.state.ElementState;
import de.clickism.clickui.state.ElementStateHolder;
import de.clickism.clickui.style.Style;
import de.clickism.clickui.style.StyleContext;
import de.clickism.clickui.style.StyleData;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An element in the UI hierarchy.
 */
public abstract class UiElement<S extends UiElement<S>>
    implements Layoutable<S>, ElementStateHolder<S>, EventTarget<S>, BaseComponents {
    // TODO: Visibility, style, hover, events, etc.
    // TODO: Simple scheduler
    /**
     * The parent element of this element, or null if this element is the root element.
     */
    private @Nullable UiElement<?> parent;
    /**
     * The children of this element.
     */
    private final List<UiElement<?>> children = new ArrayList<>();

    /**
     * Whether this element is the root of the tree and needs to be layed out again.
     */
    private boolean dirtyLayout = false;

    /**
     * Layout information for this element,
     */
    private final Layout layout = new Layout();
    /**
     * Style information for this element.
     */
    private Style style = Style.empty();
    /**
     * The state of this element, used for rendering.
     */
    private final ElementState state = new ElementState(this);
    /**
     * The event manager for this element, used for handling events.
     */
    private final EventManager events = new EventManager();
    private final EventManager globalEvents = new EventManager();

    private @Nullable UiElement<?> tooltip = null;

    /**
     * Calculated bounds of the element.
     */
    private Rect bounds = Rect.ZERO;

    /**
     * Whether this element can be hit by a mouse click event.
     */
    private boolean hitTestable = true;

    /**
     * Indicates whether debug mode is enabled for this element.
     * When enabled, additional debug information may be rendered.
     * <p>
     * If not specified, the debug mode will be inherited from the parent element.
     */
    private @Nullable Boolean debug = null;

    /**
     * Returns the intrinsic size of this element,
     * which is the size that this element would like to be if it could be any size.
     * <p>
     * Should not take children into account, as they will be layed out separately.
     *
     * @return The intrinsic size of this element.
     */
    public Size intrinsicSize() {
        return Size.ZERO;
    }

    /**
     * Returns the default minimum size of this element, which is the smallest size that this element can be
     * without breaking its layout or appearance.
     * <p>
     * This can be overriden via {@link Layout#minWidth(int)} and {@link Layout#minHeight(int)}.
     *
     * @return The minimum size of this element.
     */
    public Size defaultMinSize() {
        return Size.ZERO;
    }

    /**
     * Returns the effective minimum size of this element, which is the smallest size that this element can be
     * without breaking its layout or appearance, taking into account any overrides set.
     *
     * @return The effective minimum size of this element.
     */
    @ApiStatus.Internal
    public final Size effectiveMinSize() {
        var minWidth = width().min() != null
            ? width().min()
            : defaultMinSize().width();
        var minHeight = height().min() != null
            ? height().min()
            : defaultMinSize().height();
        return new Size(minWidth, minHeight);
    }

    /**
     * Returns the effective maximum size of this element, which is the largest size that this element can be
     * without breaking its layout or appearance, taking into account any overrides set.
     *
     * @return The effective maximum size of this element.
     */
    @ApiStatus.Internal
    public final Size effectiveMaxSize() {
        var maxWidth = width().max() != null
            ? width().max()
            : Integer.MAX_VALUE;
        var maxHeight = height().max() != null
            ? height().max()
            : Integer.MAX_VALUE;
        return new Size(maxWidth, maxHeight);
    }

    /**
     * Returns whether this element should shrink its children if they are overflowing the bounds of this element.
     *
     * @param axis the axis to check for overflow
     * @return Whether this element should shrink its children.
     */
    public boolean shrinkChildrenIfOverflowing(Axis axis) {
        return true;
    }

    /**
     * Gets the bounds of this element, which is the rectangle that this element occupies in the coordinate space.
     *
     * @return The bounds of this element.
     */
    public Rect bounds() {
        return bounds;
    }

    /**
     * Sets the bounds of this element, which is the rectangle that this element occupies in the coordinate space.
     * This method is intended to be called by the layout system, and should not be called directly by user code.
     *
     * @param bounds The new bounds of this element.
     */
    @ApiStatus.Internal
    public void bounds(Rect bounds) {
        this.bounds = bounds;
    }

    /**
     * Returns the parent of this element, or null if this element is the root element.
     *
     * @return the parent of this element, or null if this element is the root element
     */
    public @Nullable UiElement<?> parent() {
        return this.parent;
    }

    /**
     * Returns an unmodifiable list of the children of this element.
     *
     * @return an unmodifiable list of the children of this element
     */
    public List<UiElement<?>> children() {
        return Collections.unmodifiableList(this.children);
    }

    /**
     * Returns a list of the children of this element that are layoutable,
     * i.e. that have a positioning of {@link Positioning#layout()}.
     *
     * @return a list of the children of this element that are layoutable
     */
    public List<UiElement<?>> layoutChildren() {
        return this.children.stream()
            .filter(child -> child.positioning().affectsLayout())
            .toList();
    }

    /**
     * Adds the given children to this element, and sets their parent to this element.
     *
     * @param children the children to add
     * @return this element
     */
    public S children(@Nullable UiElement<?>... children) {
        for (var child : children) {
            this.add(child);
        }
        return self();
    }

    /**
     * Adds the given children to this element, and sets their parent to this element.
     *
     * @param children the children to add
     * @return this element
     */
    public S children(Collection<UiElement<?>> children) {
        if (children == null) return self();
        for (var child : children) {
            this.add(child);
        }
        return self();
    }

    /**
     * Adds the given child to this element, and sets its parent to this element.
     *
     * @param child the child to add
     * @return this element
     */
    public S add(@Nullable UiElement<?> child) {
        // Allow null children to be passed in, but ignore them
        if (child == null) return self();
        this.children.add(child);
        if (child.parent != null) {
            child.parent.children.remove(child);
        }
        child.parent = this;
        return self();
    }

    /**
     * Removes the given child from this element, and sets its parent to null.
     *
     * @param child the child to remove
     * @return this element
     */
    public S remove(@Nullable UiElement<?> child) {
        if (child == null) return self();
        this.children.remove(child);
        if (child.parent == this) {
            child.parent = null;
        }
        return self();
    }

    /**
     * Removes all children from this element, and sets their parent to null.
     *
     * @return this element
     */
    public S clear() {
        for (var child : children) {
            child.parent = null;
        }
        this.children.clear();
        return self();
    }

    @Override
    public Layout layout() {
        return this.layout;
    }

    @Override
    public ElementState state() {
        return this.state;
    }

    /**
     * Returns the style of this element, which is used for rendering.
     *
     * @return the style of this element
     */
    public Style elementStyle() {
        return this.style;
    }

    /**
     * Returns the resolved style of this element.
     *
     * @return the resolved style of this element
     */
    public StyleData resolvedStyle() {
        return this.style.resolve(new StyleContext(this, this.state));
    }

    /**
     * Merges the given style into the existing style of this element, combining their style properties.
     *
     * @param style the style to set
     * @return this element
     */
    public S style(Style style) {
        this.style.merge(style);
        return self();
    }

    /**
     * Overrides the style of this element with the given style, replacing any existing style.
     *
     * @param style the style to override with
     * @return this element
     */
    public S overrideStyle(Style style) {
        this.style = style;
        return self();
    }

    @Override
    public EventManager events() {
        return this.events;
    }

    /**
     * Returns the global event manager for this element.
     *
     * @return the global event manager for this element
     */
    public EventManager globalEvents() {
        return this.globalEvents;
    }

    /**
     * Propagates the given event to this element and all of its children recursively.
     *
     * @param event the event to propagate
     */
    public void propagateEventDown(Event event) {
        // Fire children first
        for (var child : children) {
            child.propagateEventDown(event);
            if (event.state().consumed()) {
                return;
            }
        }

        // Fire this element's event manager
        this.events.fireEvent(event);
    }

    /**
     * Propagates the given event to this element and all of its children recursively,
     * using the global event manager.
     *
     * @param event the event to propagate
     */
    public void propagateEventDownGlobal(Event event) {
        if (event.state().consumed()) {
            return;
        }
        // Fire children first
        for (var child : children) {
            child.propagateEventDownGlobal(event);
            if (event.state().consumed()) {
                return;
            }
        }

        // Fire this element's global event manager
        this.globalEvents.fireEvent(event);
    }

    /**
     * Propagates the given event to this element and all of its parents recursively.
     *
     * @param event the event to propagate
     */
    public void propagateEventUp(Event event) {
        // Fire this element's event manager
        this.events.fireEvent(event);
        if (event.state().consumed()) {
            return;
        }

        // Fire parent last
        if (parent != null) {
            parent.propagateEventUp(event);
        }
    }

    /**
     * Invalidates the layout of this element tree.
     */
    public void invalidateLayout() {
        root().dirtyLayout = true;
    }

    /**
     * Returns the root element of this element tree, which is the topmost ancestor of this element.
     *
     * @return the root element of this element tree
     */
    public UiElement<?> root() {
        UiElement<?> root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root;
    }

    /**
     * Returns whether this element is dirty and needs to be layed out again.
     *
     * @return whether this element is dirty and needs to be layed out again
     */
    @ApiStatus.Internal
    public boolean dirtyLayout() {
        return root().dirtyLayout;
    }

    /**
     * Clears the dirty flag of this element.
     */
    @ApiStatus.Internal
    public void clearDirtyLayout() {
        root().dirtyLayout = false;
    }

    /**
     * Converts a point from this element's render coordinates, to its child's render coordinates.
     *
     * @param point the point in this element's render coordinates
     * @return the point in the child's render coordinates
     */
    public Point toChildRenderCoordinates(Point point) {
        return point;
    }

    /**
     * Converts a point from this element's child's render coordinates, to its own render coordinates.
     *
     * @param point the point in the child's render coordinates
     * @return the point in this element's render coordinates
     */
    public Point fromChildRenderCoordinates(Point point) {
        return point;
    }

    /**
     * Returns the position of this element in screen coordinates, which is the position of this element relative to the top-left corner of the screen.
     *
     * @return the position of this element in screen coordinates
     */
    public Point renderPosition() {
        int x = bounds().x();
        int y = bounds().y();

        UiElement<?> element = this;
        UiElement<?> parent = element.parent();
        while (parent != null) {
            var position = parent.fromChildRenderCoordinates(new Point(x, y));
            x = position.x();
            y = position.y();
            element = parent;
            parent = element.parent();
        }
        return new Point(x, y);
    }

    /**
     * Returns the bounds of this element in screen coordinates, which is the rectangle that this element occupies relative to the top-left corner of the screen.
     *
     * @return the bounds of this element in screen coordinates
     */
    public Rect renderBounds() {
        var position = renderPosition();
        return new Rect(position.x(), position.y(), bounds().width(), bounds().height());
    }

    /**
     * Returns the tooltip of this element, which is an element that is displayed when the user hovers over this element.
     *
     * @return the tooltip of this element, or null if no tooltip is set
     */
    public UiElement<?> tooltip() {
        return tooltip;
    }

    /**
     * Sets the tooltip of this element, which is a text that is displayed when the user hovers over this element.
     *
     * @param tooltip the tooltip to set, or null to clear the tooltip
     * @return this element
     */
    public S tooltip(@Nullable Component tooltip) {
        return tooltip(tooltip, 200);
    }

    /**
     * Sets the tooltip of this element, which is a text that is displayed when the user hovers over this element.
     *
     * @param tooltip  the tooltip to set, or null to clear the tooltip
     * @param maxWidth the maximum width of the tooltip
     * @return this element
     */
    public S tooltip(@Nullable Component tooltip, int maxWidth) {
        return tooltip(tooltip != null
            ? box()
            .maxWidth(maxWidth)
            .children(
                text(tooltip)
            )
            : null);
    }

    /**
     * Sets the tooltip of this element, which is a text that is displayed when the user hovers over this element.
     *
     * @param tooltip the tooltip to set, or null to clear the tooltip
     * @return this element
     */
    public S tooltip(@Nullable String tooltip) {
        return tooltip(tooltip != null
            ? Component.literal(tooltip)
            : null);
    }

    /**
     * Sets the tooltip of this element, which is an element that is displayed when the user hovers over this element.
     *
     * @param tooltip the tooltip to set, or null to clear the tooltip
     * @return this element
     */
    public S tooltip(@Nullable UiElement<?> tooltip) {
        this.tooltip = tooltip;
        if (tooltip != null) {
            this.tooltip.invalidateLayout();
        }
        return self();
    }

    /**
     * Returns whether the tooltip of this element is currently visible.
     *
     * @return whether the tooltip of this element is currently visible
     */
    public boolean isTooltipVisible() {
        return tooltip != null && state().hovered();
    }

    /**
     * Returns whether this element can be hit by a mouse click event.
     *
     * @return whether this element can be hit by a mouse click event
     */
    public boolean hitTestable() {
        return this.hitTestable;
    }

    /**
     * Sets whether this element can be hit by a mouse click event.
     *
     * @param hitTestable whether this element can be hit by a mouse click event
     * @return this element
     */
    public S hitTestable(boolean hitTestable) {
        this.hitTestable = hitTestable;
        return self();
    }

    /**
     * Sets whether debug mode is enabled for this element.
     * When enabled, additional debug information may be rendered.
     * <p>
     * If not specified, the debug mode will be inherited from the parent element.
     *
     * @param debug whether debug mode is enabled for this element
     * @return this element
     */
    public S debug(@Nullable Boolean debug) {
        this.debug = debug;
        return self();
    }

    /**
     * Sets the given ref to this element, allowing external code to hold a reference to this element.
     *
     * @param ref the ref to set
     * @return this element
     */

    public S ref(Ref<S> ref) {
        ref.set(self());
        return self();
    }

    /**
     * Returns the total gap between all children of this element.
     *
     * @return the total gap between all children of this element
     */
    public int totalChildGap() {
        return childGap() * Math.max(0, layoutChildren().size() - 1);
    }

    /**
     * Renders this element and all of its children recursively.
     *
     * @param context the render context to render to
     */
    public void renderTree(RenderContext context) {
        var contextToUse = renderContextToUse(context);
        this.renderElement(contextToUse);
        // Render children
        for (var child : children) {
            child.renderTree(contextToUse);
        }
    }

    /**
     * Returns the render context to use for rendering this element, taking into account the debug mode.
     *
     * @param context the original render context
     * @return the render context to use for rendering this element
     */
    protected RenderContext renderContextToUse(RenderContext context) {
        if (debug != null) {
            // Override specified mode if enabled for this element
            return context.withDebug(debug);
        }
        return context;
    }

    /**
     * Renders this element with its style applied, renders debug information if debug mode is enabled.
     *
     * @param context the render context to render with
     */
    public void renderElement(RenderContext context) {
        new StyleRenderer(this, context).renderElement();
        // Render debug information if debug mode is enabled
        if (context.debug()) {
            renderDebugInfo(context);
        }
    }

    /**
     * Renders debug information for this element, such as its bounds and layout information.
     *
     * @param context the render context to render with
     */
    public void renderDebugInfo(RenderContext context) {
        // Render the bounds of this element as a red outline
        var graphics = context.graphics();
        graphics
                //? if < 26.1
                .renderOutline(
                //? if >= 26.1
                //.outline(
            bounds().x(),
            bounds().y(),
            bounds().width(),
            bounds().height(),
            0xffff0000
        );

        // Render overlay if hovered
        if (state().hoveredSelf()) {
            graphics.fill(
                bounds().x(),
                bounds().y(),
                bounds().x() + bounds().width(),
                bounds().y() + bounds().height(),
                0x40ff0000
            );
        }

        // Render class name above bounds
        var renderer = new ScaledTextRenderer(context);
        var scale = 0.5f;
        var height = (int) (context.font().lineHeight * scale);

        var renderInside = padding().top() >= height;
        var offsetY = renderInside
            ? 2
            : -height - 1;
        var offsetX = renderInside
            ? 2
            : 0;
        renderer.render(
            Component.literal(this.getClass().getSimpleName()),
            bounds().x() + offsetX,
            bounds().y() + offsetY,
            scale,
            UiColor.RED.color()
        );
    }

    /**
     * Renders this element only, without rendering its children.
     *
     * @param context the render context to render with
     */
    public abstract void render(RenderContext context);

    /**
     * This method should be called every tick to update the state of this element.
     * By default, this method does nothing, but subclasses can override it to perform
     * periodic updates, such as animations or state changes.
     */
    public void tick() {
        // Nothing here
    }

    @Override
    public String toString() {
        var root = parent == null
            ? "<root>"
            : "";
        return this.getClass().getSimpleName()
               + root
               + "["
               + "bounds=" + bounds + ", "
               + "children=" + children.size()
               + "]";
    }
}
