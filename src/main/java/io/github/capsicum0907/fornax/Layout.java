package io.github.capsicum0907.fornax;

public final class Layout {
    public static final int WIDTH = 176;
    public static final int SLOT = 18;
    public static final int PER_ROW = 8;
    public static final int TOP = 17;
    public static final int GAP = 6;
    public static final int FLAME = 14;
    public static final int ARROW_WIDTH = 24;
    public static final int ARROW_HEIGHT = 16;
    public static final int BAR = 2;
    public static final int LABEL = 11;
    public static final int INVENTORY_X = 8;
    public static final int INVENTORY_ROWS = 3;
    public static final int INVENTORY_COLUMNS = 9;
    public static final int HOTBAR_GAP = 4;
    public static final int BOTTOM = 8;

    private static final int VANILLA_INPUT_X = 56;
    private static final int VANILLA_INPUT_Y = 17;
    private static final int VANILLA_FUEL_X = 56;
    private static final int VANILLA_FUEL_Y = 53;
    private static final int VANILLA_OUTPUT_X = 116;
    private static final int VANILLA_OUTPUT_Y = 35;
    private static final int VANILLA_FLAME_X = 56;
    private static final int VANILLA_FLAME_Y = 36;
    private static final int VANILLA_ARROW_X = 79;
    private static final int VANILLA_ARROW_Y = 34;
    private static final int VANILLA_INVENTORY_Y = 84;
    private static final int VANILLA_HOTBAR_Y = 142;
    private static final int VANILLA_HEIGHT = 166;
    private static final int VANILLA_LABEL_ABOVE_BOTTOM = 94;

    private final int lines;
    private final boolean vanilla;
    private final int columns;
    private final int rows;

    public Layout(int lines) {
        this.lines = lines;
        this.vanilla = lines == 1;
        this.columns = Math.min(lines, PER_ROW);
        this.rows = (lines + PER_ROW - 1) / PER_ROW;
    }

    public int lines() {
        return lines;
    }

    public boolean vanilla() {
        return vanilla;
    }

    private int left() {
        return (WIDTH - columns * SLOT) / 2 + 1;
    }

    public int inputX(int line) {
        return vanilla ? VANILLA_INPUT_X : left() + line % columns * SLOT;
    }

    public int inputY(int line) {
        return vanilla ? VANILLA_INPUT_Y : TOP + line / columns * SLOT;
    }

    private int inputsBottom() {
        return TOP + rows * SLOT;
    }

    public int fuelX() {
        return vanilla ? VANILLA_FUEL_X : WIDTH / 2 - SLOT / 2 + 1;
    }

    public int fuelY() {
        return vanilla ? VANILLA_FUEL_Y : inputsBottom() + GAP;
    }

    public int flameX() {
        return vanilla ? VANILLA_FLAME_X : fuelX() - SLOT - FLAME / 2;
    }

    public int flameY() {
        return vanilla ? VANILLA_FLAME_Y : fuelY() + 1;
    }

    public int arrowX() {
        return VANILLA_ARROW_X;
    }

    public int arrowY() {
        return VANILLA_ARROW_Y;
    }

    public int outputX(int line) {
        return vanilla ? VANILLA_OUTPUT_X : inputX(line);
    }

    public int outputY(int line) {
        return vanilla ? VANILLA_OUTPUT_Y : fuelY() + SLOT + GAP + line / columns * SLOT;
    }

    public int inventoryLabelY() {
        return vanilla ? VANILLA_HEIGHT - VANILLA_LABEL_ABOVE_BOTTOM : fuelY() + SLOT + GAP + rows * SLOT + GAP;
    }

    public int inventoryY() {
        return vanilla ? VANILLA_INVENTORY_Y : inventoryLabelY() + LABEL;
    }

    public int hotbarY() {
        return vanilla ? VANILLA_HOTBAR_Y : inventoryY() + INVENTORY_ROWS * SLOT + HOTBAR_GAP;
    }

    public int height() {
        return vanilla ? VANILLA_HEIGHT : hotbarY() + SLOT + BOTTOM - 1;
    }
}
