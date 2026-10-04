package io.github.capsicum0907.caminus;

import java.util.function.Consumer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class FurnaceMenu extends RecipeBookMenu<SingleRecipeInput, AbstractCookingRecipe> {
    public static final int INGREDIENT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    public static final int RECIPE_SLOTS = 3;

    private static final double REACH = 8.0;

    private final FurnaceBlockEntity furnace;
    private final ContainerData data;
    private final Layout layout;
    private final Level level;
    private final int lines;
    private final Rung rung;

    public FurnaceMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, null, rung(buffer), buffer.readVarInt(), null);
    }

    public FurnaceMenu(int id, Inventory inventory, FurnaceBlockEntity furnace, ContainerData data) {
        this(id, inventory, furnace, furnace.rung(), furnace.batch(), data);
    }

    private static Rung rung(RegistryFriendlyByteBuf buffer) {
        Kind kind = Kind.byOrdinal(buffer.readVarInt());
        int tier = buffer.readVarInt();
        return new Rung(kind, tier < 0 ? null : Tier.byOrdinal(tier));
    }

    private FurnaceMenu(int id, Inventory inventory, FurnaceBlockEntity furnace, Rung rung, int batch,
            ContainerData data) {
        super(CaminusRegistry.FURNACE_MENU.get(), id);
        this.furnace = furnace;
        this.rung = rung;
        this.lines = rung.lines();
        this.layout = new Layout(lines, electric());
        this.level = inventory.player.level();
        this.data = data != null ? data : new SimpleContainerData(FurnaceData.size(lines));
        Hold inputs = furnace != null ? furnace.inputs() : new Hold(lines, () -> batch, () -> {
        });
        Hold outputs = furnace != null ? furnace.outputs() : new Hold(lines, () -> batch, () -> {
        });
        addSlot(input(inputs, 0));
        if (electric()) {
            addSlot(new Placeholder(layout.fuelX(), layout.fuelY()));
        } else {
            ItemStackHandler fuel = furnace != null ? furnace.fuel() : FuelPower.slot(() -> {
            });
            addSlot(new SlotItemHandler(fuel, 0, layout.fuelX(), layout.fuelY()));
        }
        addSlot(output(outputs, 0));
        for (int line = 1; line < lines; line++) {
            addSlot(input(inputs, line));
        }
        for (int line = 1; line < lines; line++) {
            addSlot(output(outputs, line));
        }
        for (int row = 0; row < Layout.INVENTORY_ROWS; row++) {
            for (int column = 0; column < Layout.INVENTORY_COLUMNS; column++) {
                addSlot(new Slot(inventory, column + row * Layout.INVENTORY_COLUMNS + Layout.INVENTORY_COLUMNS,
                        layout.inventoryX() + column * Layout.SLOT, layout.inventoryY() + row * Layout.SLOT));
            }
        }
        for (int column = 0; column < Layout.INVENTORY_COLUMNS; column++) {
            addSlot(new Slot(inventory, column, layout.inventoryX() + column * Layout.SLOT, layout.hotbarY()));
        }
        addDataSlots(this.data);
    }

    private HeldSlot input(Hold inputs, int line) {
        return new HeldSlot(inputs, line, layout.inputX(line), layout.inputY(line), true, player -> {
        });
    }

    private HeldSlot output(Hold outputs, int line) {
        return new HeldSlot(outputs, line, layout.outputX(line), layout.outputY(line), false, this::award);
    }

    private void award(Player player) {
        if (furnace != null && player instanceof ServerPlayer server) {
            furnace.awardExperience(server.serverLevel(), server.position(), server);
        }
    }

    public boolean electric() {
        return rung.kind() == Kind.ELECTRIC;
    }

    public Rung rung() {
        return rung;
    }

    public int energy() {
        return FurnaceData.heat(data);
    }

    public int capacity() {
        return FurnaceData.burnLength(data);
    }

    public float charged() {
        int capacity = capacity();
        return capacity <= 0 ? 0.0F : Math.min(1.0F, energy() / (float) capacity);
    }

    public Layout layout() {
        return layout;
    }

    public int lines() {
        return lines;
    }

    public int inputSlot(int line) {
        return line == 0 ? INGREDIENT_SLOT : RECIPE_SLOTS + line - 1;
    }

    public int outputSlot(int line) {
        return line == 0 ? RESULT_SLOT : RECIPE_SLOTS + lines - 1 + line - 1;
    }

    private int machineSlots() {
        return lines * 2 + 1;
    }

    public float burned() {
        return FurnaceBlockEntity.burned(FurnaceData.heat(data), FurnaceData.burnLength(data));
    }

    public boolean burning() {
        return FurnaceData.working(data);
    }

    public float progress(int line) {
        return FurnaceData.progress(data, line);
    }

    public float progress() {
        float sum = 0.0F;
        for (int line = 0; line < lines; line++) {
            sum += progress(line);
        }
        return sum / lines;
    }

    private boolean smeltable(ItemStack stack) {
        return level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level).isPresent();
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedContents contents) {
        for (int slot = 0; slot < RECIPE_SLOTS; slot++) {
            contents.accountStack(getSlot(slot).getItem());
        }
    }

    @Override
    public void clearCraftingContent() {
        getSlot(INGREDIENT_SLOT).set(ItemStack.EMPTY);
        getSlot(RESULT_SLOT).set(ItemStack.EMPTY);
    }

    @Override
    public boolean recipeMatches(RecipeHolder<AbstractCookingRecipe> recipe) {
        return recipe.value().matches(new SingleRecipeInput(getSlot(INGREDIENT_SLOT).getItem()), level);
    }

    @Override
    protected void finishPlacingRecipe(RecipeHolder<AbstractCookingRecipe> recipe) {
        if (furnace != null) {
            furnace.setChanged();
        }
    }

    @Override
    public int getResultSlotIndex() {
        return RESULT_SLOT;
    }

    @Override
    public int getGridWidth() {
        return 1;
    }

    @Override
    public int getGridHeight() {
        return 1;
    }

    @Override
    public int getSize() {
        return RECIPE_SLOTS;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.FURNACE;
    }

    @Override
    public boolean shouldMoveToInventory(int slotIndex) {
        return slotIndex != FUEL_SLOT;
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
            for (int line = 0; line < lines && !inSlot.isEmpty(); line++) {
                moved |= moveItemStackTo(inSlot, inputSlot(line), inputSlot(line) + 1, false);
            }
        }
        if (!moved && !electric() && FurnaceBlockEntity.burns(inSlot)) {
            moved = moveItemStackTo(inSlot, FUEL_SLOT, FUEL_SLOT + 1, false);
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

    public static class Placeholder extends Slot {
        public Placeholder(int x, int y) {
            super(new SimpleContainer(1), 0, x, y);
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
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
    }
}
