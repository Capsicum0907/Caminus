package io.github.capsicum0907.fornax;

import java.util.function.Consumer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class FurnaceMenu extends AbstractContainerMenu {
    private static final double REACH = 8.0;

    private final FurnaceBlockEntity furnace;
    private final ContainerData data;
    private final Layout layout;
    private final Level level;
    private final int lines;

    public FurnaceMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, null, Tier.byOrdinal(buffer.readVarInt()), buffer.readVarInt(), null);
    }

    public FurnaceMenu(int id, Inventory inventory, FurnaceBlockEntity furnace, ContainerData data) {
        this(id, inventory, furnace, furnace.tier(), furnace.batch(), data);
    }

    private FurnaceMenu(int id, Inventory inventory, FurnaceBlockEntity furnace, Tier tier, int batch,
            ContainerData data) {
        super(FornaxRegistry.FURNACE_MENU.get(), id);
        this.furnace = furnace;
        this.lines = tier.lines();
        this.layout = new Layout(lines);
        this.level = inventory.player.level();
        this.data = data != null ? data : new SimpleContainerData(FurnaceData.size(lines));
        Hold inputs = furnace != null ? furnace.inputs() : new Hold(lines, () -> batch, () -> {
        });
        Hold outputs = furnace != null ? furnace.outputs() : new Hold(lines, () -> batch, () -> {
        });
        ItemStackHandler fuel = furnace != null ? furnace.fuel() : clientFuel();

        for (int line = 0; line < lines; line++) {
            addSlot(new HeldSlot(inputs, line, layout.inputX(line), layout.inputY(line), true, player -> {
            }));
        }
        addSlot(new SlotItemHandler(fuel, 0, layout.fuelX(), layout.fuelY()));
        for (int line = 0; line < lines; line++) {
            addSlot(new HeldSlot(outputs, line, layout.outputX(line), layout.outputY(line), false, this::award));
        }
        for (int row = 0; row < Layout.INVENTORY_ROWS; row++) {
            for (int column = 0; column < Layout.INVENTORY_COLUMNS; column++) {
                addSlot(new Slot(inventory, column + row * Layout.INVENTORY_COLUMNS + Layout.INVENTORY_COLUMNS,
                        Layout.INVENTORY_X + column * Layout.SLOT, layout.inventoryY() + row * Layout.SLOT));
            }
        }
        for (int column = 0; column < Layout.INVENTORY_COLUMNS; column++) {
            addSlot(new Slot(inventory, column, Layout.INVENTORY_X + column * Layout.SLOT, layout.hotbarY()));
        }
        addDataSlots(this.data);
    }

    private static ItemStackHandler clientFuel() {
        return new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return FurnaceBlockEntity.burns(stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return Integer.MAX_VALUE;
            }
        };
    }

    private void award(Player player) {
        if (furnace != null && player instanceof ServerPlayer server) {
            furnace.awardExperience(server.serverLevel(), server.position(), server);
        }
    }

    public Layout layout() {
        return layout;
    }

    public int lines() {
        return lines;
    }

    public float burned() {
        return FurnaceBlockEntity.burned(FurnaceData.heat(data), FurnaceData.burnLength(data));
    }

    public boolean burning() {
        return FurnaceData.heat(data) > 0;
    }

    public float progress(int line) {
        return FurnaceData.progress(data, line);
    }

    private int fuelSlot() {
        return lines;
    }

    private int machineSlots() {
        return lines * 2 + 1;
    }

    private boolean smeltable(ItemStack stack) {
        return level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level).isPresent();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack inSlot = slot.getItem();
        ItemStack before = inSlot.copy();
        int machine = machineSlots();
        if (index < machine) {
            ItemStack moving = slot.remove(inSlot.getMaxStackSize());
            int removed = moving.getCount();
            moveItemStackTo(moving, machine, slots.size(), true);
            if (!moving.isEmpty()) {
                putBack(slot, moving);
            }
            int moved = removed - moving.getCount();
            if (moved <= 0) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, before.copyWithCount(moved));
            return before.copyWithCount(moved);
        }
        boolean moved = false;
        if (smeltable(inSlot)) {
            moved = moveItemStackTo(inSlot, 0, lines, false);
        }
        if (!moved && FurnaceBlockEntity.burns(inSlot)) {
            moved = moveItemStackTo(inSlot, fuelSlot(), fuelSlot() + 1, false);
        }
        if (!moved) {
            int inventoryEnd = machine + Layout.INVENTORY_ROWS * Layout.INVENTORY_COLUMNS;
            boolean fromInventory = index < inventoryEnd;
            moved = moveItemStackTo(inSlot, fromInventory ? inventoryEnd : machine,
                    fromInventory ? slots.size() : inventoryEnd, false);
        }
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (inSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return before;
    }

    private static void putBack(Slot slot, ItemStack stack) {
        ItemStack current = slot.getItem();
        if (current.isEmpty()) {
            slot.set(stack);
            return;
        }
        slot.set(current.copyWithCount(current.getCount() + stack.getCount()));
    }

    @Override
    public boolean stillValid(Player player) {
        if (furnace == null) {
            return true;
        }
        return !furnace.isRemoved()
                && player.distanceToSqr(furnace.getBlockPos().getCenter()) <= REACH * REACH;
    }

    public static class HeldSlot extends SlotItemHandler {
        private final Hold hold;
        private final boolean accepts;
        private final Consumer<Player> taken;

        public HeldSlot(Hold hold, int index, int x, int y, boolean accepts, Consumer<Player> taken) {
            super(hold, index, x, y);
            this.hold = hold;
            this.accepts = accepts;
            this.taken = taken;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return accepts && super.mayPlace(stack);
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return hold.capacity(stack);
        }

        @Override
        public void setChanged() {
            hold.touched(getSlotIndex());
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            if (!accepts) {
                stack.onCraftedBy(player.level(), player, stack.getCount());
                taken.accept(player);
            }
            super.onTake(player, stack);
        }

        public IItemHandler hold() {
            return hold;
        }
    }
}
