package dev.tr7zw.trender.gui.client;

import java.util.EnumMap;
import java.util.Objects;
import java.util.function.Consumer;

import dev.tr7zw.trender.gui.impl.LibGuiCommon;
import dev.tr7zw.trender.gui.impl.client.style.GuiStyle;
import dev.tr7zw.trender.gui.impl.client.style.TextureConstants;
import dev.tr7zw.trender.gui.impl.client.style.WidgetTextures;
import dev.tr7zw.trender.gui.impl.client.style.TextureConstants.SpriteData;
import dev.tr7zw.trender.gui.widget.WItemSlot;
import dev.tr7zw.trender.gui.widget.WWidget;
//import juuxel.libninepatch.NinePatch;
//import juuxel.libninepatch.TextureRegion;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Background painters are used to paint the background of a widget. The
 * background painter instance of a widget can be changed to customize the look
 * of a widget.
 */
@FunctionalInterface
public interface BackgroundPainter {
    /**
     * Paint the specified panel to the screen.
     *
     * @param context The draw context
     * @param left    The absolute position of the left of the panel, in gui-screen
     *                coordinates
     * @param top     The absolute position of the top of the panel, in gui-screen
     *                coordinates
     * @param panel   The panel being painted
     */
    public void paintBackground(RenderContext context, int left, int top, WWidget panel);

    /**
     * The {@code VANILLA} background painter draws a vanilla-like GUI panel using
     * nine-patch textures.
     *
     * <p>
     * This background painter uses
     * {@code libgui:textures/gui/sprites/widget/panel_light.png} as the light
     * texture and {@code libgui:textures/gui/sprites/widget/panel_dark.png} as the
     * dark texture.
     *
     * <p>
     * This background painter is the default painter for root panels. * You can
     * override {@link dev.tr7zw.trender.gui.GuiDescription#addPainters()} to
     * customize the painter yourself.
     *
     * @since 1.5.0
     */
    public static BackgroundPainter VANILLA = createStyleVariants("widget/panel_", p -> {
    });

    /**
     * The {@code SLOT} background painter draws item slots or slot-like widgets.
     *
     * <p>
     * For {@linkplain WItemSlot item slots}, this painter uses
     * {@link WItemSlot#SLOT_TEXTURE libgui:textures/widget/item_slot.png}.
     */
    public static BackgroundPainter SLOT = (context, left, top, panel) -> {
        if (!(panel instanceof WItemSlot slot)) {
            ScreenDrawing.drawBeveledPanel(context, left - 1, top - 1, panel.getWidth() + 2, panel.getHeight() + 2,
                    0xB8000000, 0x4C000000, 0xB8FFFFFF);
        } else {
            for (int x = 0; x < slot.getWidth() / 18; ++x) {
                for (int y = 0; y < slot.getHeight() / 18; ++y) {
                    int index = x + y * (slot.getWidth() / 18);
                    float px = 1 / 64f;
                    if (slot.isBigSlot()) {
                        int sx = (x * 18) + left - 4;
                        int sy = (y * 18) + top - 4;
                        ScreenDrawing.texturedRect(context, sx, sy, 26, 26, WItemSlot.SLOT_TEXTURE, 18 * px, 0, 44 * px,
                                26 * px, 0xFF_FFFFFF);
                        if (slot.getFocusedSlot() == index) {
                            ScreenDrawing.texturedRect(context, sx, sy, 26, 26, WItemSlot.SLOT_TEXTURE, 18 * px,
                                    26 * px, 44 * px, 52 * px, 0xFF_FFFFFF);
                        }
                    } else {
                        int sx = (x * 18) + left;
                        int sy = (y * 18) + top;
                        ScreenDrawing.texturedRect(context, sx, sy, 18, 18, WItemSlot.SLOT_TEXTURE, 0, 0, 18 * px,
                                18 * px, 0xFF_FFFFFF);
                        if (slot.getFocusedSlot() == index) {
                            ScreenDrawing.texturedRect(context, sx, sy, 18, 18, WItemSlot.SLOT_TEXTURE, 0, 26 * px,
                                    18 * px, 44 * px, 0xFF_FFFFFF);
                        }
                    }
                }
            }
        }
    };

    /**
     * Creates a colorful gui panel painter. This painter paints the panel using the
     * specified color.
     *
     * @param panelColor the panel background color
     * @return a colorful gui panel painter
     * @see ScreenDrawing#drawGuiPanel(GuiGraphics, int, int, int, int, int)
     */
    public static BackgroundPainter createColorful(int panelColor) {
        return (context, left, top, panel) -> {
            ScreenDrawing.drawGuiPanel(context, left, top, panel.getWidth(), panel.getHeight(), panelColor);
        };
    }

    /**
     * Creates a colorful gui panel painter that has a custom contrast between the
     * shadows and highlights.
     *
     * @param panelColor the panel background color
     * @param contrast   the contrast between the shadows and highlights
     * @return a colorful gui panel painter
     */
    public static BackgroundPainter createColorful(int panelColor, float contrast) {
        return (context, left, top, panel) -> {
            int shadowColor = ScreenDrawing.multiplyColor(panelColor, 1.0f - contrast);
            int hilightColor = ScreenDrawing.multiplyColor(panelColor, 1.0f + contrast);

            ScreenDrawing.drawGuiPanel(context, left, top, panel.getWidth(), panel.getHeight(), shadowColor, panelColor,
                    hilightColor, 0xFF000000);
        };
    }

    /**
     * Creates a new nine-patch background painter.
     *
     * <p>
     * The resulting painter has a corner size of 4 px and a corner UV of 0.25.
     *
     * @param texture the background painter texture
     * @return a new nine-patch background painter
     * @since 1.5.0
     * @see NinePatchBackgroundPainter
     */
    public static NinePatchBackgroundPainter createNinePatch(ResourceLocation texture) {
        return new NinePatchBackgroundPainter(texture);
        //return createNinePatch(new Texture(texture), builder -> builder.cornerSize(4).cornerUv(0.25f));
    }

    /**
     * Creates a new nine-patch background painter with a custom configuration.
     *
     * <p>
     * This method cannot be used for {@linkplain Texture.Type#GUI_SPRITE GUI
     * sprites}. Instead, you can use the vanilla nine-slice mechanism or use a
     * standalone texture referring to the same file.
     *
     * @param texture      the background painter texture
     * @param configurator a consumer that configures the {@link NinePatch.Builder}
     * @return the created nine-patch background painter
     * @since 4.0.0
     * @see NinePatch
     * @see NinePatch.Builder
     * @see NinePatchBackgroundPainter
     * @throws IllegalArgumentException when the texture is not
     *                                  {@linkplain Texture.Type#STANDALONE
     *                                  standalone}
     */
    //    public static NinePatchBackgroundPainter createNinePatch(Texture texture,
    //            Consumer<NinePatch.Builder<ResourceLocation>> configurator) {
    //        if (texture.type() != Texture.Type.STANDALONE) {
    //            throw new IllegalArgumentException("Non-standalone texture " + texture + " cannot be used for nine-patch");
    //        }
    //
    //        TextureRegion<ResourceLocation> region = new TextureRegion<>(texture.image(), texture.u1(), texture.v1(),
    //                texture.u2(), texture.v2());
    //        var builder = NinePatch.builder(region);
    //        configurator.accept(builder);
    //        return new NinePatchBackgroundPainter(builder.build());
    //    }

    /**
     * Creates a background painter that uses either the {@code light} or the
     * {@code dark} background painter depending on the
     * {@linkplain WWidget#shouldRenderInDarkMode current setting}.
     *
     * @param light the light mode background painter
     * @param dark  the dark mode background painter
     * @return a new background painter that chooses between the two inputs
     * @since 1.5.0
     */
    public static BackgroundPainter createStyleVariants(String prefix, Consumer<BackgroundPainter> configurator) {
        EnumMap<GuiStyle, BackgroundPainter> styleMap = new EnumMap<>(GuiStyle.class);

        for (GuiStyle style : GuiStyle.values()) {
            BackgroundPainter painter = createGuiSprite(WidgetTextures.getId(prefix + style.getPrefix()));
            configurator.accept(painter);
            styleMap.put(style, painter);
        }

        return (context, left, top, panel) -> {
            styleMap.get(LibGui.getGuiStyle()).paintBackground(context, left, top, panel);
        };
    }

    public static BackgroundPainter createStyleVariantsNinePatch(String prefix,
            Consumer<NinePatchBackgroundPainter> configurator) {
        EnumMap<GuiStyle, BackgroundPainter> styleMap = new EnumMap<>(GuiStyle.class);

        for (GuiStyle style : GuiStyle.values()) {
            NinePatchBackgroundPainter painter = createNinePatch(LibGuiCommon.id(prefix + style.getPrefix() + ".png"));
            configurator.accept(painter);
            styleMap.put(style, painter);
        }

        return (context, left, top, panel) -> {
            styleMap.get(LibGui.getGuiStyle()).paintBackground(context, left, top, panel);
        };
    }

    /**
     * Creates a background painter that uses a texture from the GUI atlas.
     *
     * <p>
     * This method can be used to draw tiled or nine-slice GUI sprites from resource
     * packs as a simpler and more data-driven alternative to
     * {@link #createNinePatch(ResourceLocation)}.
     *
     * @param texture the texture ID
     * @return a new background painter that uses a GUI sprite
     * @since 9.0.0
     */
    static BackgroundPainter createGuiSprite(ResourceLocation texture) {
        Objects.requireNonNull(texture, "Texture cannot be null");
        return (context, left, top, panel) -> {
            //#if MC < 12100
            //$$ com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            //#endif
            SpriteData data = TextureConstants.get(texture);
            context.blitSprite(texture, left, top, panel.getWidth(), panel.getHeight(), data.border(), data.border(),
                    data.width(), data.height());
        };
    }
}
