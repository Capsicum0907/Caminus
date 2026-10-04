package io.github.capsicum0907.caminus;

import java.util.function.Predicate;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class WholeView implements IItemHandler {
    private final Hold inputs;
    private final ItemStackHandler fuel;
    private final Hold outputs;
    private final InputView inputView;
    private final OutputView outputView;
    private final Predicate<ItemStack> smeltable;

    public WholeView(Hold inputs, ItemStackHandler fuel, Hold outputs, InputView inputView, OutputView outputView,
            Predicate<ItemStack> smeltable) {
        this.inputs = inputs;
        this.fuel = fuel;
        this.outputs = outputs;
        this.inputView = inputView;
        this.outputView = outputView;
        this.smeltable = smeltable;
    }

    private int fuelSlots() {
        return fuel == null ? 0 : 1;
    }

    private int firstOutput() {
        return inputs.getSlots() + fuelSlots();
    }

    private boolean isFuel(int slot) {
        return fuel != null && slot == inputs.getSlots();
    }

    @Override
    public int getSlots() {
        return firstOutput() + outputs.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < inputs.getSlots()) {
            return inputs.getStackInSlot(slot);
        }
        if (isFuel(slot)) {
            return fuel.getStackInSlot(0);
        }
        return outputs.getStackInSlot(slot - firstOutput());
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (fuel != null && !smeltable.test(stack) && FuelPower.burns(stack)) {
            return fuel.insertItem(0, stack, simulate);
        }
        return inputView.insertItem(0, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot < inputs.getSlots()) {
            return ItemStack.EMPTY;
        }
        if (isFuel(slot)) {
            return outputView.extractItem(outputs.getSlots(), amount, simulate);
        }
        return outputView.extractItem(slot - firstOutput(), amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot < inputs.getSlots()) {
            return inputs.getSlotLimit(slot);
        }
        if (isFuel(slot)) {
            return fuel.getSlotLimit(0);
        }
        return outputs.getSlotLimit(slot - firstOutput());
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot < inputs.getSlots() || isFuel(slot) && FuelPower.burns(stack);
    }
}
