package io.github.capsicum0907.fornax;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class InputView implements IItemHandler {
    private final Hold inputs;

    public InputView(Hold inputs) {
        this.inputs = inputs;
    }

    @Override
    public int getSlots() {
        return inputs.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inputs.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        ItemStack left = stack;
        boolean[] tried = new boolean[inputs.getSlots()];
        while (!left.isEmpty()) {
            int line = emptiest(left, tried);
            if (line < 0) {
                break;
            }
            tried[line] = true;
            left = inputs.insertItem(line, left, simulate);
        }
        return left;
    }

    private int emptiest(ItemStack stack, boolean[] tried) {
        int best = -1;
        int fewest = Integer.MAX_VALUE;
        for (int line = 0; line < inputs.getSlots(); line++) {
            if (tried[line] || inputs.room(line, stack) <= 0) {
                continue;
            }
            int count = inputs.getStackInSlot(line).getCount();
            if (count < fewest) {
                fewest = count;
                best = line;
            }
        }
        return best;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return inputs.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }
}
