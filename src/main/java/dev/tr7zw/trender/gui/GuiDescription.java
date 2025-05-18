package dev.tr7zw.trender.gui;

import net.minecraft.world.inventory.ContainerData;

import org.jetbrains.annotations.Nullable;

import dev.tr7zw.trender.gui.client.LibGui;
import dev.tr7zw.trender.gui.impl.client.config.GuiStyle;
import dev.tr7zw.trender.gui.widget.WPanel;
import dev.tr7zw.trender.gui.widget.WWidget;
import dev.tr7zw.trender.gui.widget.data.HorizontalAlignment;
import dev.tr7zw.trender.gui.widget.data.Vec2i;

/**
 * A GUI description represents a GUI without depending on screens.
 *
 * <p>
 * GUI descriptions contain the root panel and the property delegate of the GUI.
 * They also manage the focused widget.
 *
 * @see dev.tr7zw.trender.gui.client.LightweightGuiDescription
 * @see SyncedGuiDescription
 */
public interface GuiDescription {
    public WPanel getRootPanel();

    public int getTitleColor();

    public GuiDescription setRootPanel(WPanel panel);

    /**
     * Sets the title color of this GUI.
     *
     * <p>
     * The dark-mode title color will also be set by this method. If the specified
     * color is {@link dev.tr7zw.trender.gui.widget.WLabel#DEFAULT_TEXT_COLOR}, the
     * dark-mode color will be
     * {@link dev.tr7zw.trender.gui.widget.WLabel#DEFAULT_DARKMODE_TEXT_COLOR};
     * otherwise it will be the specified color.
     *
     * @param color the new title color
     * @return this GUI
     */
    public GuiDescription setTitleColor(int color);

    /** Sets the object which manages the integer properties used by WBars */
    public GuiDescription setPropertyDelegate(ContainerData delegate);

    /**
     * Typical users won't call this. This adds a Slot to Container/Controller-based
     * guis, and does nothing on lightweight guis.
     */
    public void addSlotPeer(ValidatedSlot slot);

    /**
     * Guis should use this method to add clientside styles and BackgroundPainters
     * to their controls
     */

    public void addPainters();

    /**
     * Tests whether the root panel should get a
     * {@linkplain dev.tr7zw.trender.gui.client.BackgroundPainter#VANILLA vanilla
     * background} in the default {@link #addPainters} implementations.
     *
     * @return {@code true} if the default background painter is applied,
     *         {@code false} otherwise
     * @since 12.0.0
     */
    public boolean getUseDefaultRootBackground();

    /**
     * Enables or disables the default
     * {@linkplain dev.tr7zw.trender.gui.client.BackgroundPainter#VANILLA vanilla
     * background} for root panels.
     *
     * @param useDefaultRootBackground {@code true} if the default background
     *                                 painter is applied, {@code false} otherwise
     * @see #getUseDefaultRootBackground
     * @since 12.0.0
     */
    public void setUseDefaultRootBackground(boolean useDefaultRootBackground);

    /**
     * Gets the object which manages the integer properties used by WBars and such.
     */
    @Nullable
    public ContainerData getPropertyDelegate();

    /** Tests whether the widget is the currently-focused one. */
    public boolean isFocused(WWidget widget);

    /** Gets the currently-focused WWidget. May be null. */
    @Nullable
    public WWidget getFocus();

    /** Notifies this gui that the widget wants to acquire focus. */
    public void requestFocus(WWidget widget);

    /** Notifies this gui that the widget wants to give up its hold over focus. */
    public void releaseFocus(WWidget widget);

    /**
     * Gets whether this GUI is fullscreen.
     *
     * <p>
     * Fullscreen GUIs have no default background painter and have the root panel
     * stretched to fit the entire screen on the client.
     *
     * @return true if this GUI is fullscreen, false otherwise
     * @since 2.0.0
     */
    boolean isFullscreen();

    /**
     * Sets whether this GUI is fullscreen.
     *
     * @param fullscreen true if this GUI is fullscreen, false otherwise
     * @since 2.0.0
     */
    void setFullscreen(boolean fullscreen);

    /**
     * Gets whether the title of this GUI should be rendered by the screen.
     *
     * <p>
     * Modders can disable this to render the title themselves with a widget.
     *
     * @return true if the title is visible, false otherwise
     * @since 2.0.0
     */
    boolean isTitleVisible();

    /**
     * Sets whether the title of this GUI should be rendered by the screen.
     *
     * @param titleVisible true if the title is visible, false otherwise
     * @since 2.0.0
     */
    void setTitleVisible(boolean titleVisible);

    /**
     * Gets the horizontal alignment of the GUI title.
     *
     * @return the alignment
     * @since 2.1.0
     */
    HorizontalAlignment getTitleAlignment();

    /**
     * Sets the horizontal alignment of the GUI title.
     *
     * @param alignment the new alignment
     * @since 2.1.0
     */
    void setTitleAlignment(HorizontalAlignment alignment);

    /**
     * Gets the position of the screen title.
     *
     * @return the title position
     * @since 4.0.0
     */
    Vec2i getTitlePos();

    /**
     * Sets the position of the screen title.
     *
     * @param titlePos the new title position
     * @since 4.0.0
     */
    void setTitlePos(Vec2i titlePos);

    /**
     * Gets the style of this GUI.
     *
     *
     * @return the style
     */
    default GuiStyle getStyle() {
        return LibGui.getGuiStyle();
    }
}
