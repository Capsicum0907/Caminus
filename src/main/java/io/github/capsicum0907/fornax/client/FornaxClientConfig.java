package io.github.capsicum0907.fornax.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class FornaxClientConfig {
    private static final ModConfigSpec.BooleanValue HIDE_RECIPE_BOOK;

    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        HIDE_RECIPE_BOOK = builder.define("hideRecipeBook", false);
        SPEC = builder.build();
    }

    private FornaxClientConfig() {
    }

    public static boolean hideRecipeBook() {
        return SPEC.isLoaded() ? HIDE_RECIPE_BOOK.get() : HIDE_RECIPE_BOOK.getDefault();
    }
}
