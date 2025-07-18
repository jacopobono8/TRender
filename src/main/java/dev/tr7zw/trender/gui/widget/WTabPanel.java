package dev.tr7zw.trender.gui.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import dev.tr7zw.trender.gui.client.BackgroundPainter;
import dev.tr7zw.trender.gui.client.LibGui;
import dev.tr7zw.trender.gui.client.RenderContext;
import dev.tr7zw.trender.gui.client.ScreenDrawing;
import dev.tr7zw.trender.gui.impl.LibGuiCommon;
import dev.tr7zw.trender.gui.impl.client.NarrationMessages;
import dev.tr7zw.trender.gui.impl.client.style.GuiStyle;
import dev.tr7zw.trender.gui.impl.client.style.StyleConstants;
import dev.tr7zw.trender.gui.widget.data.Axis;
import dev.tr7zw.trender.gui.widget.data.HorizontalAlignment;
import dev.tr7zw.trender.gui.widget.data.InputResult;
import dev.tr7zw.trender.gui.widget.icon.Icon;
import dev.tr7zw.transition.mc.ComponentProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//#if MC >= 11800
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//#endif
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

// TODO: Different tab positions

/**
 * A panel that contains creative inventory-style tabs on the top.
 *
 * @since 3.0.0
 */
public class WTabPanel extends WPanel {
    private static final int TAB_PADDING = 4;
    private static final int TAB_WIDTH = 28;
    private static final int TAB_HEIGHT = 20;
    private static final int ICON_SIZE = 16;
    private final WBox tabRibbon = new WBox(Axis.HORIZONTAL).setSpacing(1);
    private final List<WTab> tabWidgets = new ArrayList<>();
    private final Map<Tab, WTab> tabWidgetsByData = new HashMap<>();
    private final WCardPanel mainPanel = new WCardPanel();

    /**
     * Constructs a new tab panel.
     */
    public WTabPanel() {
        add(tabRibbon, 0, 0);
        add(mainPanel, 0, TAB_HEIGHT);
    }

    private void add(WWidget widget, int x, int y) {
        children.add(widget);
        widget.setParent(this);
        widget.setLocation(x, y);
        expandToFit(widget);
    }

    /**
     * Adds a tab to this panel.
     *
     * @param tab the added tab
     */
    public void add(Tab tab) {
        WTab tabWidget = new WTab(tab);

        if (tabWidgets.isEmpty()) {
            tabWidget.selected = true;
        }

        tabWidgets.add(tabWidget);
        tabWidgetsByData.put(tab, tabWidget);
        tabRibbon.add(tabWidget, TAB_WIDTH, TAB_HEIGHT);
        mainPanel.add(tab.getWidget());
    }

    /**
     * Configures and adds a tab to this panel.
     *
     * @param widget       the contained widget
     * @param configurator the tab configurator
     */
    public void add(WWidget widget, Consumer<Tab.Builder> configurator) {
        Tab.Builder builder = new Tab.Builder(widget);
        configurator.accept(builder);
        add(builder.build());
    }

    /**
     * {@return the currently open tab's data}
     * 
     * @since 6.3.0
     */
    public Tab getSelectedTab() {
        return ((WTab) mainPanel.getSelectedCard()).data;
    }

    /**
     * Sets the currently open tab to the provided {@link Tab}.
     *
     * @param tab the tab to open, cannot be null
     * @return this tab panel
     * @throws NoSuchElementException if the tab is not in this panel
     * @since 6.3.0
     */
    @Contract("null -> fail; _ -> this")
    public WTabPanel setSelectedTab(Tab tab) {
        Objects.requireNonNull(tab, "tab");
        WTab widget = tabWidgetsByData.get(tab);

        if (widget == null) {
            throw new NoSuchElementException("Trying to select unknown tab " + tab);
        }

        return setSelectedIndex(tabWidgets.indexOf(widget));
    }

    /**
     * {@return the index of the currently open tab}
     * 
     * @since 6.3.0
     */
    public int getSelectedIndex() {
        return mainPanel.getSelectedIndex();
    }

    /**
     * Sets the currently open tab by its index.
     *
     * @param tabIndex the 0-based index of the tab to select, in order of adding
     * @return this tab panel
     * @throws IndexOutOfBoundsException if the tab index is invalid for this tab
     *                                   panel
     * @since 6.3.0
     */
    @Contract("_ -> this")
    public WTabPanel setSelectedIndex(int tabIndex) {
        mainPanel.setSelectedIndex(tabIndex);

        for (int i = 0; i < getTabCount(); i++) {
            tabWidgets.get(i).selected = (i == tabIndex);
        }

        layout();
        return this;
    }

    /**
     * {@return the number of tabs in this tab panel}
     * 
     * @since 6.3.0
     */
    public int getTabCount() {
        return tabWidgets.size();
    }

    @Override
    public void setSize(int x, int y) {
        super.setSize(x, y);
        tabRibbon.setSize(x, TAB_HEIGHT);
    }

    @Override
    public void addPainters() {
        super.addPainters();
        mainPanel.setBackgroundPainter(BackgroundPainter.VANILLA);
    }

    /**
     * The data of a tab.
     */
    public static class Tab {
        @Nullable
        private final Component title;
        @Nullable
        private final Icon icon;
        private final WWidget widget;
        @Nullable
        private final Consumer<TooltipBuilder> tooltip;

        private Tab(@Nullable Component title, @Nullable Icon icon, WWidget widget,
                @Nullable Consumer<TooltipBuilder> tooltip) {
            if (title == null && icon == null) {
                throw new IllegalArgumentException("A tab must have a title or an icon");
            }

            this.title = title;
            this.icon = icon;
            this.widget = Objects.requireNonNull(widget, "widget");
            this.tooltip = tooltip;
        }

        /**
         * Gets the title of this tab.
         *
         * @return the title, or null if there's no title
         */
        @Nullable
        public Component getTitle() {
            return title;
        }

        /**
         * Gets the icon of this tab.
         *
         * @return the icon, or null if there's no title
         */
        @Nullable
        public Icon getIcon() {
            return icon;
        }

        /**
         * Gets the contained widget of this tab.
         *
         * @return the contained widget
         */
        public WWidget getWidget() {
            return widget;
        }

        /**
         * Adds this widget's tooltip to the {@code tooltip} builder.
         *
         * @param tooltip the tooltip builder
         */

        public void addTooltip(TooltipBuilder tooltip) {
            if (this.tooltip != null) {
                this.tooltip.accept(tooltip);
            }
        }

        /**
         * A builder for tab data.
         */
        public static final class Builder {
            @Nullable
            private Component title;
            @Nullable
            private Icon icon;
            private final WWidget widget;
            private final List<Component> tooltip = new ArrayList<>();

            /**
             * Constructs a new tab data builder.
             *
             * @param widget the contained widget
             * @throws NullPointerException if the widget is null
             */
            public Builder(WWidget widget) {
                this.widget = Objects.requireNonNull(widget, "widget");
            }

            /**
             * Sets the tab title.
             *
             * @param title the new title
             * @return this builder
             * @throws NullPointerException if the title is null
             */
            public Builder title(Component title) {
                this.title = Objects.requireNonNull(title, "title");
                return this;
            }

            /**
             * Sets the tab icon.
             *
             * @param icon the new icon
             * @return this builder
             * @throws NullPointerException if the icon is null
             */
            public Builder icon(Icon icon) {
                this.icon = Objects.requireNonNull(icon, "icon");
                return this;
            }

            /**
             * Adds lines to the tab's tooltip.
             *
             * @param lines the added lines
             * @return this builder
             * @throws NullPointerException if the line array is null
             */
            public Builder tooltip(Component... lines) {
                Objects.requireNonNull(lines, "lines");
                Collections.addAll(tooltip, lines);

                return this;
            }

            /**
             * Adds lines to the tab's tooltip.
             *
             * @param lines the added lines
             * @return this builder
             * @throws NullPointerException if the line collection is null
             */
            public Builder tooltip(Collection<? extends Component> lines) {
                Objects.requireNonNull(lines, "lines");
                tooltip.addAll(lines);
                return this;
            }

            /**
             * Builds a tab from this builder.
             *
             * @return the built tab
             */
            public Tab build() {
                Consumer<TooltipBuilder> tooltip = null;

                if (!this.tooltip.isEmpty()) {
                    tooltip = new Consumer<TooltipBuilder>() {

                        @Override
                        public void accept(TooltipBuilder builder) {
                            for (Component line : Builder.this.tooltip) {
                                Minecraft.getInstance().font.split(line, 170).forEach((f) -> {
                                    builder.add(new FormattedCharSequence[] { f });
                                });
                            }
                        }
                    };
                }

                return new Tab(title, icon, widget, tooltip);
            }
        }
    }

    private final class WTab extends WWidget {
        private final Tab data;
        boolean selected = false;

        WTab(Tab data) {
            this.data = data;
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
        public InputResult onClick(int x, int y, int button) {
            super.onClick(x, y, button);

            Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            setSelectedIndex(tabWidgets.indexOf(this));
            return InputResult.PROCESSED;
        }

        @Override
        public InputResult onKeyPressed(int ch, int key, int modifiers) {
            if (isActivationKey(ch)) {
                onClick(0, 0, 0);
                return InputResult.PROCESSED;
            }

            return InputResult.IGNORED;
        }

        @Override
        public void paint(RenderContext context, int x, int y, int mouseX, int mouseY) {
            Font renderer = Minecraft.getInstance().font;
            Component title = data.getTitle();
            Icon icon = data.getIcon();

            if (title != null) {
                int width = TAB_WIDTH + renderer.width(title);
                if (icon == null)
                    width = Math.max(TAB_WIDTH, width - ICON_SIZE);

                if (this.getWidth() != width) {
                    setSize(width, this.getHeight());
                    getParent().layout();
                }
            }

            (selected ? Painters.SELECTED_TAB : Painters.UNSELECTED_TAB).paintBackground(context, x, y, this);
            if (isFocused()) {
                (selected ? Painters.SELECTED_TAB_FOCUS_BORDER : Painters.UNSELECTED_TAB_FOCUS_BORDER)
                        .paintBackground(context, x, y, this);
            }

            int iconX = 6;

            if (title != null) {
                int titleX = (icon != null) ? iconX + ICON_SIZE + 1 : 0;
                int titleY = (getHeight() - renderer.lineHeight) / 2 + 3;
                int width = (icon != null) ? this.getWidth() - iconX - ICON_SIZE : this.getWidth();
                HorizontalAlignment align = (icon != null) ? HorizontalAlignment.LEFT : HorizontalAlignment.CENTER;

                int color;
                if (LibGui.getGuiStyle() == GuiStyle.VANILLA_OLD) {
                    color = selected ? 0xFFFFFFFF : 0xFFAAAAAA;
                } else if (LibGui.getGuiStyle().isDark()) {
                    color = selected ? 0xFFEEEEEE : 0xFF777777;
                } else {
                    color = selected ? StyleConstants.DEFAULT_TEXT_COLOR : 0xFFEEEEEE;
                }

                if (LibGui.getGuiStyle().isFontShadow()) {
                    ScreenDrawing.drawStringWithShadow(context, title.getVisualOrderText(), align, x + titleX,
                            y + titleY, width, color);
                } else {
                    ScreenDrawing.drawString(context, title.getVisualOrderText(), align, x + titleX, y + titleY, width,
                            color);
                }
            }

            if (icon != null) {
                icon.paint(context, x + iconX, y + 1 + (getHeight() - ICON_SIZE) / 2, ICON_SIZE);
            }
        }

        @Override
        public void addTooltip(TooltipBuilder tooltip) {
            data.addTooltip(tooltip);
        }

        //#if MC >= 11800
        @Override
        public void addNarrations(NarrationElementOutput builder) {
            Component label = data.getTitle();

            if (label != null) {
                builder.add(NarratedElementType.TITLE,
                        ComponentProvider.translatable(NarrationMessages.TAB_TITLE_KEY, label));
            }

            builder.add(NarratedElementType.POSITION, ComponentProvider.translatable(NarrationMessages.TAB_POSITION_KEY,
                    tabWidgets.indexOf(this) + 1, tabWidgets.size()));
        }
        //#endif
    }

    /**
     * Internal background painter instances for tabs.
     */

    final static class Painters {
        static final BackgroundPainter SELECTED_TAB = BackgroundPainter
                .createStyleVariantsNinePatch("textures/widget/tab/selected_", p -> {
                });

        static final BackgroundPainter UNSELECTED_TAB = BackgroundPainter
                .createStyleVariantsNinePatch("textures/widget/tab/unselected_", p -> {
                });

        static final BackgroundPainter SELECTED_TAB_FOCUS_BORDER = BackgroundPainter
                .createNinePatch(LibGuiCommon.id("textures/widget/tab/focus.png")).setTopPadding(2);
        static final BackgroundPainter UNSELECTED_TAB_FOCUS_BORDER = BackgroundPainter
                .createNinePatch(LibGuiCommon.id("textures/widget/tab/focus.png"));
    }
}
