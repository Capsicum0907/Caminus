package io.github.capsicum0907.fornax;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ElectricPower implements Power {
    private static final String ENERGY = "Energy";

    private final Store store;

    public ElectricPower(Rung rung, Runnable changed) {
        int capacity = capacity(rung);
        this.store = new Store(capacity, changed);
    }

    public static int costOf(int recipeTicks) {
        return (int) Math.min(Integer.MAX_VALUE, (long) recipeTicks * FornaxConfig.energyPerTick());
    }

    public static int capacity(Rung rung) {
        long perSmelt = (long) rung.lines() * rung.batch() * costOf(FornaxConfig.STANDARD);
        long perTick = perSmelt / Math.max(1, rung.ticks());
        long buffered = perTick * FornaxConfig.bufferTicks();
        long floor = perSmelt * FornaxConfig.capacityBatches();
        return (int) Math.min(Integer.MAX_VALUE, Math.max(buffered, floor));
    }

    public int stored() {
        return store.getEnergyStored();
    }

    @Override
    public IEnergyStorage energy() {
        return store;
    }

    @Override
    public boolean ready(int cost) {
        return store.getEnergyStored() >= costOf(cost);
    }

    @Override
    public int afford(int wanted, int cost) {
        return (int) Math.min(wanted, store.getEnergyStored() / (long) costOf(cost));
    }

    @Override
    public void pay(int count, int cost) {
        store.spend((long) count * costOf(cost));
    }

    @Override
    public int gauge(double spent) {
        return store.getEnergyStored();
    }

    @Override
    public int gaugeFull() {
        return store.getMaxEnergyStored();
    }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt(ENERGY, store.getEnergyStored());
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        store.deserializeNBT(registries, IntTag.valueOf(Math.min(tag.getInt(ENERGY), store.getMaxEnergyStored())));
    }

    private static final class Store extends EnergyStorage {
        private final Runnable changed;

        Store(int capacity, Runnable changed) {
            super(capacity, capacity, 0);
            this.changed = changed;
        }

        void spend(long amount) {
            energy = (int) Math.max(0, energy - amount);
            changed.run();
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int received = super.receiveEnergy(toReceive, simulate);
            if (received > 0 && !simulate) {
                changed.run();
            }
            return received;
        }
    }
}
