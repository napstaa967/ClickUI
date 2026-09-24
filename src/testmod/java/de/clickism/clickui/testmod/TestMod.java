package de.clickism.clickui.testmod;

import com.mojang.blaze3d.platform.InputConstants;
import de.clickism.clickui.BaseComponents;
import de.clickism.clickui.SafeRef;
import de.clickism.clickui.UiColor;
import de.clickism.clickui.UiScreen;
import de.clickism.clickui.elements.input.NumberField;
import de.clickism.clickui.layout.Align;import de.clickism.clickui.style.Border;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if < 26.1
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//? if >= 26.1
//import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?} elif forge {
//import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.event.TickEvent;
//?} elif neoforge {
/*import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.IEventBus;
*///?}

//? if fabric {
public class TestMod implements ClientModInitializer, BaseComponents {
 //?} elif forge {
/*@Mod("clickuitestmod")
public class TestMod implements BaseComponents {
 *///?} elif neoforge {
/*@Mod("clickuitestmod")
public class TestMod implements BaseComponents {
*///?}

    //? if >= 26.1
    //public static final KeyMapping.Category CLICKUITESTMOD = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("clickui","testmod"));
    private static KeyMapping openMenuKey;

    //? if fabric {
    @Override
    public void onInitializeClient() {
        //? if < 26.1
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        //? if >= 26.1
        //openMenuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.clickuitestmod.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            //? if < 26.1
            "category.clickuitestmod"
            //? if >= 26.1
            //CLICKUITESTMOD
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.consumeClick()) {
                this.openTestScreen();
            }
        });
    }
    //?} else {

    /*public TestMod(/^? if neoforge {^//^IEventBus modBus^//^?}^/) {
        openMenuKey = new KeyMapping(
            "key.clickuitestmod.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.clickuitestmod"
        );
        //? if forge
        /^MinecraftForge.EVENT_BUS.register(this);^/
        //? if neoforge {
        /^NeoForge.EVENT_BUS.register(this);
        modBus.addListener(this::registerKeys);
        ^///?}
    }

    public void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(openMenuKey);
    }

    @SubscribeEvent
    public void onClientTick(/^? if forge {^//^TickEvent.ClientTickEvent^//^?} elif neoforge {^/ /^PlayerTickEvent.Post ^//^?}^/ event) {
        while (openMenuKey.consumeClick()) {
            this.openTestScreen();
        }
    }
    *///?}

    private void openTestScreen() {
        var screen = UiScreen.asScreen(box()
            .vertical()
            .childGap(10)
            .padding(30)
            .children(
                text("Hello, this is a test screen!")
                    .padding(20)
                    .style(style()
                        .fontScale(2.0f)
                        .backgroundColor(UiColor.BLUE)
                        .borderColor(UiColor.YELLOW)
                        .borderWidth(5)),
                text(Component.literal("Whaaat?").withStyle(ChatFormatting.BOLD)),
                button("Click me!")
                    .width(200)
                    .disabled(true)
                    .onClick(event -> {
                        event.player().sendSystemMessage(Component.literal("Button clicked!"));
                    })
                    .onRelease(event -> {
                        event.player().sendSystemMessage(Component.literal("Button released!"));
                    })
                    .onKeyPress(event -> {
                        event.player().sendSystemMessage(Component.literal("Key pressed on button!"));
                    }),
                box()
                    .height(200)
                    .vertical()
                    .style(style()
                        .backgroundColor(UiColor.GREEN)
                        .alpha(0.5f))
                    .children(
                        text("This is a box")
                            .padding(10),
                        button("I don't feel like I fit in")
                            .width(50),
                        button("I do though"),
                        button("Click me too!")
                            .padding(20)
                    )
            )
        );

        SafeRef<NumberField> numberRef = ref();

        var newScreen = UiScreen.asScreen(box()
            .alignCenter()
            .grow()
            .style(style()
                .backgroundColor(UiColor.WHITE_A50)
                .alpha(0.5f))
            .children(
                box()
                    .height(300)
                    .width(800)
                    .style(style()
                        .borderColor(UiColor.LIGHT_GRAY)
                        .backgroundColor(UiColor.BLACK)
                        .alpha(0.5f))
                    .padding(8)
                    .childGap(8)
                    .scrollable(true)
                    .children(
                        h1("New Screen!"),
                        text("There are some important info here!"),
                        textField()
                            .height(40),
                        h3("For example:"),
                        box()
                            .scrollable(true)
                            .crossAlign(Align.CENTER)
                            .width(300)
                            .padding(16)
                            .height(100)
                            .childGap(10)
                            .style(style().borderColor(UiColor.GREEN))
                            .children(
                                text("Scrollable content line 1"),
                                textField(),
                                text("Scrollable content line 2"),
                                text("Scrollable content line 3"),
                                text("Scrollable content line 4"),
                                textField(),
                                button("Drag me!")
                                    .onDragStart(event -> {
                                        event.player().sendSystemMessage(Component.literal("Drag started!"));
                                    })
                                    .onDragEnd(event -> {
                                        event.player().sendSystemMessage(Component.literal("Drag ended!"));
                                    })
                                    .onDrag(event -> {
                                        event.player().sendSystemMessage(Component.literal("Dragging!"));
                                    }),
                                text("Scrollable content line 5"),
                                text("Scrollable content line 6"),
                                text("Scrollable content line 7"),
                                button("Whaat?"),
                                text("Scrollable content line 8"),
                                text("Scrollable content line 9"),
                                button("Far down!?"),
                                // Test borders
                                box()
                                    .width(50)
                                    .height(50)
                                    .style(style()
                                        .backgroundColor(UiColor.WHITE_A30)
                                        .whenHovered(style()
                                            .borderPosition(Border.Position.CENTER)
                                            .borderColor(UiColor.RED)
                                            .borderWidth(3))),

                                box()
                                    .width(50)
                                    .height(50)
                                    .style(style()
                                        .backgroundColor(UiColor.WHITE_A30)
                                        .whenHovered(style()
                                            .borderPosition(Border.Position.INSIDE)
                                            .borderColor(UiColor.BLUE))),

                                box()
                                    .width(50)
                                    .height(50)
                                    .style(style()
                                        .backgroundColor(UiColor.WHITE_A30)),

                                // Test headers, random text
                                box().children(
                                    h1("Header 1"),
                                    text("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."),
                                    h2("Header 2"),
                                    text("Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."),
                                    h3("Header 3"),
                                    text("Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur."),
                                    h4("Header 4"),
                                    text("Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."),
                                    h5("Header 5"),
                                    text("Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo."),
                                    h6("Header 6"),
                                    text("Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem.")
                                )
                            ),
                        text("You can go back to the previous screen by clicking the button below. Alternatively, you can also press the §lESC §rkey to go back.")
                            .tooltip("Test boksdjl fdsjkl jfslkd jfklsddfgdfgdfgdfgfdsfsdj jk jdfkl sjkl fsd kfds kljfsl kjfkl sdj klsjdkf jsdlk jfklsdl")
                            .alignTextCenter()
                            .padding(5),
                        new Counter()
                            .onKeyPress(event -> {
                                event.player().sendSystemMessage(Component.literal("Counter key pressed!"));
                            }),
                        numberField("Type something...")
                            .ref(numberRef)
                            .tooltip(box()
                                .size(20)
                                .style(style()
                                    .backgroundColor(UiColor.YELLOW)
                                    .borderColor(UiColor.RED)))
                            .maxLength(32)
                            .highlightInvalid(true)
                            .suggest("hello", "bye", "heat"),
                        checkbox()
                            .size(10),
                        button("Print Number")
                            .tooltip(new Counter())
                            .onClick(event -> {
                                var number = numberRef.get().doubleValue();
                                event.player().sendSystemMessage(Component.literal("Number: " + number));
                            }),
                        box()
                            .size(20)
                            .style(style()
                                .backgroundColor(UiColor.BLACK)
                                .whenHovered(style()
                                    .borderWidth(3)
                                    .borderColor(UiColor.WHITE_A50))),
                        button("Go back but very long so the text should be scrolling! So let's see if it actually does that")
                            .width(150)
                            .tooltip("Click to go back to the previous screen")
                            .onClick(event -> {
                                event.screen().close();
                            })
                    )
            )
        );

        newScreen
            .debug(false)
            .open();
    }
}