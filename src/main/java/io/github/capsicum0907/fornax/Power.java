package io.github.capsicum0907.fornax;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

public interface Power {
    boolean ready(int cost);

    int afford(int wanted, int cost);

    void pay(int count, int cost);

    int gauge(double spent);

    int gaugeFull();

    void save(CompoundTag tag, HolderLookup.Provider registries);

    void load(CompoundTag tag, HolderLookup.Provider registries);

    default void idle() {
    }

    default boolean burning() {
        return false;
    }

    default ItemStackHandler fuel() {
        return null;
    }

    default IEnergyStorage energy() {
        return null;
    }

    default void spill(NonNullList<ItemStack> into) {
    }
}
