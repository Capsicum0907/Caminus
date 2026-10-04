package io.github.capsicum0907.fornax;

public final class Layout {
    public static final int WIDTH = 176;
    public static final int SLOT = 18;
    public static final int PER_ROW = 8;
    public static final int TOP = 17;
    public static final int GAP = 6;
    public static final int FLAME = 14;
    public static final int BAR = 2;
    public static final int LABEL = 11;
    public static final int INVENTORY_X = 8;
    public static final int INVENTORY_ROWS = 3;
    public static final int INVENTORY_COLUMNS = 9;
    public static final int HOTBAR_GAP = 4;
    public static final int BOTTOM = 8;

    private final int lines;
    private final int columns;
    private final int rows;

    public Layout(int lines) {
        this.lines = lines;
        this.columns = Math.min(lines, PER_ROW);
        this.rows = (lines + PER_ROW - 1) / PER_ROW;
    }

    public int lines() {
        return lines;
    }

    private int left() {
        return (WIDTH - columns * SLOT) / 2 + 1;
    }

    public int inputX(int line) {
        return left() + line % columns * SLOT;
    }

    public int inputY(int line) {
        return TOP + line / columns * SLOT;
    }

    private int inputsBottom() {
        return TOP + rows * SLOT;
    }

    public int fuelX() {
        return WIDTH / 2 - SLOT / 2 + 1;
    }

    public int fuelY() {
        return inputsBottom() + GAP;
    }

    public int flameX() {
        return fuelX() - SLOT - FLAME / 2;
    }

    public int flameY() {
        return fuelY() + 1;
    }

    public int outputX(int line) {
        return inputX(line);
    }

    public int outputY(int line) {
        return fuelY() + SLOT + GAP + line / columns * SLOT;
    }

    public int inventoryLabelY() {
        return fuelY() + SLOT + GAP + rows * SLOT + GAP;
    }

    public int inventoryY() {
        return inventoryLabelY() + LABEL;
    }

    public int hotbarY() {
        return inventoryY() + INVENTORY_ROWS * SLOT + HOTBAR_GAP;
    }

    public int height() {
        return hotbarY() + SLOT + BOTTOM - 1;
    }
}
