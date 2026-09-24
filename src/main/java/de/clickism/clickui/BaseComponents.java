package de.clickism.clickui;

import de.clickism.clickui.elements.*;
import de.clickism.clickui.elements.input.Checkbox;
import de.clickism.clickui.elements.input.NumberField;
import de.clickism.clickui.elements.input.TextField;
import de.clickism.clickui.style.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A base interface providing methods for creating common UI components.
 * <p>
 * This interface can be implemented by classes that want to provide a convenient way
 * to create UI elements without needing to instantiate them directly.
 */
public interface BaseComponents {
    /**
     * Creates a new reference holder for a value of type T.
     *
     * @param <T> the type of the value to be held in the reference
     * @return a new Ref instance
     */
    default <T> SafeRef<T> ref() {
        return new SafeRef<>();
    }

    /**
     * Creates a new nullable reference holder for a value of type T.
     *
     * @param <T> the type of the value to be held in the reference
     * @return a new NullableRef instance
     */
    default <T> NullableRef<T> nullableRef() {
        return new NullableRef<>();
    }

    /**
     * Creates a new empty style.
     *
     * @return a new empty style instance
     */
    default Style style() {
        return Style.empty();
    }

    /**
     * Creates a new Box element.
     *
     * @return a new Box instance
     */
    default Box box() {
        return new Box();
    }

    /**
     * Creates a new Grid element with the specified number of columns.
     *
     * @param columns the number of columns in the grid
     * @return a new Grid instance
     */
    default Grid grid(int columns) {
        return new Grid(columns);
    }

    /**
     * Creates a new Button element with the specified label.
     *
     * @param label the label of the button
     * @return a new Button instance
     */
    default Button button(Component label) {
        return new Button(label);
    }

    /**
     * Creates a new Button element with the specified label.
     *
     * @param label the label of the button
     * @return a new Button instance
     */
    default Button button(String label) {
        return new Button(Component.literal(label));
    }

    /**
     * Creates a new Text element with the specified text.
     *
     * @param text the text to display
     * @return a new Text instance
     */
    default Text text(String text) {
        return new Text(Component.literal(text));
    }

    /**
     * Creates a new Text element with the specified text component.
     *
     * @param text the text component to display
     * @return a new Text instance
     */
    default Text text(Component text) {
        return new Text(text);
    }

    /**
     * Creates a new Header element with the given title and level 1.
     *
     * @param title the title of the header
     * @return a new Header instance with level 1
     */
    default Header h1(String title) {
        return new Header(Component.literal(title), 1);
    }

    /**
     * Creates a new Header element with the given title and level 1.
     *
     * @param title the title of the header
     * @return a new Header instance with level 1
     */
    default Header h1(Component title) {
        return new Header(title, 1);
    }

    /**
     * Creates a new Header element with the given title and level 2.
     *
     * @param title the title of the header
     * @return a new Header instance with level 2
     */
    default Header h2(String title) {
        return new Header(Component.literal(title), 2);
    }

    /**
     * Creates a new Header element with the given title and level 2.
     *
     * @param title the title of the header
     * @return a new Header instance with level 2
     */
    default Header h2(Component title) {
        return new Header(title, 2);
    }

    /**
     * Creates a new Header element with the given title and level 3.
     *
     * @param title the title of the header
     * @return a new Header instance with level 3
     */
    default Header h3(String title) {
        return new Header(Component.literal(title), 3);
    }

    /**
     * Creates a new Header element with the given title and level 3.
     *
     * @param title the title of the header
     * @return a new Header instance with level 3
     */
    default Header h3(Component title) {
        return new Header(title, 3);
    }

    /**
     * Creates a new Header element with the given title and level 4.
     *
     * @param title the title of the header
     * @return a new Header instance with level 4
     */
    default Header h4(String title) {
        return new Header(Component.literal(title), 4);
    }

    /**
     * Creates a new Header element with the given title and level 4.
     *
     * @param title the title of the header
     * @return a new Header instance with level 4
     */
    default Header h4(Component title) {
        return new Header(title, 4);
    }

    /**
     * Creates a new Header element with the given title and level 5.
     *
     * @param title the title of the header
     * @return a new Header instance with level 5
     */
    default Header h5(String title) {
        return new Header(Component.literal(title), 5);
    }

    /**
     * Creates a new Header element with the given title and level 5.
     *
     * @param title the title of the header
     * @return a new Header instance with level 5
     */
    default Header h5(Component title) {
        return new Header(title, 5);
    }

    /**
     * Creates a new Header element with the given title and level 6.
     *
     * @param title the title of the header
     * @return a new Header instance with level 6
     */
    default Header h6(String title) {
        return new Header(Component.literal(title), 6);
    }

    /**
     * Creates a new Header element with the given title and level 6.
     *
     * @param title the title of the header
     * @return a new Header instance with level 6
     */
    default Header h6(Component title) {
        return new Header(title, 6);
    }

    /**
     * Creates a new TextField element.
     *
     * @return a new TextField instance
     */
    default TextField textField() {
        return new TextField();
    }

    /**
     * Creates a new TextField element with the specified placeholder text.
     *
     * @param placeholder the placeholder text to display in the TextField
     * @return a new TextField instance with the specified placeholder
     */
    default TextField textField(String placeholder) {
        return new TextField().placeholder(placeholder);
    }

    /**
     * Creates a new NumberField element.
     *
     * @return a new NumberField instance
     */
    default NumberField numberField() {
        return new NumberField();
    }

    /**
     * Creates a new NumberField element with the specified placeholder text.
     *
     * @param placeholder the placeholder text to display in the NumberField
     * @return a new NumberField instance with the specified placeholder
     */
    default NumberField numberField(String placeholder) {
        return new NumberField().placeholder(placeholder);
    }

    /**
     * Creates a new Image element with the specified resource location, width, and height.
     *
     * @param location the resource location of the image
     * @param width    the width of the image
     * @param height   the height of the image
     * @return a new Image instance
     */
    default Image image(Identifier location, int width, int height) {
        return new Image(location, width, height);
    }

    /**
     * Creates a new Checkbox component.
     *
     * @return a new Checkbox instance
     */
    default Checkbox checkbox() {
        return new Checkbox();
    }

    /**
     * Creates a new Checkbox component with the specified checked state.
     *
     * @param checked the initial checked state of the checkbox
     * @return a new Checkbox instance with the specified checked state
     */
    default Checkbox checkbox(boolean checked) {
        return new Checkbox().checked(checked);
    }
}
