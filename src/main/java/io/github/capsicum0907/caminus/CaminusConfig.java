package io.github.capsicum0907.caminus;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class CaminusConfig {
    public static final int STANDARD = AbstractFurnaceBlockEntity.BURN_TIME_STANDARD;

    private static final int MOST_TICKS = 72_000;
    private static final int MOST_BATCH = 1 << 20;
    private static final int MOST_CAPACITY_BATCHES = 64;
    private static final int MOST_ENERGY_PER_TICK = 1_000_000;
    private static final int MOST_BUFFER_TICKS = 72_000;

    private static final Map<Tier, ModConfigSpec.IntValue> TICKS = new EnumMap<>(Tier.class);
    private static final Map<Tier, ModConfigSpec.IntValue> BATCH = new EnumMap<>(Tier.class);
    private static final ModConfigSpec.IntValue CAPACITY_BATCHES;
    private static final ModConfigSpec.IntValue ENERGY_PER_TICK;
    private static final ModConfigSpec.IntValue BUFFER_TICKS;

    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        for (Tier tier : Tier.values()) {
            builder.push(tier.id());
            TICKS.put(tier, builder.defineInRange("ticks", defaultTicks(tier), 1, MOST_TICKS));
            BATCH.put(tier, builder.defineInRange("batch", defaultBatch(tier), 1, MOST_BATCH));
            builder.pop();
        }
        CAPACITY_BATCHES = builder.defineInRange("capacityBatches", 2, 1, MOST_CAPACITY_BATCHES);
        builder.push("electric");
        ENERGY_PER_TICK = builder.defineInRange("energyPerTick", 20, 1, MOST_ENERGY_PER_TICK);
        BUFFER_TICKS = builder.defineInRange("bufferTicks", 200, 1, MOST_BUFFER_TICKS);
        builder.pop();
        SPEC = builder.build();
    }

    private CaminusConfig() {
    }

    public static int defaultTicks(Tier tier) {
        double reach = tier.rung() / (double) Tier.SPEED_CEILING.rung();
        return (int) Math.max(1, Math.round(STANDARD * Math.pow(1.0 / STANDARD, reach)));
    }

    public static int defaultBatch(Tier tier) {
        return tier.batch();
    }

    public static int ticks(Tier tier) {
        return SPEC.isLoaded() ? TICKS.get(tier).get() : defaultTicks(tier);
    }

    public static int batch(Tier tier) {
        return SPEC.isLoaded() ? BATCH.get(tier).get() : defaultBatch(tier);
    }

    public static int capacityBatches() {
        return SPEC.isLoaded() ? CAPACITY_BATCHES.get() : CAPACITY_BATCHES.getDefault();
    }

    public static int energyPerTick() {
        return SPEC.isLoaded() ? ENERGY_PER_TICK.get() : ENERGY_PER_TICK.getDefault();
    }

    public static int bufferTicks() {
        return SPEC.isLoaded() ? BUFFER_TICKS.get() : BUFFER_TICKS.getDefault();
    }
}
