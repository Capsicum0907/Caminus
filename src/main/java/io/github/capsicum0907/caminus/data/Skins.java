package io.github.capsicum0907.caminus.data;

import java.util.Arrays;
import java.util.Locale;

import io.github.capsicum0907.caminus.Kind;
import io.github.capsicum0907.caminus.Rung;
import io.github.capsicum0907.caminus.Tier;

public final class Skins {
    public static final int SIZE = 16;

    private static final int OPAQUE = 0xFF000000;
    private static final int SHINE = 0xFFFFFF;
    private static final int PATINA = 0x4FA88A;
    private static final int COIL_CORE = 0xFF8A3C;
    private static final int COIL_EDGE = 0x9A2A1C;
    private static final int COIL_COLD = 0x4A3C3A;
    private static final int COIL_COLD_EDGE = 0x2C2322;
    private static final int GLOW_CORE = 0x5C1C12;
    private static final int GLOW_EDGE = 0x24100C;
    private static final int[] COIL_ROWS = { 1, 3 };
    private static final int CLEAR = 0x00000000;
    public static final String OVERLAY = "electric_furnace_overlay";

    private static final float METAL_MID = 0.78F;
    private static final float METAL_DARK = 0.55F;
    private static final float METAL_DEEP = 0.35F;
    private static final int BEVEL_LIT = 22;
    private static final int BEVEL_DARK = 30;
    private static final int PATINA_SHARE = 14;
    private static final float PATINA_DEPTH = 0.7F;
    private static final int BRUSH = 7;
    private static final int BRUSH_LENGTH = 5;
    private static final int POLISH_EVERY = 9;
    private static final int FACET = 4;
    private static final int SPARKLE_SHARE = 4;
    private static final int STREAK_LENGTH = 4;
    private static final int STREAK_EVERY = 7;
    private static final int GLEAM_PER_RUNG = 3;

    private static final int RIVET_INSET = 2;
    private static final int UPPER_ARCH = 3;
    private static final int LOWER_ARCH = 11;
    private static final int[] OPENING_HALF_WIDTHS = { 4, 5, 5, 5 };
    private static final int SHELF_TOP = 7;
    private static final int SHELF_BAND = 9;
    private static final int LEDGE_LIT = 10;
    private static final float SHELF_SHINE = 0.45F;
    private static final float LIP_SHINE = 0.25F;
    private static final float UPPER_SHADE = 0.75F;
    private static final int LIP_LEFT = 3;
    private static final int PLATE_INSET = 3;

    public enum Face {
        FRONT, SIDE, TOP;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private Skins() {
    }

    public static String name(Rung rung, Face face, boolean lit) {
        return rung.id() + "_" + face.id() + (lit ? "_on" : "");
    }

    public static String overlayName(boolean lit) {
        return OVERLAY + (lit ? "_on" : "");
    }

    public static boolean lights(Face face) {
        return face == Face.FRONT;
    }

    public static int[][] skin(Rung rung, Face face, boolean lit) {
        Tier tier = rung.tier();
        boolean electric = rung.kind() == Kind.ELECTRIC;
        int[][] pixels = new int[SIZE][SIZE];
        plated(pixels, tier, 0, 0, SIZE - 1, SIZE - 1);
        switch (face) {
            case FRONT -> front(pixels, tier, lit, electric);
            case SIDE -> rivets(pixels, tier);
            case TOP -> {
                plated(pixels, tier, PLATE_INSET, PLATE_INSET, SIZE - 1 - PLATE_INSET, SIZE - 1 - PLATE_INSET);
                rivets(pixels, tier);
            }
        }
        return pixels;
    }

    public static int[][] overlaySkin(boolean lit) {
        int[][] pixels = new int[SIZE][SIZE];
        for (int[] row : pixels) {
            Arrays.fill(row, CLEAR);
        }
        element(pixels, lit);
        return pixels;
    }

    private static void front(int[][] pixels, Tier tier, boolean lit, boolean electric) {
        hearth(pixels, tier, lit && !electric);
        if (electric) {
            element(pixels, lit);
        }
    }

    private static void element(int[][] pixels, boolean lit) {
        for (int row : COIL_ROWS) {
            int y = LOWER_ARCH + row;
            int half = OPENING_HALF_WIDTHS[row];
            for (int x = SIZE / 2 - half; x <= SIZE / 2 - 1 + half; x++) {
                float heat = centred(x, half);
                pixels[y][x] = OPAQUE | (lit ? mix(COIL_EDGE, COIL_CORE, heat) : mix(COIL_COLD_EDGE, COIL_COLD, heat));
                if (!lit) {
                    continue;
                }
                for (int glow : new int[] { y - 1, y + 1 }) {
                    int glowRow = glow - LOWER_ARCH;
                    if (glowRow < 0 || glowRow >= OPENING_HALF_WIDTHS.length || isCoil(glowRow)) {
                        continue;
                    }
                    int glowHalf = OPENING_HALF_WIDTHS[glowRow];
                    if (x >= SIZE / 2 - glowHalf && x <= SIZE / 2 - 1 + glowHalf) {
                        pixels[glow][x] = OPAQUE | mix(GLOW_EDGE, GLOW_CORE, heat);
                    }
                }
            }
        }
    }

    private static float centred(int x, int half) {
        float middle = (SIZE - 1) / 2.0F;
        return 1.0F - Math.abs(x - middle) / half;
    }

    private static boolean isCoil(int row) {
        for (int coil : COIL_ROWS) {
            if (coil == row) {
                return true;
            }
        }
        return false;
    }

    private static void hearth(int[][] pixels, Tier tier, boolean lit) {
        paint(pixels, 1, 1, SIZE - 2, SHELF_BAND - 1, (x, y) -> scale(metal(tier, x, y), UPPER_SHADE));
        paint(pixels, LIP_LEFT, SHELF_TOP, SIZE - 1 - LIP_LEFT, SHELF_TOP,
                (x, y) -> mix(metal(tier, x, y), SHINE, LIP_SHINE));
        paint(pixels, 1, SHELF_BAND, SIZE - 2, SHELF_BAND, (x, y) -> mix(metal(tier, x, y), SHINE, SHELF_SHINE));
        paint(pixels, 1, SHELF_BAND + 1, SIZE - 2, SIZE - 2, (x, y) -> shift(metal(tier, x, y), LEDGE_LIT));
        arch(pixels, UPPER_ARCH, false);
        arch(pixels, LOWER_ARCH, lit);
    }

    private static void arch(int[][] pixels, int top, boolean spill) {
        for (int row = 0; row < OPENING_HALF_WIDTHS.length; row++) {
            clear(pixels, top + row, OPENING_HALF_WIDTHS[row]);
        }
        if (spill) {
            clear(pixels, top + OPENING_HALF_WIDTHS.length, OPENING_HALF_WIDTHS[OPENING_HALF_WIDTHS.length - 1]);
        }
    }

    private static void clear(int[][] pixels, int y, int half) {
        for (int x = SIZE / 2 - half; x <= SIZE / 2 - 1 + half; x++) {
            pixels[y][x] = CLEAR;
        }
    }

    private enum Finish { PATINA, BRUSHED, POLISHED, FACETED, DARK, GLEAM }

    private static Finish finish(Tier tier) {
        return switch (tier) {
            case COPPER -> Finish.PATINA;
            case IRON -> Finish.BRUSHED;
            case GOLD -> Finish.POLISHED;
            case DIAMOND -> Finish.FACETED;
            case NETHERITE -> Finish.DARK;
            case NETHER_STAR, COMPRESSED_NETHER_STAR, SUPER_COMPRESSED_NETHER_STAR -> Finish.GLEAM;
        };
    }

    private static int metal(Tier tier, int x, int y) {
        int light = tier.colour();
        int mid = scale(light, METAL_MID);
        int dark = scale(light, METAL_DARK);
        int roll = hash(x, y) % 100;
        return switch (finish(tier)) {
            case PATINA -> roll < PATINA_SHARE ? mix(mid, PATINA, PATINA_DEPTH) : grain(mid, x, y);
            case BRUSHED -> shift(mid, hash(y, x / BRUSH_LENGTH) % (2 * BRUSH + 1) - BRUSH);
            case POLISHED -> {
                int band = Math.floorMod(x + y, POLISH_EVERY);
                yield band < 2 ? light : band == 2 ? mix(light, mid, 0.5F) : mid;
            }
            case FACETED -> roll < SPARKLE_SHARE ? SHINE
                    : Math.floorMod(x, FACET) > Math.floorMod(y, FACET) ? mix(light, mid, 0.4F) : mid;
            case DARK -> hash(y, x / STREAK_LENGTH) % STREAK_EVERY == 0 ? mid : dark;
            case GLEAM -> {
                int gleams = (tier.ordinal() - Tier.NETHER_STAR.ordinal() + 1) * GLEAM_PER_RUNG;
                yield roll < gleams ? SHINE : roll < 2 * gleams ? mix(light, SHINE, 0.4F) : mid;
            }
        };
    }

    @FunctionalInterface
    private interface Paint {
        int at(int x, int y);
    }

    private static void paint(int[][] pixels, int left, int top, int right, int bottom, Paint paint) {
        for (int y = top; y <= bottom; y++) {
            for (int x = left; x <= right; x++) {
                pixels[y][x] = OPAQUE | paint.at(x, y);
            }
        }
    }

    private static void plated(int[][] pixels, Tier tier, int left, int top, int right, int bottom) {
        paint(pixels, left, top, right, bottom, (x, y) -> {
            int colour = metal(tier, x, y);
            if (y == top || x == left) {
                return shift(colour, BEVEL_LIT);
            }
            return y == bottom || x == right ? shift(colour, -BEVEL_DARK) : colour;
        });
    }

    private static void rivets(int[][] pixels, Tier tier) {
        int stud = mix(tier.colour(), SHINE, 0.5F);
        for (int y : new int[] { RIVET_INSET, SIZE - 1 - RIVET_INSET }) {
            for (int x : new int[] { RIVET_INSET, SIZE - 1 - RIVET_INSET }) {
                pixels[y][x] = OPAQUE | stud;
            }
        }
    }

    private static int hash(int x, int y) {
        int h = x * 73856093 ^ y * 19349663;
        h ^= h >>> 13;
        h *= 0x5BD1E995;
        return (h ^ h >>> 15) & 0x7FFFFFFF;
    }

    private static int scale(int colour, float by) {
        return Math.round(((colour >> 16) & 0xFF) * by) << 16
                | Math.round(((colour >> 8) & 0xFF) * by) << 8
                | Math.round((colour & 0xFF) * by);
    }

    private static int shift(int colour, int by) {
        return channel(colour, 16, by) | channel(colour, 8, by) | channel(colour, 0, by);
    }

    private static int grain(int colour, int x, int y) {
        int shift = (((x * 7 + y * 13) % 5) - 2) * 4;
        return shift(colour, shift);
    }

    private static int channel(int colour, int at, int shift) {
        int value = Math.clamp(((colour >> at) & 0xFF) + shift, 0, 0xFF);
        return value << at;
    }

    private static int mix(int from, int to, float amount) {
        return blend(from, to, 16, amount) | blend(from, to, 8, amount) | blend(from, to, 0, amount);
    }

    private static int blend(int from, int to, int at, float amount) {
        int start = (from >> at) & 0xFF;
        int end = (to >> at) & 0xFF;
        return Math.round(start + (end - start) * amount) << at;
    }
}
