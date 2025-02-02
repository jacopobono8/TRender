package dev.tr7zw.trender.gui;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import dev.tr7zw.trender.gui.client.BackgroundPainter;
import dev.tr7zw.trender.gui.client.LibGui;
import dev.tr7zw.trender.gui.networking.NetworkSide;
import dev.tr7zw.trender.gui.widget.WGridPanel;
import dev.tr7zw.trender.gui.widget.WLabel;
import dev.tr7zw.trender.gui.widget.WPanel;
import dev.tr7zw.trender.gui.widget.WPlayerInvPanel;
import dev.tr7zw.trender.gui.widget.WWidget;
import dev.tr7zw.trender.gui.widget.data.HorizontalAlignment;
import dev.tr7zw.trender.gui.widget.data.Insets;
import dev.tr7zw.trender.gui.widget.data.Vec2i;
import dev.tr7zw.util.NMSHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A screen handler-based GUI description for GUIs with slots.
 */
public class SyncedGuiDescription extends AbstractContainerMenu implements GuiDescription {

    protected Container blockInventory;
    protected Inventory playerInventory;
    protected Level world;
    protected ContainerData propertyDelegate;

    protected WPanel rootPanel = new WGridPanel().setInsets(Insets.ROOT_PANEL);
    protected int titleColor = WLabel.DEFAULT_TEXT_COLOR;
    protected int darkTitleColor = WLabel.DEFAULT_DARKMODE_TEXT_COLOR;
    protected boolean fullscreen = false;
    protected boolean titleVisible = true;
    protected HorizontalAlignment titleAlignment = HorizontalAlignment.LEFT;

    protected WWidget focus;
    private Vec2i titlePos = new Vec2i(8, 6);
    private boolean useDefaultRootBackground = true;

    /**
     * Constructs a new synced GUI description without a block inventory or a
     * property delegate.
     *
     * @param type            the {@link MenuType} of this GUI description
     * @param syncId          the current sync ID
     * @param playerInventory the player inventory of the player viewing this screen
     */
    public SyncedGuiDescription(MenuType<?> type, int syncId, Inventory playerInventory) {
        super(type, syncId);
        this.blockInventory = null;
        this.playerInventory = playerInventory;
        //#if MC >= 11800
        this.world = playerInventory.player.level();
        //#else
        //$$ this.world = playerInventory.player.level;
        //#endif
        this.propertyDelegate = null;//new ArrayPropertyDelegate(1);
    }

    /**
     * Constructs a new synced GUI description.
     *
     * @param type             the {@link MenuType} of this GUI description
     * @param syncId           the current sync ID
     * @param playerInventory  the player inventory of the player viewing this
     *                         screen
     * @param blockInventory   the block inventory of a corresponding container
     *                         block, or null if not found or applicable
     * @param propertyDelegate a property delegate whose properties, if any, will
     *                         automatically be
     *                         {@linkplain #addDataSlots(ContainerData) added}
     */
    public SyncedGuiDescription(MenuType<?> type, int syncId, Inventory playerInventory,
            @Nullable Container blockInventory, @Nullable ContainerData propertyDelegate) {
        super(type, syncId);
        this.blockInventory = blockInventory;
        this.playerInventory = playerInventory;
        //#if MC >= 11800
        this.world = playerInventory.player.level();
        //#else
        //$$ this.world = playerInventory.player.level;
        //#endif
        this.propertyDelegate = propertyDelegate;
        if (propertyDelegate != null && propertyDelegate.getCount() > 0)
            this.addDataSlots(propertyDelegate);
        if (blockInventory != null)
            blockInventory.startOpen(playerInventory.player);
    }

    public WPanel getRootPanel() {
        return rootPanel;
    }

    public int getTitleColor() {
        return (world.isClientSide && isDarkMode().withDefault(LibGui.isDarkMode())) ? darkTitleColor : titleColor;
    }

    public SyncedGuiDescription setRootPanel(WPanel panel) {
        this.rootPanel = panel;
        return this;
    }

    @Override
    public SyncedGuiDescription setTitleColor(int color) {
        this.titleColor = color;
        this.darkTitleColor = (color == WLabel.DEFAULT_TEXT_COLOR) ? WLabel.DEFAULT_DARKMODE_TEXT_COLOR : color;
        return this;
    }

    @Override
    public SyncedGuiDescription setTitleColor(int lightColor, int darkColor) {
        this.titleColor = lightColor;
        this.darkTitleColor = darkColor;
        return this;
    }

    public void addPainters() {
        if (this.rootPanel != null && !fullscreen && getUseDefaultRootBackground()) {
            this.rootPanel.setBackgroundPainter(BackgroundPainter.VANILLA);
        }
    }

    @Override
    public boolean getUseDefaultRootBackground() {
        return useDefaultRootBackground;
    }

    @Override
    public void setUseDefaultRootBackground(boolean useDefaultRootBackground) {
        this.useDefaultRootBackground = useDefaultRootBackground;
    }

    public void addSlotPeer(ValidatedSlot slot) {
        this.addSlot(slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            if (blockInventory != null) {
                if (slot.container == blockInventory) {
                    //Try to transfer the item from the block into the player's inventory
                    if (!this.insertItem(slotStack, this.playerInventory, true, player)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.insertItem(slotStack, this.blockInventory, false, player)) { //Try to transfer the item from the player to the block
                    return ItemStack.EMPTY;
                }
            } else {
                //There's no block, just swap between the player's storage and their hotbar
                if (!swapHotbar(slotStack, index, this.playerInventory, player)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                //#if MC >= 11904
                slot.setByPlayer(ItemStack.EMPTY);
                //#else
                //$$ slot.set(ItemStack.EMPTY);
                //#endif
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    /** WILL MODIFY toInsert! Returns true if anything was inserted. */
    private boolean insertIntoExisting(ItemStack toInsert, Slot slot, Player player) {
        ItemStack curSlotStack = slot.getItem();
        if (!curSlotStack.isEmpty() && NMSHelper.isSame(toInsert, curSlotStack) && slot.mayPlace(toInsert)) {
            int combinedAmount = curSlotStack.getCount() + toInsert.getCount();
            int maxAmount = Math.min(toInsert.getMaxStackSize(), slot.getMaxStackSize(toInsert));
            if (combinedAmount <= maxAmount) {
                toInsert.setCount(0);
                curSlotStack.setCount(combinedAmount);
                slot.setChanged();
                return true;
            } else if (curSlotStack.getCount() < maxAmount) {
                toInsert.shrink(maxAmount - curSlotStack.getCount());
                curSlotStack.setCount(maxAmount);
                slot.setChanged();
                return true;
            }
        }
        return false;
    }

    /** WILL MODIFY toInsert! Returns true if anything was inserted. */
    private boolean insertIntoEmpty(ItemStack toInsert, Slot slot) {
        ItemStack curSlotStack = slot.getItem();
        if (curSlotStack.isEmpty() && slot.mayPlace(toInsert)) {
            if (toInsert.getCount() > slot.getMaxStackSize(toInsert)) {
                //#if MC >= 11904
                slot.setByPlayer(toInsert.split(slot.getMaxStackSize(toInsert)));
                //#else
                //$$ slot.set(toInsert.split(slot.getMaxStackSize(toInsert)));
                //#endif
            } else {
                //#if MC >= 11904
                slot.setByPlayer(toInsert.split(toInsert.getCount()));
                //#else
                //$$ slot.set(toInsert.split(slot.getMaxStackSize(toInsert)));
                //#endif
            }

            slot.setChanged();
            return true;
        }

        return false;
    }

    private boolean insertItem(ItemStack toInsert, Container inventory, boolean walkBackwards, Player player) {
        //Make a unified list of slots *only from this inventory*
        ArrayList<Slot> inventorySlots = new ArrayList<>();
        for (Slot slot : slots) {
            if (slot.container == inventory)
                inventorySlots.add(slot);
        }
        if (inventorySlots.isEmpty())
            return false;

        //Try to insert it on top of existing stacks
        boolean inserted = false;
        if (walkBackwards) {
            for (int i = inventorySlots.size() - 1; i >= 0; i--) {
                Slot curSlot = inventorySlots.get(i);
                if (insertIntoExisting(toInsert, curSlot, player))
                    inserted = true;
                if (toInsert.isEmpty())
                    break;
            }
        } else {
            for (int i = 0; i < inventorySlots.size(); i++) {
                Slot curSlot = inventorySlots.get(i);
                if (insertIntoExisting(toInsert, curSlot, player))
                    inserted = true;
                if (toInsert.isEmpty())
                    break;
            }

        }

        //If we still have any, shove them into empty slots
        if (!toInsert.isEmpty()) {
            if (walkBackwards) {
                for (int i = inventorySlots.size() - 1; i >= 0; i--) {
                    Slot curSlot = inventorySlots.get(i);
                    if (insertIntoEmpty(toInsert, curSlot))
                        inserted = true;
                    if (toInsert.isEmpty())
                        break;
                }
            } else {
                for (int i = 0; i < inventorySlots.size(); i++) {
                    Slot curSlot = inventorySlots.get(i);
                    if (insertIntoEmpty(toInsert, curSlot))
                        inserted = true;
                    if (toInsert.isEmpty())
                        break;
                }

            }
        }

        return inserted;
    }

    private boolean swapHotbar(ItemStack toInsert, int slotNumber, Container inventory, Player player) {
        //Feel out the slots to see what's storage versus hotbar
        ArrayList<Slot> storageSlots = new ArrayList<>();
        ArrayList<Slot> hotbarSlots = new ArrayList<>();
        boolean swapToStorage = true;
        boolean inserted = false;

        for (Slot slot : slots) {
            if (slot.container == inventory && slot instanceof ValidatedSlot validated) {
                int index = validated.getInventoryIndex();
                if (Inventory.isHotbarSlot(index)) {
                    hotbarSlots.add(slot);
                } else {
                    storageSlots.add(slot);
                    if (slot.index == slotNumber)
                        swapToStorage = false;
                }
            }
        }
        if (storageSlots.isEmpty() || hotbarSlots.isEmpty())
            return false;

        if (swapToStorage) {
            //swap from hotbar to storage
            for (int i = 0; i < storageSlots.size(); i++) {
                Slot curSlot = storageSlots.get(i);
                if (insertIntoExisting(toInsert, curSlot, player))
                    inserted = true;
                if (toInsert.isEmpty())
                    break;
            }
            if (!toInsert.isEmpty()) {
                for (int i = 0; i < storageSlots.size(); i++) {
                    Slot curSlot = storageSlots.get(i);
                    if (insertIntoEmpty(toInsert, curSlot))
                        inserted = true;
                    if (toInsert.isEmpty())
                        break;
                }
            }
        } else {
            //swap from storage to hotbar
            for (int i = 0; i < hotbarSlots.size(); i++) {
                Slot curSlot = hotbarSlots.get(i);
                if (insertIntoExisting(toInsert, curSlot, player))
                    inserted = true;
                if (toInsert.isEmpty())
                    break;
            }
            if (!toInsert.isEmpty()) {
                for (int i = 0; i < hotbarSlots.size(); i++) {
                    Slot curSlot = hotbarSlots.get(i);
                    if (insertIntoEmpty(toInsert, curSlot))
                        inserted = true;
                    if (toInsert.isEmpty())
                        break;
                }
            }
        }

        return inserted;
    }

    @Nullable
    @Override
    public ContainerData getPropertyDelegate() {
        return propertyDelegate;
    }

    @Override
    public GuiDescription setPropertyDelegate(ContainerData delegate) {
        this.propertyDelegate = delegate;
        return this;
    }

    /**
     * Creates a player inventory widget from this panel's
     * {@linkplain #playerInventory player inventory}.
     *
     * @return the created inventory widget
     */
    public WPlayerInvPanel createPlayerInventoryPanel() {
        return new WPlayerInvPanel(this.playerInventory);
    }

    /**
     * Creates a player inventory widget from this panel's
     * {@linkplain #playerInventory player inventory}.
     *
     * @param hasLabel whether the "Inventory" label should be displayed
     * @return the created inventory widget
     * @since 2.0.0
     */
    public WPlayerInvPanel createPlayerInventoryPanel(boolean hasLabel) {
        return new WPlayerInvPanel(this.playerInventory, hasLabel);
    }

    /**
     * Creates a player inventory widget from this panel's
     * {@linkplain #playerInventory player inventory}.
     *
     * @param label the inventory label widget
     * @return the created inventory widget
     * @since 2.0.0
     */
    public WPlayerInvPanel createPlayerInventoryPanel(WWidget label) {
        return new WPlayerInvPanel(this.playerInventory, label);
    }

    /**
     * Gets the block inventory at the context.
     *
     * <p>
     * If no inventory is found, returns {@link EmptyInventory#INSTANCE}.
     *
     * <p>
     * Searches for these implementations in the following order:
     * <ol>
     * <li>Blocks implementing {@code InventoryProvider}</li>
     * <li>Block entities implementing {@code InventoryProvider}</li>
     * <li>Block entities implementing {@code Inventory}</li>
     * </ol>
     *
     * @param ctx the context
     * @return the found inventory
     */
    public static Container getBlockInventory(ContainerLevelAccess ctx) {
        return getBlockInventory(ctx, () -> EmptyInventory.INSTANCE);
    }

    /**
     * Gets the block inventory at the context.
     *
     * <p>
     * If no inventory is found, returns a simple mutable inventory with the
     * specified number of slots.
     *
     * <p>
     * Searches for these implementations in the following order:
     * <ol>
     * <li>Blocks implementing {@code InventoryProvider}</li>
     * <li>Block entities implementing {@code InventoryProvider}</li>
     * <li>Block entities implementing {@code Inventory}</li>
     * </ol>
     *
     * @param ctx  the context
     * @param size the fallback inventory size
     * @return the found inventory
     * @since 2.0.0
     */
    public static Container getBlockInventory(ContainerLevelAccess ctx, int size) {
        return getBlockInventory(ctx, () -> new SimpleContainer(size));
    }

    private static Container getBlockInventory(ContainerLevelAccess ctx, Supplier<Container> fallback) {
        return ctx.evaluate((world, pos) -> {
            BlockState state = world.getBlockState(pos);
            Block b = state.getBlock();

            if (b instanceof WorldlyContainerHolder inventoryProvider) {
                Container inventory = inventoryProvider.getContainer(state, world, pos);
                if (inventory != null) {
                    return inventory;
                }
            }

            BlockEntity be = world.getBlockEntity(pos);
            if (be != null) {
                if (be instanceof WorldlyContainerHolder inventoryProvider) {
                    Container inventory = inventoryProvider.getContainer(state, world, pos);
                    if (inventory != null) {
                        return inventory;
                    }
                } else if (be instanceof Container inventory) {
                    return inventory;
                }
            }

            return fallback.get();
        }).orElseGet(fallback);
    }

    /**
     * Gets the property delegate at the context.
     *
     * <p>
     * If no property delegate is found, returns an empty property delegate with no
     * properties.
     *
     * <p>
     * Searches for block entities implementing {@link PropertyDelegateHolder}.
     *
     * @param ctx the context
     * @return the found property delegate
     */
    public static ContainerData getBlockPropertyDelegate(ContainerLevelAccess ctx) {
        return ctx.evaluate((world, pos) -> {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof PropertyDelegateHolder holder) {
                return holder.getPropertyDelegate();
            }

            return new SimpleContainerData(0);
        }).orElse(new SimpleContainerData(0));
    }

    /**
     * Gets the property delegate at the context.
     *
     * <p>
     * If no property delegate is found, returns an array property delegate with the
     * specified number of properties.
     *
     * <p>
     * Searches for block entities implementing {@link PropertyDelegateHolder}.
     *
     * @param ctx  the context
     * @param size the number of properties
     * @return the found property delegate
     * @since 2.0.0
     */
    public static ContainerData getBlockPropertyDelegate(ContainerLevelAccess ctx, int size) {
        return ctx.evaluate((world, pos) -> {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof PropertyDelegateHolder holder) {
                return holder.getPropertyDelegate();
            }

            return new SimpleContainerData(size);
        }).orElse(new SimpleContainerData(size));
    }

    //extends ScreenHandler {
    @Override
    public boolean stillValid(Player entity) {
        return (blockInventory != null) ? blockInventory.stillValid(entity) : true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (blockInventory != null)
            blockInventory.stopOpen(player);
    }
    //}

    @Override
    public boolean isFocused(WWidget widget) {
        return focus == widget;
    }

    @Override
    public WWidget getFocus() {
        return focus;
    }

    @Override
    public void requestFocus(WWidget widget) {
        //TODO: Are there circumstances where focus can't be stolen?
        if (focus == widget)
            return; //Nothing happens if we're already focused
        if (!widget.canFocus())
            return; //This is kind of a gotcha but needs to happen
        if (focus != null)
            focus.onFocusLost();
        focus = widget;
        focus.onFocusGained();
    }

    @Override
    public void releaseFocus(WWidget widget) {
        if (focus == widget) {
            focus = null;
            widget.onFocusLost();
        }
    }

    @Override
    public boolean isFullscreen() {
        return fullscreen;
    }

    @Override
    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    @Override
    public boolean isTitleVisible() {
        return titleVisible;
    }

    @Override
    public void setTitleVisible(boolean titleVisible) {
        this.titleVisible = titleVisible;
    }

    @Override
    public HorizontalAlignment getTitleAlignment() {
        return titleAlignment;
    }

    @Override
    public void setTitleAlignment(HorizontalAlignment titleAlignment) {
        this.titleAlignment = titleAlignment;
    }

    @Override
    public Vec2i getTitlePos() {
        return titlePos;
    }

    @Override
    public void setTitlePos(Vec2i titlePos) {
        this.titlePos = titlePos;
    }

    /**
     * {@return the world of this GUI description's player}
     * 
     * @since 10.0.0
     */
    public Level getWorld() {
        return world;
    }

    /**
     * Gets the network side this GUI description runs on.
     *
     * @return this GUI's network side
     * @since 3.3.0
     */
    public final NetworkSide getNetworkSide() {
        return world instanceof ServerLevel ? NetworkSide.SERVER : NetworkSide.CLIENT;
    }

    /**
     * Gets the packet sender corresponding to this GUI's network side.
     *
     * @return the packet sender
     * @since 3.3.0
     */
    public final Consumer<Packet<?>> getPacketSender() {
        if (getNetworkSide() == NetworkSide.SERVER) {
            return ((ServerPlayer) playerInventory.player).connection::send;
        } else {
            return Minecraft.getInstance().getConnection()::send;
        }
    }

}
