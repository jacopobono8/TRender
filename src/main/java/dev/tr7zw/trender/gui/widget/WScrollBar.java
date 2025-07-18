package dev.tr7zw.trender.gui.widget;

import dev.tr7zw.trender.gui.client.RenderContext;
import dev.tr7zw.trender.gui.impl.client.NarrationMessages;
import dev.tr7zw.trender.gui.impl.client.style.WidgetTextures;
import dev.tr7zw.trender.gui.widget.data.Axis;
import dev.tr7zw.trender.gui.widget.data.InputResult;
//#if MC >= 11800
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//#endif
import net.minecraft.resources.ResourceLocation;

public class WScrollBar extends WWidget {

    /**
     * The default {@linkplain #getScrollingSpeed() scrolling speed for mouse
     * inputs}.
     *
     * @since 9.0.0
     */
    public static final int DEFAULT_SCROLLING_SPEED = 4;
    private int scrollingSpeed = DEFAULT_SCROLLING_SPEED;

    protected Axis axis = Axis.HORIZONTAL;
    protected int value;
    protected int maxValue = 100;
    protected int window = 16;

    protected int anchor = -1;
    protected int anchorValue = -1;
    protected boolean sliding = false;

    /**
     * Constructs a horizontal scroll bar.
     */
    public WScrollBar() {
    }

    /**
     * Constructs a scroll bar with a custom axis.
     *
     * @param axis the axis
     */
    public WScrollBar(Axis axis) {
        this.axis = axis;
    }

    @Override
    public void paint(RenderContext context, int x, int y, int mouseX, int mouseY) {
        var textures = WidgetTextures.getScrollBarTextures().get();

        context.blitSprite(textures.background(), x, y, getWidth(), getHeight(), 4, 4, 16, 16);

        ResourceLocation thumbTexture = textures.thumb();

        if (maxValue <= 0)
            return;

        if (sliding) {
            thumbTexture = textures.thumbPressed();
        } else if (isWithinBounds(mouseX, mouseY)) {
            thumbTexture = textures.thumbHovered();
        }

        context.pushPose();

        if (axis == Axis.HORIZONTAL) {
            context.translate(x + 1 + getHandlePosition(), y + 1);
            context.blitSprite(thumbTexture, 0, 0, getHandleSize(), getHeight() - 2, 4, 4, 16, 16);

            if (isFocused()) {
                context.blitSprite(textures.thumbHovered(), 0, 0, getHandleSize(), getHeight() - 2, 4, 4, 16, 16);
            }
        } else {
            context.translate(x + 1, y + 1 + getHandlePosition());
            context.blitSprite(thumbTexture, 0, 0, getWidth() - 2, getHandleSize(), 4, 4, 16, 16);

            if (isFocused()) {
                context.blitSprite(textures.thumbHovered(), 0, 0, getWidth() - 2, getHandleSize(), 4, 4, 16, 16);
            }
        }

        context.popPose();
    }

    @Override
    public boolean canResize() {
        return true;
    }

    @Override
    public boolean canFocus() {
        return true;
    }

    /**
     * Gets the on-axis size of the scrollbar handle in gui pixels
     */
    public int getHandleSize() {
        float percentage = (window >= maxValue) ? 1f : window / (float) maxValue;
        int bar = (axis == Axis.HORIZONTAL) ? getWidth() - 2 : getHeight() - 2;
        int result = (int) (percentage * bar);
        if (result < 6)
            result = 6;
        return result;
    }

    /**
     * Gets the number of pixels the scrollbar handle is able to move along its
     * track from one end to the other.
     */
    public int getMovableDistance() {
        int bar = (axis == Axis.HORIZONTAL) ? getWidth() - 2 : getHeight() - 2;
        return bar - getHandleSize();
    }

    public int pixelsToValues(int pixels) {
        int bar = getMovableDistance();
        float percent = pixels / (float) bar;
        return (int) (percent * (maxValue - window));
    }

    public int getHandlePosition() {
        float percent = value / (float) Math.max(maxValue - window, 1);
        return (int) (percent * getMovableDistance());
    }

    /**
     * Gets the maximum scroll value achievable; this will typically be the maximum
     * value minus the window size
     */
    public int getMaxScrollValue() {
        return maxValue - window;
    }

    protected void adjustSlider(int x, int y) {

        int delta = 0;
        if (axis == Axis.HORIZONTAL) {
            delta = x - anchor;
        } else {
            delta = y - anchor;
        }

        int valueDelta = pixelsToValues(delta);
        int valueNew = anchorValue + valueDelta;

        if (valueNew > getMaxScrollValue())
            valueNew = getMaxScrollValue();
        if (valueNew < 0)
            valueNew = 0;
        this.value = valueNew;
    }

    @Override
    public InputResult onMouseDown(int x, int y, int button) {
        //TODO: Clicking before or after the handle should jump instead of scrolling
        requestFocus();

        if (axis == Axis.HORIZONTAL) {
            anchor = x;
            anchorValue = value;
        } else {
            anchor = y;
            anchorValue = value;
        }
        sliding = true;
        return InputResult.PROCESSED;
    }

    @Override
    public InputResult onMouseDrag(int x, int y, int button, double deltaX, double deltaY) {
        adjustSlider(x, y);
        return InputResult.PROCESSED;
    }

    @Override
    public InputResult onMouseUp(int x, int y, int button) {
        //TODO: Clicking before or after the handle should jump instead of scrolling
        anchor = -1;
        anchorValue = -1;
        sliding = false;
        return InputResult.PROCESSED;
    }

    @Override
    public InputResult onKeyPressed(int ch, int key, int modifiers) {
        WAbstractIntSlider.Direction direction = axis == Axis.HORIZONTAL ? WAbstractIntSlider.Direction.RIGHT
                : WAbstractIntSlider.Direction.DOWN;

        if (WAbstractIntSlider.isIncreasingKey(ch, direction)) {
            if (value < getMaxScrollValue()) {
                value++;
            }
            return InputResult.PROCESSED;
        } else if (WAbstractIntSlider.isDecreasingKey(ch, direction)) {
            if (value > 0) {
                value--;
            }
            return InputResult.PROCESSED;
        }

        return InputResult.IGNORED;
    }

    @Override
    public InputResult onMouseScroll(int x, int y, double horizontalAmount, double verticalAmount) {
        setValue(getValue() + (int) (horizontalAmount - verticalAmount) * scrollingSpeed);
        return InputResult.PROCESSED;
    }

    public int getValue() {
        return value;
    }

    public WScrollBar setValue(int value) {
        this.value = value;
        checkValue();
        return this;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public WScrollBar setMaxValue(int max) {
        this.maxValue = max;
        checkValue();
        return this;
    }

    /**
     * Sets the mouse scroll speed.
     *
     * <p>
     * By default, the scrolling speed is {@value #DEFAULT_SCROLLING_SPEED}.
     *
     * @param scrollingSpeed the scroll speed, must be positive
     * @return this scroll bar
     */
    public WScrollBar setScrollingSpeed(int scrollingSpeed) {
        if (scrollingSpeed < 0)
            throw new IllegalArgumentException("Negative value for scrolling speed");
        if (scrollingSpeed == 0)
            throw new IllegalArgumentException("Zero value for scrolling speed");

        this.scrollingSpeed = scrollingSpeed;
        return this;
    }

    /**
     * {@return the default mouse scroll speed}
     *
     * <p>
     * By default, the scrolling speed is {@value #DEFAULT_SCROLLING_SPEED}.
     */
    public int getScrollingSpeed() {
        return scrollingSpeed;
    }

    public int getWindow() {
        return window;
    }

    public WScrollBar setWindow(int window) {
        this.window = window;
        return this;
    }

    /**
     * Checks that the current value is in the correct range and adjusts it if
     * needed.
     */
    protected void checkValue() {
        if (this.value > maxValue - window) {
            this.value = maxValue - window;
        }
        if (this.value < 0)
            this.value = 0;
    }

    //#if MC >= 11800
    @Override
    public void addNarrations(NarrationElementOutput builder) {
        builder.add(NarratedElementType.TITLE, NarrationMessages.SCROLL_BAR_TITLE);
        builder.add(NarratedElementType.USAGE, NarrationMessages.SLIDER_USAGE);
    }
    //#endif
}
