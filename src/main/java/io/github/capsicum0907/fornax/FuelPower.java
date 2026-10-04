package io.github.capsicum0907.fornax;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.items.ItemStackHandler;

public class FuelPower implements Power {
    private static final String FUEL = "Fuel";
    private static final String HEAT = "Heat";
    private static final String BURN_LENGTH = "BurnLength";
    private static final int IDLE_BURN = 1;

    private final ItemStackHandler fuel;
    private int heat;
    private int burnLength;

    public FuelPower(Runnable changed) {
        this.fuel = slot(changed);
    }

    public static ItemStackHandler slot(Runnable changed) {
        return new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return burns(stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return Integer.MAX_VALUE;
            }

            @Override
            protected void onContentsChanged(int slot) {
                changed.run();
            }
        };
    }

    public static boolean burns(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING) > 0;
    }

    public int heat() {
        return heat;
    }

    @Override
    public void idle() {
        heat = Math.max(0, heat - IDLE_BURN);
    }

    @Override
    public boolean burning() {
        return heat > 0;
    }

    @Override
    public ItemStackHandler fuel() {
        return fuel;
    }

    @Override
    public boolean ready(int cost) {
        return heat >= cost || pull();
    }

    @Override
    public int afford(int wanted, int cost) {
        long needed = (long) wanted * cost;
        while (heat < needed && pull()) {
        }
        return (int) Math.min(wanted, heat / cost);
    }

    @Override
    public void pay(int count, int cost) {
        heat -= count * cost;
    }

    @Override
    public int gauge(double spent) {
        return (int) Math.max(0, Math.round(heat - spent));
    }

    @Override
    public int gaugeFull() {
        return burnLength;
    }

    private boolean fuelReady() {
        ItemStack stack = fuel.getStackInSlot(0);
        return burns(stack) && (stack.getCount() == 1 || stack.getCraftingRemainingItem().isEmpty());
    }

    private boolean pull() {
        if (!fuelReady()) {
            return false;
        }
        ItemStack stack = fuel.getStackInSlot(0);
        int burn = stack.getBurnTime(RecipeType.SMELTING);
        ItemStack remainder = stack.getCraftingRemainingItem();
        fuel.setStackInSlot(0, stack.getCount() == 1 ? remainder : stack.copyWithCount(stack.getCount() - 1));
        heat += burn;
        burnLength = burn;
        return true;
    }

    @Override
    public void spill(NonNullList<ItemStack> into) {
        into.add(fuel.getStackInSlot(0).copy());
    }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put(FUEL, fuel.serializeNBT(registries));
        tag.putInt(HEAT, heat);
        tag.putInt(BURN_LENGTH, burnLength);
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        fuel.deserializeNBT(registries, tag.getCompound(FUEL));
        heat = tag.getInt(HEAT);
        burnLength = tag.getInt(BURN_LENGTH);
    }
}
