package io.github.capsicum0907.caminus;

import java.util.function.IntSupplier;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class Hold extends ItemStackHandler {
    private static final String ITEMS = "Items";
    private static final String SLOT = "Slot";
    private static final String SAMPLE = "Sample";
    private static final String COUNT = "Count";

    private final IntSupplier batch;
    private final Runnable changed;

    public Hold(int size, IntSupplier batch, Runnable changed) {
        super(size);
        this.batch = batch;
        this.changed = changed;
    }

    public int capacity(ItemStack stack) {
        int batches = (int) Math.min(Integer.MAX_VALUE,
                (long) batch.getAsInt() * CaminusConfig.capacityBatches());
        return stack.isEmpty() ? batches : Math.max(batches, stack.getMaxStackSize());
    }

    public int room(int slot, ItemStack stack) {
        ItemStack held = getStackInSlot(slot);
        if (held.isEmpty()) {
            return capacity(stack);
        }
        if (!ItemStack.isSameItemSameComponents(held, stack)) {
            return 0;
        }
        return Math.max(0, capacity(held) - held.getCount());
    }

    @Override
    public int getSlotLimit(int slot) {
        return capacity(getStackInSlot(slot));
    }

    @Override
    protected int getStackLimit(int slot, ItemStack stack) {
        return capacity(stack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        changed.run();
    }

    public void touched(int slot) {
        onContentsChanged(slot);
    }

    public int size() {
        return stacks.size();
    }

    public void put(int slot, ItemStack stack) {
        stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        ListTag items = new ListTag();
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putInt(SLOT, slot);
            entry.put(SAMPLE, stack.copyWithCount(1).save(registries));
            entry.putInt(COUNT, stack.getCount());
            items.add(entry);
        }
        CompoundTag tag = new CompoundTag();
        tag.put(ITEMS, items);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        for (int slot = 0; slot < stacks.size(); slot++) {
            stacks.set(slot, ItemStack.EMPTY);
        }
        ListTag items = tag.getList(ITEMS, Tag.TAG_COMPOUND);
        for (int index = 0; index < items.size(); index++) {
            CompoundTag entry = items.getCompound(index);
            int slot = entry.getInt(SLOT);
            if (slot < 0 || slot >= stacks.size()) {
                continue;
            }
            ItemStack sample = ItemStack.parseOptional(registries, entry.getCompound(SAMPLE));
            if (!sample.isEmpty()) {
                stacks.set(slot, sample.copyWithCount(entry.getInt(COUNT)));
            }
        }
    }
}
