package dev.tr7zw.trender.gui.widget;

import org.jetbrains.annotations.Nullable;

import dev.tr7zw.trender.gui.client.RenderContext;
import dev.tr7zw.trender.gui.client.ScreenDrawing;
import dev.tr7zw.trender.gui.impl.client.NarrationMessages;
import dev.tr7zw.trender.gui.impl.client.style.StyleConstants;
import dev.tr7zw.trender.gui.impl.client.style.WidgetTextures;
import dev.tr7zw.trender.gui.widget.data.HorizontalAlignment;
import dev.tr7zw.trender.gui.widget.data.InputResult;
import dev.tr7zw.trender.gui.widget.icon.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
//#if MC >= 11800
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//#endif
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class WButton extends WWidget {
    private static final int ICON_SPACING = 2;

    @Nullable
    private Component label;
    protected int color = StyleConstants.DEFAULT_TEXT_COLOR;
    /**
     * The size (width/height) of this button's icon in pixels.
     * 
     * @since 6.4.0
     */
    protected int iconSize = 16;
    private boolean enabled = true;
    protected HorizontalAlignment alignment = HorizontalAlignment.CENTER;

    @Nullable
    private Runnable onClick;
    @Nullable
    private Icon icon = null;

    /**
     * Constructs a button with no label and no icon.
     */
    public WButton() {

    }

    /**
     * Constructs a button with an icon.
     *
     * @param icon the icon
     * @since 2.2.0
     */
    public WButton(@Nullable Icon icon) {
        this.icon = icon;
    }

    /**
     * Constructs a button with a label.
     *
     * @param label the label
     */
    public WButton(@Nullable Component label) {
        this.label = label;
    }

    /**
     * Constructs a button with an icon and a label.
     *
     * @param icon  the icon
     * @param label the label
     * @since 2.2.0
     */
    public WButton(@Nullable Icon icon, @Nullable Component label) {
        this.icon = icon;
        this.label = label;
    }

    @Override
    public boolean canResize() {
        return true;
    }

    @Override
    public boolean canFocus() {
        return true;
    }

    @Override
    public void paint(RenderContext context, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isWithinBounds(mouseX, mouseY);
        var textures = WidgetTextures.getButtonTextures().get();
        context.blitSprite(textures.get(enabled, hovered || isFocused()), x, y, getWidth(), getHeight(), 20, 4, 200,
                20);

        if (icon != null) {
            icon.paint(context, x + ICON_SPACING, y + (getHeight() - iconSize) / 2, iconSize);
        }

        if (label != null) {
            int color = StyleConstants.BUTTON_TEXT_COLOR;
            if (!enabled) {
                color = StyleConstants.BUTTON_TEXT_COLOR_DISABLED;
            } /*
               * else if (hovered) { color = 0xFFFFA0; }
               */

            int xOffset = (icon != null && alignment == HorizontalAlignment.LEFT)
                    ? ICON_SPACING + iconSize + ICON_SPACING
                    : 0;
            // FIXME: WHY?
            //#if MC <= 12001
            //$$context.getPose().pushPose();
            //$$context.getPose().translate(0, 0, 300);
            //#endif
            ScreenDrawing.drawStringWithShadow(context, label.getVisualOrderText(), alignment, x + xOffset,
                    y + ((getHeight() - 8) / 2), getWidth(), color); //LibGuiClient.config.darkMode ? darkmodeColor : color);
            //#if MC <= 12001
            //$$context.getPose().popPose();
            //#endif
        }
    }

    @Override
    public InputResult onClick(int x, int y, int button) {
        super.onClick(x, y, button);

        if (enabled && isWithinBounds(x, y)) {
            Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            if (onClick != null)
                onClick.run();
            return InputResult.PROCESSED;
        }

        return InputResult.IGNORED;
    }

    @Override
    public InputResult onKeyPressed(int ch, int key, int modifiers) {
        if (isActivationKey(ch)) {
            onClick(0, 0, 0);
            return InputResult.PROCESSED;
        }

        return InputResult.IGNORED;
    }

    /**
     * Gets the click handler of this button.
     *
     * @return the click handler
     * @since 2.2.0
     */
    @Nullable
    public Runnable getOnClick() {
        return onClick;
    }

    /**
     * Sets the click handler of this button.
     *
     * @param onClick the new click handler
     * @return this button
     */
    public WButton setOnClick(@Nullable Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public WButton setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public @Nullable Component getLabel() {
        return label;
    }

    public WButton setLabel(Component label) {
        this.label = label;
        return this;
    }

    public HorizontalAlignment getAlignment() {
        return alignment;
    }

    public WButton setAlignment(HorizontalAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    /**
     * Gets the current height / width of the icon.
     *
     * @return the current height / width of the icon
     * @since 6.4.0
     */
    public int getIconSize() {
        return iconSize;
    }

    /**
     * Sets the new size of the icon.
     *
     * @param iconSize the new height and width of the icon
     * @return this button
     * @since 6.4.0
     */
    public WButton setIconSize(int iconSize) {
        this.iconSize = iconSize;
        return this;
    }

    /**
     * Gets the icon of this button.
     *
     * @return the icon
     * @since 2.2.0
     */
    @Nullable
    public Icon getIcon() {
        return icon;
    }

    /**
     * Sets the icon of this button.
     *
     * @param icon the new icon
     * @return this button
     * @since 2.2.0
     */
    public WButton setIcon(@Nullable Icon icon) {
        this.icon = icon;
        return this;
    }

    //#if MC >= 11800
    @Override
    public void addNarrations(NarrationElementOutput builder) {
        builder.add(NarratedElementType.TITLE, AbstractWidget.wrapDefaultNarrationMessage(getLabel()));

        if (isEnabled()) {
            if (isFocused()) {
                builder.add(NarratedElementType.USAGE, NarrationMessages.Vanilla.BUTTON_USAGE_FOCUSED);
            } else if (isHovered()) {
                builder.add(NarratedElementType.USAGE, NarrationMessages.Vanilla.BUTTON_USAGE_HOVERED);
            }
        }
    }
    //#endif
}
