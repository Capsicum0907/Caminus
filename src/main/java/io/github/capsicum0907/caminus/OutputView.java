package io.github.capsicum0907.caminus;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class OutputView implements IItemHandler {
    private final Hold outputs;
    private final ItemStackHandler fuel;

    public OutputView(Hold outputs, ItemStackHandler fuel) {
        this.outputs = outputs;
        this.fuel = fuel;
    }

    private boolean isFuelSlot(int slot) {
        return fuel != null && slot == outputs.getSlots();
    }

    @Override
    public int getSlots() {
        return outputs.getSlots() + (fuel == null ? 0 : 1);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return isFuelSlot(slot) ? fuel.getStackInSlot(0) : outputs.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!isFuelSlot(slot)) {
            return outputs.extractItem(slot, amount, simulate);
        }
        if (FurnaceBlockEntity.burns(fuel.getStackInSlot(0))) {
            return ItemStack.EMPTY;
        }
        return fuel.extractItem(0, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return isFuelSlot(slot) ? fuel.getSlotLimit(0) : outputs.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return false;
    }
}
