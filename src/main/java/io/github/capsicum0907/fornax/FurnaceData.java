package io.github.capsicum0907.fornax;

import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;

import net.minecraft.world.inventory.ContainerData;

public final class FurnaceData implements ContainerData {
    public static final int SCALE = Short.MAX_VALUE;

    private static final int HEAT = 0;
    private static final int BURN_LENGTH = 2;
    private static final int WORKING = 4;
    private static final int LINES = 5;

    private final int lines;
    private final IntSupplier heat;
    private final IntSupplier burnLength;
    private final IntSupplier working;
    private final IntUnaryOperator progress;

    public FurnaceData(int lines, IntSupplier heat, IntSupplier burnLength, IntSupplier working,
            IntUnaryOperator progress) {
        this.lines = lines;
        this.heat = heat;
        this.burnLength = burnLength;
        this.working = working;
        this.progress = progress;
    }

    public static int size(int lines) {
        return LINES + lines;
    }

    public static int scaled(int done, int total) {
        return (int) ((long) done * SCALE / total);
    }

    @Override
    public int get(int index) {
        if (index == WORKING) {
            return working.getAsInt();
        }
        if (index < WORKING) {
            int whole = (index < BURN_LENGTH ? heat : burnLength).getAsInt();
            return (index & 1) == 0 ? whole >>> 16 : whole & 0xFFFF;
        }
        return progress.applyAsInt(index - LINES);
    }

    @Override
    public void set(int index, int value) {
    }

    @Override
    public int getCount() {
        return size(lines);
    }

    public static int heat(ContainerData data) {
        return whole(data, HEAT);
    }

    public static int burnLength(ContainerData data) {
        return whole(data, BURN_LENGTH);
    }

    public static boolean working(ContainerData data) {
        return data.get(WORKING) != 0;
    }

    public static float progress(ContainerData data, int line) {
        return (data.get(LINES + line) & 0xFFFF) / (float) SCALE;
    }

    private static int whole(ContainerData data, int at) {
        return (data.get(at) & 0xFFFF) << 16 | (data.get(at + 1) & 0xFFFF);
    }
}
