package io.github.capsicum0907.fornax;

public final class Layout {
    public static final int SLOT = 18;
    public static final int FLAME = 14;
    public static final int ARROW_WIDTH = 24;
    public static final int ARROW_HEIGHT = 16;
    public static final int INVENTORY_ROWS = 3;
    public static final int INVENTORY_COLUMNS = 9;

    private static final int VANILLA_WIDTH = 176;
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
    private static final int VANILLA_INVENTORY_X = 8;
    private static final int VANILLA_INVENTORY_Y = 84;
    private static final int VANILLA_HOTBAR_Y = 142;
    private static final int VANILLA_LABEL_ABOVE_BOTTOM = 94;

    private static final int FRAME_TOP = 16;
    private static final int EDGE = 7;
    private static final int ARROW_GAP = 8;
    private static final int STACK_GAP = 2;
    private static final int LABEL_GAP = 2;
    private static final int LABEL_TO_SLOTS = 12;
    private static final int HOTBAR_GAP = 4;
    private static final int BELOW_HOTBAR = 24;

    private final int lines;
    private final boolean vanilla;
    private final int columns;
    private final int grid;
    private final int width;
    private final int left;

    public Layout(int lines) {
        this.lines = lines;
        this.vanilla = lines == 1;
        this.columns = (int) Math.ceil(Math.sqrt(lines));
        this.grid = columns * SLOT;
        int content = grid * 2 + ARROW_GAP * 2 + ARROW_WIDTH;
        this.width = vanilla ? VANILLA_WIDTH : Math.max(VANILLA_WIDTH, content + EDGE * 2);
        this.left = (width - content) / 2;
    }

    public int lines() {
        return lines;
    }

    public boolean vanilla() {
        return vanilla;
    }

    public int width() {
        return width;
    }

    public int inputX(int line) {
        return vanilla ? VANILLA_INPUT_X : left + line % columns * SLOT + 1;
    }

    public int inputY(int line) {
        return vanilla ? VANILLA_INPUT_Y : FRAME_TOP + line / columns * SLOT + 1;
    }

    public int outputX(int line) {
        return vanilla ? VANILLA_OUTPUT_X : left + grid + ARROW_GAP * 2 + ARROW_WIDTH + line % columns * SLOT + 1;
    }

    public int outputY(int line) {
        return vanilla ? VANILLA_OUTPUT_Y : inputY(line);
    }

    public int arrowX() {
        return vanilla ? VANILLA_ARROW_X : left + grid + ARROW_GAP;
    }

    public int arrowY() {
        return vanilla ? VANILLA_ARROW_Y : FRAME_TOP + grid / 2 - ARROW_HEIGHT / 2;
    }

    public int flameX() {
        return vanilla ? VANILLA_FLAME_X : arrowX() + (ARROW_WIDTH - FLAME) / 2;
    }

    public int flameY() {
        return vanilla ? VANILLA_FLAME_Y : arrowY() + ARROW_HEIGHT + STACK_GAP;
    }

    public int fuelX() {
        return vanilla ? VANILLA_FUEL_X : arrowX() + (ARROW_WIDTH - SLOT) / 2 + 1;
    }

    public int fuelY() {
        return vanilla ? VANILLA_FUEL_Y : flameY() + FLAME + STACK_GAP + 1;
    }

    private int contentBottom() {
        return Math.max(FRAME_TOP + grid, fuelY() - 1 + SLOT);
    }

    public int inventoryLabelY() {
        return vanilla ? VANILLA_HEIGHT - VANILLA_LABEL_ABOVE_BOTTOM : contentBottom() + LABEL_GAP;
    }

    public int inventoryX() {
        return vanilla ? VANILLA_INVENTORY_X : (width - INVENTORY_COLUMNS * SLOT) / 2 + 1;
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
