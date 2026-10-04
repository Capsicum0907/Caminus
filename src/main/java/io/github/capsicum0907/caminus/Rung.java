package io.github.capsicum0907.caminus;

import java.util.ArrayList;
import java.util.List;

public record Rung(Kind kind, Tier tier) {
    private static final int VANILLA_LINES = 1;
    private static final int VANILLA_BATCH = 1;

    public static List<Rung> all() {
        List<Rung> rungs = new ArrayList<>();
        for (Kind kind : Kind.values()) {
            if (kind.hasVanillaRung()) {
                rungs.add(new Rung(kind, null));
            }
            for (Tier tier : Tier.values()) {
                rungs.add(new Rung(kind, tier));
            }
        }
        return rungs;
    }

    public boolean vanilla() {
        return tier == null;
    }

    public String id() {
        return vanilla() ? kind.block() : tier.id() + "_" + kind.block();
    }

    public int lines() {
        return vanilla() ? VANILLA_LINES : tier.lines();
    }

    public int batch() {
        return vanilla() ? VANILLA_BATCH : CaminusConfig.batch(tier);
    }

    public int ticks() {
        return vanilla() ? CaminusConfig.STANDARD : CaminusConfig.ticks(tier);
    }

    public Rung under() {
        if (vanilla()) {
            return null;
        }
        Tier below = tier.under();
        if (below == null && !kind.hasVanillaRung()) {
            return null;
        }
        return new Rung(kind, below);
    }
}
