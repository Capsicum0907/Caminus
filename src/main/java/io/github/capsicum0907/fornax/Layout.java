package io.github.capsicum0907.fornax;

public final class Layout {
    public static final int WIDTH = 176;
    public static final int SLOT = 18;
    public static final int FLAME = 14;
    public static final int ARROW_WIDTH = 24;
    public static final int ARROW_HEIGHT = 16;
    public static final int INVENTORY_ROWS = 3;
    public static final int INVENTORY_COLUMNS = 9;
    public static final int BOOK_WIDTH = 20;
    public static final int BOOK_HEIGHT = 18;

    private static final int VANILLA_HEIGHT = 166;
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
    private static final int VANILLA_LABEL_ABOVE_BOTTOM = 94;
    private static final int VANILLA_BOOK_X = 20;
    private static final int VANILLA_BOOK_ABOVE_MIDDLE = 49;

    private static final int EDGE = 7;
    private static final int COLUMN_GAP = 4;
    private static final int FRAME_TOP = 16;
    private static final int PER_ROW = 8;
    private static final int ARROW_GAP = 2;
    private static final int LABEL_GAP = 2;
    private static final int LABEL_TO_SLOTS = 12;
    private static final int HOTBAR_GAP = 4;
    private static final int BELOW_HOTBAR = 24;

    private final int lines;
    private final boolean electric;
    private final boolean vanilla;
    private final int perRow;
    private final int rows;
    private final int width;
    private final int inventoryFrame;
    private final int slotsLeft;

    public Layout(int lines, boolean electric) {
        this.lines = lines;
        this.electric = electric;
        this.vanilla = lines == 1;
        this.perRow = Math.min(lines, PER_ROW);
        this.rows = (lines + perRow - 1) / perRow;
        int blockColumn = (INVENTORY_COLUMNS - (perRow + 1)) / 2 + 1;
        int sideRoom = EDGE + (blockColumn - 1) * SLOT;
        int widen = vanilla ? 0 : Math.max(0, EDGE + COLUMN_GAP - sideRoom);
        this.width = WIDTH + widen * 2;
        this.inventoryFrame = (width - INVENTORY_COLUMNS * SLOT) / 2;
        this.slotsLeft = inventoryFrame + blockColumn * SLOT;
    }

    public int lines() {
        return lines;
    }

    public boolean vanilla() {
        return vanilla;
    }

    public boolean electric() {
        return electric;
    }

    public int barX() {
        return fuelX() - 1;
    }

    public int barY() {
        return flameY();
    }

    public int barWidth() {
        return SLOT;
    }

    public int barHeight() {
        return fuelY() - 1 + SLOT - flameY();
    }

    public int width() {
        return width;
    }

    private int slotsLeft() {
        return slotsLeft;
    }

    private int column() {
        return slotsLeft - COLUMN_GAP - SLOT;
    }

    private int outputsTop() {
        return arrowY() + ARROW_WIDTH + ARROW_GAP;
    }

    public int bookX() {
        return vanilla ? VANILLA_BOOK_X : column() - (BOOK_WIDTH - SLOT);
    }

    public int bookY(int screenHeight, int top) {
        return vanilla ? screenHeight / 2 - VANILLA_BOOK_ABOVE_MIDDLE
                : top + FRAME_TOP + (rows * SLOT - BOOK_HEIGHT) / 2;
    }

    public int inputX(int line) {
        return vanilla ? VANILLA_INPUT_X : slotsLeft() + line % perRow * SLOT + 1;
    }

    public int inputY(int line) {
        return vanilla ? VANILLA_INPUT_Y : FRAME_TOP + line / perRow * SLOT + 1;
    }

    public int outputX(int line) {
        return vanilla ? VANILLA_OUTPUT_X : inputX(line);
    }

    public int outputY(int line) {
        return vanilla ? VANILLA_OUTPUT_Y : outputsTop() + line / perRow * SLOT + 1;
    }

    public boolean arrowDown() {
        return !vanilla;
    }

    public int arrowX() {
        return vanilla ? VANILLA_ARROW_X : slotsLeft() + perRow * SLOT / 2 - ARROW_HEIGHT / 2;
    }

    public int arrowY() {
        return vanilla ? VANILLA_ARROW_Y : FRAME_TOP + rows * SLOT + ARROW_GAP;
    }

    public int fuelX() {
        return vanilla ? VANILLA_FUEL_X : column() + 1;
    }

    public int fuelY() {
        return vanilla ? VANILLA_FUEL_Y : outputsTop() + (rows - 1) * SLOT + 1;
    }

    public int flameX() {
        return vanilla ? VANILLA_FLAME_X : column() + (SLOT - FLAME) / 2;
    }

    public int flameY() {
        return vanilla ? VANILLA_FLAME_Y : VANILLA_FLAME_Y + (fuelY() - VANILLA_FUEL_Y);
    }

    private int contentBottom() {
        return outputsTop() + rows * SLOT;
    }

    public int inventoryLabelY() {
        return vanilla ? VANILLA_HEIGHT - VANILLA_LABEL_ABOVE_BOTTOM : contentBottom() + LABEL_GAP;
    }

    public int inventoryX() {
        return inventoryFrame + 1;
    }

    public int inventoryY() {
        return vanilla ? VANILLA_INVENTORY_Y : inventoryLabelY() + LABEL_TO_SLOTS;
    }

    public int hotbarY() {
        return vanilla ? VANILLA_HOTBAR_Y : inventoryY() + INVENTORY_ROWS * SLOT + HOTBAR_GAP;
    }

    public int height() {
        return vanilla ? VANILLA_HEIGHT : hotbarY() + BELOW_HOTBAR;
    }
}
