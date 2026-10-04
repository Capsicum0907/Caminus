package io.github.capsicum0907.fornax;

import java.util.Locale;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum Tier implements StringRepresentable {
    COPPER(0xE07C57, false, 1),
    IRON(0xD5DBE0, false, 1),
    GOLD(0xF0C246, false, 1),
    DIAMOND(0x5BE0D6, false, 1),
    NETHERITE(0xB0A2A5, false, 1),
    NETHER_STAR(0xF3EFD8, false, 1),
    COMPRESSED_NETHER_STAR(0xCBBCE8, true, 4),
    SUPER_COMPRESSED_NETHER_STAR(0x9B7BDF, true, 16);

    public static final Tier SPEED_CEILING = NETHER_STAR;

    public static final Codec<Tier> CODEC = StringRepresentable.fromEnum(Tier::values);

    private final String id = name().toLowerCase(Locale.ROOT);
    private final int colour;
    private final boolean compressed;
    private final int lines;

    Tier(int colour, boolean compressed, int lines) {
        this.colour = colour;
        this.compressed = compressed;
        this.lines = lines;
    }

    public String id() {
        return id;
    }

    public int colour() {
        return colour;
    }

    public boolean compressed() {
        return compressed;
    }

    public int lines() {
        return lines;
    }

    public int rung() {
        return ordinal() + 1;
    }

    public Tier under() {
        return ordinal() == 0 ? null : values()[ordinal() - 1];
    }

    public static Tier byOrdinal(int ordinal) {
        return values()[Math.clamp(ordinal, 0, values().length - 1)];
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
