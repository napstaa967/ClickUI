package de.clickism.clickui.elements.input;

import de.clickism.clickui.UiElement;
import de.clickism.clickui.layout.Point;
import de.clickism.clickui.render.RenderContext;
import de.clickism.clickui.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

// Versioned dependencies
//? if < 1.21{
/*import net.minecraft.SharedConstants;
*///?} elif >= 1.21 {
import net.minecraft.util.StringUtil;
//?}


/**
 * An abstract class representing a text field UI element.
 * It provides basic functionality for text input, cursor movement, and text editing.
 *
 * @param <S> the type of the subclass extending this abstract class
 */
public abstract class AbstractField<S extends AbstractField<S>>
    extends UiElement<S> {

    private String value = "";
    private String placeholder = "";

    protected int cursorPos = 0;
    protected int highlightPos = 0;
    protected int displayPos = 0;

    private boolean scrolling = true;

    private int maxLength = Integer.MAX_VALUE;

    private int blinkInterval = 300; // Blink every 300 ms

    private Function<String, String> inputFilter = Function.identity();
    private Predicate<String> validator = s -> true;

    private final List<Consumer<String>> listeners = new ArrayList<>();

    private Function<String, String> suggestionProvider = s -> "";
    private String currentSuggestion = "";

    private boolean invalidInput = false;
    private boolean allowInput = true;
    private boolean multiLine = false;

    /*+
     * Constructs a new AbstractTextField instance.
     */
    public AbstractField() {
        // Register key press event handler
        this.onKeyPress(event -> {
            if (!listening()) return;
            if (handleKeyPress(event.code())) {
                event.consume();
            }
        });
        this.onKeyHeld(event -> {
            if (!listening()) return;
            if (handleKeyPress(event.code())) {
                event.consume();
            }
        });
        // Register character typed event handler
        this.onCharTyped(event -> {
            if (!listening()) return;
            // Insert text
            insertText(Character.toString(event.character()));
            event.consume();
        });
        // Click to move cursor
        this.onClick(event -> {
            if (!listening()) return;
            int newCursorPos = cursorPosAt(event.x(), event.y());
            cursorPos = Mth.clamp(newCursorPos, 0, value.length());
            if (!Screen.hasShiftDown()) {
                highlightPos = cursorPos;
            }
        });
    }

    /**
     * Get the current text value of the text box.
     *
     * @return the current text value of the text box
     */
    public String value() {
        return this.value;
    }

    /**
     * Set the text value of the text box.
     *
     * @param value the text value to set
     */
    public S value(String value) {
        if (Objects.equals(this.value, value)) {
            // Old value, don't trigger anything
            return self();
        }
        this.value = value;
        // Make sure cursor is valid still
        this.cursorPos = Mth.clamp(cursorPos, 0, value.length());
        this.highlightPos = cursorPos;
        this.handleCursorMove();
        return self();
    }

    /**
     * Sets the placeholder text for the text box.
     *
     * @param placeholder the placeholder text to set
     * @return the current instance of the text box
     */
    public S placeholder(String placeholder) {
        this.placeholder = placeholder;
        return self();
    }

    /**
     * Registers a listener that will be called whenever the value of the text box changes.
     *
     * @param listener the listener to register
     * @return the current instance of the text box
     */
    public S onValueChanged(Consumer<String> listener) {
        this.listeners.add(listener);
        return self();
    }

    /**
     * Sets the maximum length of the text that can be entered into the text box.
     *
     * @param maxLength the maximum length of the text
     * @return the current instance of the text box
     */
    public S maxLength(int maxLength) {
        this.maxLength = maxLength;
        return self();
    }

    /**
     * Sets the blink interval for the cursor in milliseconds.
     *
     * @param ms the blink interval in milliseconds
     * @return the current instance of the text box
     */
    public S blinkInterval(int ms) {
        this.blinkInterval = ms;
        return self();
    }

    /**
     * Sets whether the text box should scroll when the text exceeds the visible area.
     * <p>
     * If disabled, the text will be clipped.
     *
     * @param scrolling true to enable scrolling, false to disable
     * @return the current instance of the text box
     */
    public S scrolling(boolean scrolling) {
        this.scrolling = scrolling;
        return self();
    }

    /**
     * Sets whether the text box should allow multi-line input.
     * <p>
     * If enabled, the text box will accept line breaks and display multiple lines of text.
     *
     * @param multiLine true to enable multi-line input, false to disable
     * @return the current instance of the text box
     */
    protected S multiLine(boolean multiLine) {
        this.multiLine = multiLine;
        return self();
    }

    /**
     * Sets a custom suggestion provider for the text box.
     * <p>
     * Suggestions are shown as the user types in the text box.
     *
     * @param provider the suggestion provider function to set
     * @return the current instance of the text box
     */
    public S suggest(Function<String, String> provider) {
        this.suggestionProvider = provider;
        return self();
    }

    /**
     * Suggests a list of strings based on the current input in the text box.
     * <p>
     * The first suggestion that starts with the current input will be shown.
     *
     * @param suggestions the list of suggestions to use
     * @return the current instance of the text box
     */
    public S suggest(List<String> suggestions) {
        return suggest(input -> {
            if (input.isEmpty()) return "";
            for (String suggestion : suggestions) {
                if (suggestion.startsWith(input)) {
                    return suggestion.substring(input.length());
                }
            }
            return "";
        });
    }

    /**
     * Suggests a list of strings based on the current input in the text box.
     *
     * @param suggestions the list of suggestions to use
     * @return the current instance of the text box
     */
    public S suggest(String... suggestions) {
        return suggest(List.of(suggestions));
    }

    /**
     * Sets a custom validator for the text box.
     * <p>
     * The validator is used to determine whether the current input is valid or not.
     *
     * @param validator the validator function to set
     * @return the current instance of the text box
     */
    public S validator(Predicate<String> validator) {
        this.validator = validator;
        return self();
    }

    /**
     * Whether there has been a recent invalid input in the text box.
     * (i.E: too long, invalid characters, etc.)
     *
     * @return true if there has been a recent invalid input, false otherwise
     */
    public boolean invalid() {
        return invalidInput || !validator.test(value);
    }

    /**
     * Sets whether the text box should allow input from the user.
     *
     * @param allow true to allow input, false to disable input
     * @return the current instance of the text box
     */
    public S allowInput(boolean allow) {
        this.allowInput = allow;
        return self();
    }

    /**
     * Sets a custom input filter for the text box.
     *
     * @param filter the input filter function to set
     * @return the current instance of the text box
     */
    public S filterInput(Function<String, String> filter) {
        this.inputFilter = filter;
        return self();
    }

    /**
     * Filters the input string before inserting it into the text box.
     *
     * @param input the input string to filter
     * @return the filtered string
     */
    protected String applyFilter(String input) {
        // Remove invalid characters
        //? if < 1.21 {
         /*input = SharedConstants.filterText(input, multiLine);
        *///?} elif >= 1.21 {
        input = StringUtil.filterText(input, multiLine);
        //?}
        // Apply custom input filter
        input = inputFilter.apply(input);
        // Limit to max length
        int currentLength = value.length();
        if (currentLength + input.length() > maxLength) {
            var allowed = maxLength - currentLength;
            if (allowed <= 0) {
                return "";
            }
            input = input.substring(0, allowed);
        }
        return input;
    }

    /**
     * Triggers the value changed event, notifying all registered listeners of the current value.
     */
    private void triggerValueChanged() {
        for (var listener : listeners) {
            listener.accept(value);
        }
    }

    /**
     * Whether the text box is currently focused, editable and listening for input.
     *
     * @return true if the text box is focused and editable, false otherwise
     */
    public boolean listening() {
        // TODO: Check if visible
        return !this.disabled() && this.focused() && allowInput;
    }

    /**
     * Handles cursor movement and updates the display position and suggestion accordingly.
     * <p>
     * Should be called whenever the cursor position changes.
     */
    protected void handleCursorMove() {
        updateDisplayPos();
        updateSuggestion();
    }

    /**
     * Calculates the display position of the text box based on the cursor position and the width of the text box.
     * <p>
     * It ensures that the cursor is always visible within the text box by adjusting the display position.
     */
    protected void updateDisplayPos() {
        int width = bounds().width() - padding().horizontal();
        width -= 4; // Padding for cursor

        // Cursor is left of the visible text
        if (cursorPos <= displayPos) {
            // Move cursor by half the width of the text box to the left
            var half = Util.font().plainSubstrByWidth(
                value.substring(0, cursorPos),
                width / 2
            );
            // Set displayPos to the start of the visible text
            displayPos = cursorPos - half.length();
            if (displayPos < 0) {
                displayPos = 0;
            }
            return;
        }

        // Get the visible text starting from displayPos
        String visible = Util.font().plainSubstrByWidth(
            value.substring(displayPos),
            width
        );
        int visibleEnd = displayPos + visible.length();

        // Cursor is right of the visible text
        if (cursorPos > visibleEnd) {
            displayPos += cursorPos - visibleEnd;
        }

        // Clamp displayPos to valid range
        displayPos = Mth.clamp(displayPos, 0, value.length());
    }

    /**
     * Updates the current suggestion based on the current value of the text box.
     */
    protected void updateSuggestion() {
        if (!listening()) {
            currentSuggestion = "";
            return;
        }
        // Update suggestion based on current value
        currentSuggestion = suggestionProvider.apply(value);
    }

    /**
     * Insert text at the current cursor position.
     *
     * @param string the text to insert
     */
    protected void insertText(String string) {
        if (!listening()) return;
        var filtered = applyFilter(string);
        // Update invalid state
        invalidInput = !string.equals(filtered);
        // Use filtered string
        string = filtered;

        if (highlightPos != cursorPos) {
            // Remove highlighted text before inserting
            int start = highlightStart();
            int end = highlightEnd();
            value = value.substring(0, start) + value.substring(end);
            cursorPos = start;
            highlightPos = cursorPos;
        }
        // Insert text
        value = value.substring(0, cursorPos) + string + value.substring(cursorPos);
        cursorPos = Mth.clamp(cursorPos + string.length(), 0, value.length());
        highlightPos = cursorPos;
        handleCursorMove();
        triggerValueChanged();
    }

    /**
     * Deletes text in the given direction (positive for forward, negative for backward)
     * If text is highlighted, it will be removed instead.
     *
     * @param direction the direction to delete in, positive or negative
     */
    protected void deleteText(int direction) {
        if (!listening()) return;
        if (direction == 0) return;
        // If text is highlighted, remove it instead
        if (highlightPos != cursorPos) {
            insertText("");
            return;
        }
        // Delete in direction
        if (direction > 0) {
            // Delete forward
            int end = Math.min(value.length(), cursorPos + direction);
            value = value.substring(0, cursorPos) + value.substring(end);
            // Cursor stays the same
        } else {
            // Delete backward
            int start = Math.max(0, cursorPos + direction);
            value = value.substring(0, start) + value.substring(cursorPos);
            cursorPos = start;
            highlightPos = cursorPos;
        }
        // Update invalid state
        invalidInput = value.length() > maxLength;

        handleCursorMove();
        triggerValueChanged();
    }

    /**
     * Gets the position of the next word in the given direction (positive for forward, negative for backward).
     *
     * @param direction the direction to move in, positive or negative
     * @return the position of the next word in the given direction
     */
    protected int wordPosition(int direction) {
        int pos = cursorPos;
        if (direction > 0) {
            // Skip current word
            while (pos < value.length() && !Character.isWhitespace(value.charAt(pos))) {
                pos++;
            }
            // Skip spaces
            while (pos < value.length() && Character.isWhitespace(value.charAt(pos))) {
                pos++;
            }
        } else {
            // Skip spaces
            while (pos > 0 && Character.isWhitespace(value.charAt(pos - 1))) {
                pos--;
            }
            // Skip word
            while (pos > 0 && !Character.isWhitespace(value.charAt(pos - 1))) {
                pos--;
            }
        }
        return pos;
    }

    /**
     * Moves the cursor to a valid position in the given direction
     * (positive for forward, negative for backward).
     *
     * @param direction the direction to move in, positive or negative
     */
    protected void moveCursor(int direction) {
        if (Screen.hasControlDown()) {
            // Move to next word
            cursorPos = wordPosition(direction);
        } else {
            // Move by one character
            cursorPos = Mth.clamp(cursorPos + direction, 0, value.length());
        }
        if (highlightPos != cursorPos) {
            // If highlight is active, move to highlight start or end
            if (direction > 0) {
                cursorPos = highlightEnd();
            } else {
                cursorPos = highlightStart();
            }
        }
        if (!Screen.hasShiftDown()) {
            highlightPos = cursorPos;
        }
        handleCursorMove();
    }

    /**
     * Handles key press events for the text box, including text editing and navigation.
     *
     * @param code the key code of the pressed key
     * @return true if the key press was handled, false otherwise
     */
    protected boolean handleKeyPress(int code) {
        if (Screen.isSelectAll(code)) {
            // Move cursor to the end
            cursorPos = value.length();
            highlightPos = 0; // Highlight from start to end
            handleCursorMove();
            return true;
        }
        if (Screen.isCopy(code)) {
            // Copy highlighted text to clipboard
            var keyboard = Minecraft.getInstance().keyboardHandler;
            keyboard.setClipboard(highlightedText());
            return true;
        }
        if (Screen.isPaste(code)) {
            // Paste text from clipboard
            var keyboard = Minecraft.getInstance().keyboardHandler;
            insertText(keyboard.getClipboard());
            return true;
        }
        if (Screen.isCut(code)) {
            // Copy highlighted text to clipboard and remove it from the value
            var keyboard = Minecraft.getInstance().keyboardHandler;
            keyboard.setClipboard(highlightedText());
            insertText("");
            return true;
        }
        // Other keys
        boolean processed = true;
        switch (code) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                deleteText(-1);
            }
            case GLFW.GLFW_KEY_DELETE -> {
                deleteText(1);
            }
            case GLFW.GLFW_KEY_LEFT -> {
                moveCursor(-1);
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                moveCursor(1);
            }
            case GLFW.GLFW_KEY_HOME -> {
                cursorPos = 0;
                if (!Screen.hasShiftDown()) {
                    highlightPos = cursorPos;
                }
            }
            case GLFW.GLFW_KEY_END -> {
                cursorPos = value.length();
                if (!Screen.hasShiftDown()) {
                    highlightPos = cursorPos;
                }
            }
            case GLFW.GLFW_KEY_TAB -> {
                // Apply suggestion
                if (!currentSuggestion.isEmpty()) {
                    insertText(currentSuggestion);
                }
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                if (multiLine) {
                    insertText("\n");
                }
            }
            default -> {
                processed = false;
            }
        }
        // Calculate display pos after cursor movement
        handleCursorMove();
        return processed;
    }

    /**
     * Returns the height of the text to be rendered in the text box.
     *
     * @return the height of the text in pixels
     */
    protected float textHeight() {
        return Util.font().lineHeight;
    }

    /**
     * Calculates the UI position where the text should be rendered within the text box.
     *
     * @return the global UI position of the text within the text box
     */
    protected Point textPosition() {
        var bounds = bounds();
        var x = bounds.x();
        var y = bounds.y();
        var padding = padding();
        // Align
        float textHeight = textHeight();
        float availableHeight = bounds.height() - padding.vertical();
        if (availableHeight > textHeight) {
            // Center vertically
            y += Mth.ceil((availableHeight - textHeight) / 2f);
        }
        // Add padding
        x += padding.left();
        y += padding.top();
        return new Point(x, y);
    }

    /**
     * Calculates the cursor position based on the mouse click within the text box.
     *
     * @param mouseX the x-coordinate of the mouse click
     * @return the cursor position corresponding to the mouse click
     */
    protected int cursorPosAt(int mouseX, int mouseY) {
        var textPos = textPosition();
        int x = mouseX - textPos.x();
        // Get the substring of the value starting from displayPos
        String visibleText = value.substring(displayPos);
        // Find the character index corresponding to the mouse click position
        int charIndex = Util.font().plainSubstrByWidth(visibleText, x).length();
        return displayPos + charIndex;
    }

    /**
     * Returns the text to be displayed in the text box,
     * either the current value or the placeholder if the value is empty.
     *
     * @return the text to be displayed in the text box
     */
    protected String textToShow() {
        return value.isEmpty() && !listening()
            ? placeholder
            : value;
    }

    /**
     * Gets the highlighted section of the text
     *
     * @return the highlighted text
     */
    protected String highlightedText() {
        int start = Math.min(cursorPos, highlightPos);
        int end = Math.max(cursorPos, highlightPos);
        return value.substring(start, end);
    }

    /**
     * Gets the start index of the highlighted section of the text
     *
     * @return the start index of the highlighted text
     */
    protected int highlightStart() {
        return Math.min(cursorPos, highlightPos);
    }

    /**
     * Gets the end index of the highlighted section of the text
     *
     * @return the end index of the highlighted text
     */
    protected int highlightEnd() {
        return Math.max(cursorPos, highlightPos);
    }

    /**
     * Whether the placeholder text should be visible
     *
     * @return true if the placeholder text should be visible, false otherwise
     */
    private boolean isPlaceholderVisible() {
        return value.isEmpty() && !placeholder.isEmpty() && !listening();
    }

    /**
     * Whether the cursor should be visible (blinking)
     *
     * @return true if the cursor should be visible, false otherwise
     */
    private boolean isCursorVisible() {
        // Blink every 6 ticks
        var time = System.currentTimeMillis();
        var blinking = time / blinkInterval % 2 == 0;
        return listening() && !blinking;
    }

    /**
     * Whether the highlight should be visible (if there is a selection)
     *
     * @return true if the highlight should be visible, false otherwise
     */
    private boolean isHighlightVisible() {
        return highlightPos != cursorPos;
    }

    @Override
    public void render(RenderContext context) {
        // Render with scissor enabled
        if (scrolling) {
            renderWithScissor(context, () -> renderTextField(context));
        } else {
            renderTextField(context);
        }
    }

    /**
     * Renders the text field, including the text, cursor, and highlight.
     *
     * @param context the render context
     */
    private void renderTextField(RenderContext context) {
        var graphics = context.graphics();
        var text = textToShow();

        if (scrolling) {
            // Transform by display pos
            int offset = context.font().width(text.substring(0, displayPos));
            graphics.pose().pushPose();
            graphics.pose().translate(-offset, 0, 0);
        }

        try {
            var textPos = textPosition();
            // Render text
            {
                var x = textPos.x();
                var y = textPos.y();
                var placeholder = isPlaceholderVisible();

                renderText(context, text, x, y, placeholder, currentSuggestion);
            }

            // Cursor
            if (isCursorVisible()) {
                boolean inline = cursorPos < text.length();
                var leftOfCursor = text.substring(0, cursorPos);

                var font = context.font();
                var x = textPos.x() + font.width(leftOfCursor);
                var y = textPos.y();
                renderCursor(context, x, y, inline);
            }

            // Highlight
            if (isHighlightVisible()) {
                // Calculate highlight start and end positions
                String before = text.substring(0, highlightStart());
                String highlighted = text.substring(highlightStart(), highlightEnd());

                var font = Util.font();

                var x = textPos.x() + font.width(before);
                var y = textPos.y();
                var width = font.width(highlighted);
                renderHighlight(context, x, y, width);
            }
        } finally {
            // Undo transform
            if (scrolling) {
                graphics.pose().popPose();
            }
        }

    }

    /**
     * Renders the given content with scisorr enabled.
     *
     * @param context the render context
     * @param render  the rendering logic
     */
    protected void renderWithScissor(RenderContext context, Runnable render) {
        var graphics = context.graphics();
        // Enable scissor
        var bounds = renderBounds();
        graphics.enableScissor(
            bounds.x() + 1, // For inline border
            bounds.y(),
            bounds.x() + bounds.width() - 1, // For inline border
            bounds.y() + bounds.height()
        );
        // Render
        render.run();
        // Disable scissor
        graphics.disableScissor();
    }

    /**
     * Renders the text within the text field.
     *
     * @param context     the render context
     * @param text        the text to render
     * @param x           the x-coordinate for rendering
     * @param y           the y-coordinate for rendering
     * @param placeholder whether the text is a placeholder
     * @param suggestion  the current suggestion to render (or empty)
     */
    protected abstract void renderText(
        RenderContext context,
        String text,
        int x,
        int y,
        boolean placeholder,
        String suggestion
    );

    /**
     * Renders the cursor at the specified position.
     *
     * @param context the render context
     * @param x       the x-coordinate for rendering the cursor
     * @param y       the y-coordinate for rendering the cursor
     * @param inline  whether the cursor is inline or not (whether it is within the text or at the end)
     */
    protected abstract void renderCursor(
        RenderContext context,
        int x,
        int y,
        boolean inline
    );

    /**
     * Renders the highlight for the selected text.
     *
     * @param context the render context
     * @param x       the x-coordinate for rendering the highlight
     * @param y       the y-coordinate for rendering the highlight
     * @param width   the width of the highlight
     */
    protected abstract void renderHighlight(
        RenderContext context,
        int x,
        int y,
        int width
    );
}
