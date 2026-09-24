package de.clickism.clickui.elements;

import de.clickism.clickui.UiElement;
import de.clickism.clickui.layout.Axis;
import de.clickism.clickui.layout.Point;
import de.clickism.clickui.render.RenderContext;
import net.minecraft.util.Mth;

// TODO: Overflow hidden, show, scroll etc.
// TODO: Fix nested scroll boxes don't work
// TODO: Fix scroll boxes capture scroll events even when not hovered
// TODO: Fix some elements cut off if content is too large or alignment is not correct

/**
 * A simple container element that can hold other elements
 * and provides scrolling functionality in case of
 * vertical overflow.
 */
public class Box extends UiElement<Box> {
    /**
     * The default scroll rate for all boxes.
     */
    private static final double DEFAULT_SCROLL_RATE = 9;

    /**
     * Scrollbar colors and dimensions.
     */
    protected static final int SCROLLBAR_BACKGROUND = 0xFF000000;
    protected static final int SCROLLBAR_COLOR = 0xFF808080;
    protected static final int SCROLLBAR_SHADOW_COLOR = 0xFFC0C0C0;

    protected static final int MIN_SCROLLBAR_HEIGHT = 32;
    protected static final int SCROLLBAR_WIDTH = 6;

    /**
     * The current vertical scroll offset of the box.
     */
    private double scrollY;
    /**
     * The scroll rate.
     */
    private double scrollRate = DEFAULT_SCROLL_RATE;
    /**
     * Whether the box is scrollable or not.
     */
    private boolean scrollable = false;

    /**
     * Whether the scrollbar is currently being dragged by the user.
     */
    private boolean draggingScrollbar = false;

    /**
     * Creates a new Box element with default settings.
     */
    public Box() {
        this.onScroll(event -> {
            if (!scrollable) return;
            this.scrollY(scrollY - event.delta() * scrollRate);
            event.consume();
        });
        // Drag events for scrolling
        this.onDragStart(event -> {
            if (!scrollable) return;
            if (isMouseOnScrollbar(event.startX(), event.startY())) {
                draggingScrollbar = true;
            }
            event.consume();
        });
        this.onDrag(event -> {
            if (!draggingScrollbar) return;
            // Scroll
            var y = bounds().y();
            var height = bounds().height();
            if (event.currentY() < y) {
                scrollY(0); // Scroll to top
            } else {
                if (event.currentY() > y + height) {
                    scrollY(maxScrollY()); // Scroll to bottom
                } else {
                    // Scroll proportionally to mouse position
                    double scrollRatio = Math.max(1, maxScrollY() / (height - scrollbarHeight()));
                    this.scrollY(scrollY + event.deltaY() * scrollRatio);
                }
            }
            event.consume();
        });
        this.onDragEnd(event -> {
            if (!draggingScrollbar) return;
            draggingScrollbar = false;
            event.consume();
        });
        // TODO: Add touch support for scrolling
    }

    /**
     * Sets whether this box is scrollable or not,
     * in case the content overflows vertically.
     *
     * @param scrollable true to make the box scrollable, false to disable scrolling
     * @return this box instance for chaining
     */
    public Box scrollable(boolean scrollable) {
        this.scrollable = scrollable;
        return this;
    }

    /**
     * Sets the scroll rate for this box.
     * The scroll rate determines how fast the content scrolls in response to scroll events.
     * <p>
     * Default is 1.0 corresponding to the default scroll rate.
     *
     * @param multiplier the scroll rate multiplier
     * @return this box instance for chaining
     */
    public Box scrollRate(double multiplier) {
        this.scrollRate = multiplier * DEFAULT_SCROLL_RATE;
        return this;
    }

    /**
     * Calculates the maximum vertical scroll offset based on the content height and the box height.
     *
     * @return the maximum vertical scroll offset
     */
    protected double maxScrollY() {
        return Math.max(0, contentHeight() - bounds().height());
    }

    /**
     * Sets the vertical scroll offset of the box, clamping it to the valid range.
     *
     * @param scrollY the new vertical scroll offset
     */
    protected void scrollY(double scrollY) {
        this.scrollY = Mth.clamp(scrollY, 0, maxScrollY());
    }

    /**
     * The total height of the content inside this box, including the gaps between children.
     *
     * @return the total content height
     */
    protected int contentHeight() {
        // TODO: Calculate via min and max XY of children
        if (axis().isHorizontal()) {
            // Horizontal axis
            return this.children().stream()
                       .mapToInt(child -> child.bounds().height())
                       .max()
                       .orElse(0) + padding().top() + padding().bottom();
        }
        // Vertical axis
        int totalPadding = padding().top() + padding().bottom();
        int totalGap = totalChildGap();
        int childrenHeight = this.children().stream()
            .mapToInt(child -> child.bounds().height())
            .sum();
        return childrenHeight + totalGap + totalPadding;
    }

    /**
     * Whether the content of this box is overflowing vertically and requires scrolling.
     *
     * @return true if the content is overflowing, false otherwise
     */
    protected boolean isOverflowing() {
        return maxScrollY() > 0;
    }

    @Override
    public boolean shrinkChildrenIfOverflowing(Axis axis) {
        // If the box is scrollable, we don't shrink children vertically
        if (scrollable && !axis.isHorizontal()) {
            return false;
        }
        return super.shrinkChildrenIfOverflowing(axis);
    }

    /**
     * Calculates the x position of the scrollbar.
     */
    protected int scrollbarX() {
        return this.bounds().x() + this.bounds().width() - SCROLLBAR_WIDTH;
    }

    /**
     * Calculates the height of the scrollbar based on the ratio of the container height to the content height.
     *
     * @return the height of the scrollbar
     */
    protected int scrollbarHeight() {
        var height = bounds().height();
        int scrollbarHeight = ((height * height) / contentHeight());
        // Ensure scrollbar is at least 32 pixels high
        int maxScrollbarHeight = height - 8; // Ensure a bit of leeway
        return Mth.clamp(scrollbarHeight, MIN_SCROLLBAR_HEIGHT, maxScrollbarHeight);
    }

    /**
     * Calculates the width of the scrollbar, which is SCROLLBAR_WIDTH or 0 if the scrollbar is not visible.
     *
     * @return the width of the scrollbar
     */
    protected int scrollbarWidth() {
        return isOverflowing()
            ? SCROLLBAR_WIDTH
            : 0;
    }

    /**
     * Checks if the mouse is currently hovering over the scrollbar.
     *
     * @param mouseX the x position of the mouse
     * @param mouseY the y position of the mouse
     * @return true if the mouse is hovering over the scrollbar, false otherwise
     */
    protected boolean isMouseOnScrollbar(double mouseX, double mouseY) {
        if (!scrollable) return false;
        if (!isOverflowing()) return false;
        if (!bounds().contains(mouseX, mouseY)) return false;
        int scrollbarX = scrollbarX();
        int scrollbarEndX = scrollbarX + SCROLLBAR_WIDTH;
        return mouseX >= scrollbarX && mouseX <= scrollbarEndX;
    }

    @Override
    public Point toChildRenderCoordinates(Point point) {
        return new Point(point.x(), (int) (point.y() + scrollY));
    }

    @Override
    public Point fromChildRenderCoordinates(Point point) {
        return new Point(point.x(), (int) (point.y() - scrollY));
    }

    @Override
    public void renderTree(RenderContext context) {
        if (!scrollable) {
            // Skip scroll rendering
            super.renderTree(context);
            return;
        }
        context = renderContextToUse(context);
        // Render self
        this.renderElement(context);
        // Render children with scroll offset
        var graphics = context.graphics();
        // Enable scissor to clip children
        var bounds = renderBounds();
        var x1 = bounds.x();
        var y1 = bounds.y();
        var x2 = bounds.x() + bounds.width();
        var y2 = bounds.y() + bounds.height();
        graphics.enableScissor(x1, y1, x2, y2);


        graphics.pose()
                //$ if <26.1 '.pushPose();' else '.pushMatrix();'
                .pushPose();
        // Apply scroll offset
        scrollY(scrollY); // Clamp scrollY to valid range
        //? if < 26.1
        graphics.pose().translate(0, -scrollY, 0);
        //? if >= 26.1
        //graphics.pose().translate(0, (int) -scrollY);

        // Render children
        for (var child : children()) {
            child.renderTree(context);
        }

        graphics.pose()
                //$ if <26.1 '.popPose();' else '.popMatrix();'
                .popPose();
        // Disable scissor
        graphics.disableScissor();

        if (scrollable && isOverflowing()) {
            graphics.pose()
                    //$ if <26.1 '.pushPose();' else '.pushMatrix();'
                    .pushPose();
            // Render scrollbar on top of children
            //? if < 26.1
            graphics.pose().translate(0, 0, 100);
            //? if >= 26.1
            //graphics.pose().translate(0, 0);

            renderScrollbar(context);

            graphics.pose()
                    //$ if <26.1 '.popPose();' else '.popMatrix();'
                    .popPose();
        }

    }

    @Override
    public void render(RenderContext context) {
        // Nothing to render for the box itself
    }

    /**
     * Renders the scrollbar on the right side of the container.
     *
     * @param context the render context to render with
     */
    protected void renderScrollbar(RenderContext context) {
        var graphics = context.graphics();
        int scrollbarX = scrollbarX();
        int scrollbarEndX = scrollbarX + SCROLLBAR_WIDTH;
        // Scrollbar background
        var height = bounds().height();
        var y = bounds().y();
        graphics.fill(scrollbarX, y, scrollbarEndX, y + height, SCROLLBAR_BACKGROUND);
        // Render scrollbar
        int scrollbarHeight = scrollbarHeight();
        int scrollbarY = (int) (scrollY * (height - scrollbarHeight) / maxScrollY()) + y;
        int scrollbarEndY = scrollbarY + scrollbarHeight;
        // Render scrollbar
        graphics.fill(scrollbarX, scrollbarY, scrollbarEndX, scrollbarEndY, SCROLLBAR_COLOR);
        // Render shadow
        graphics.fill(scrollbarX, scrollbarEndY, scrollbarEndX - 1, scrollbarEndY - 1, SCROLLBAR_SHADOW_COLOR);
    }
}
