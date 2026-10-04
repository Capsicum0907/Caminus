package io.github.capsicum0907.fornax.data;

import java.util.Locale;

import io.github.capsicum0907.fornax.FornaxRegistry;
import io.github.capsicum0907.fornax.Tier;

public final class Skins {
    public static final int SIZE = 16;

    private static final int OPAQUE = 0xFF000000;
    private static final int SHINE = 0xFFFFFF;
    private static final int PATINA = 0x4FA88A;
    private static final int CAVITY = 0x1A1614;
    private static final int ASH = 0x3A3532;
    private static final int EMBER = 0xC8461B;
    private static final int MOUTH_LIT = 0xFF9A2E;
    private static final int FIRE_CORE = 0xFFE08A;

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
    private static final int FLICKER = 18;
    private static final int GRILL_EVERY = 3;

    private static final int MOUTH_LEFT = 3;
    private static final int MOUTH_RIGHT = 12;
    private static final int MOUTH_TOP = 8;
    private static final int MOUTH_BOTTOM = 13;
    private static final int VENT_LEFT = 4;
    private static final int VENT_RIGHT = 11;
    private static final int[] VENT_ROWS = { 3, 5 };
    private static final int RIVET_INSET = 2;
    private static final int PLATE_INSET = 3;

    public enum Face {
        FRONT, SIDE, TOP;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private Skins() {
    }

    public static String name(Tier tier, Face face, boolean lit) {
        return FornaxRegistry.id(tier) + "_" + face.id() + (lit ? "_on" : "");
    }

    public static boolean lights(Face face) {
        return face == Face.FRONT;
    }

    public static int[][] skin(Tier tier, Face face, boolean lit) {
        int[][] pixels = new int[SIZE][SIZE];
        plated(pixels, tier, 0, 0, SIZE - 1, SIZE - 1);
        switch (face) {
            case FRONT -> front(pixels, tier, lit);
            case SIDE -> rivets(pixels, tier);
            case TOP -> {
                plated(pixels, tier, PLATE_INSET, PLATE_INSET, SIZE - 1 - PLATE_INSET, SIZE - 1 - PLATE_INSET);
                rivets(pixels, tier);
            }
        }
        return pixels;
    }

    private static void front(int[][] pixels, Tier tier, boolean lit) {
        int slot = scale(tier.colour(), METAL_DEEP);
        for (int row : VENT_ROWS) {
            paint(pixels, VENT_LEFT, row, VENT_RIGHT, row, (x, y) -> slot);
        }
        plated(pixels, tier, MOUTH_LEFT - 1, MOUTH_TOP - 1, MOUTH_RIGHT + 1, MOUTH_BOTTOM + 1);
        paint(pixels, MOUTH_LEFT, MOUTH_TOP, MOUTH_RIGHT, MOUTH_BOTTOM, (x, y) -> {
            if (x % GRILL_EVERY == GRILL_EVERY - 1) {
                return scale(tier.colour(), METAL_DARK);
            }
            if (lit) {
                return fire(x, y);
            }
            return y == MOUTH_BOTTOM ? ASH : CAVITY;
        });
    }

    private static int fire(int x, int y) {
        float down = (y - MOUTH_TOP) / (float) Math.max(1, MOUTH_BOTTOM - MOUTH_TOP);
        if (down > 0.5F && hash(x, y) % 100 < FLICKER) {
            return FIRE_CORE;
        }
        return mix(EMBER, MOUTH_LIT, down);
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
