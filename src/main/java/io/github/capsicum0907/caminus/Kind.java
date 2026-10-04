package io.github.capsicum0907.caminus;

import java.util.Locale;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum Kind implements StringRepresentable {
    FUEL("furnace"),
    ELECTRIC("electric_furnace");

    public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

    private final String block;

    Kind(String block) {
        this.block = block;
    }

    public String block() {
        return block;
    }

    public boolean hasVanillaRung() {
        return this == ELECTRIC;
    }

    public static Kind byOrdinal(int ordinal) {
        return values()[Math.clamp(ordinal, 0, values().length - 1)];
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
