# Fornax

A furnace that is faster, and smelts more than one thing at once.

*Fornax* is Latin for a furnace.

> **Status: scaffold only.** The mod loads and does nothing.

## Target

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.248 |
| Java | 21 |

1.21.1 is the version large tech mods stayed on, so it is where this mod is useful.

## Design

A furnace that smelts several things at once, each faster than the vanilla one.

**Several lines, not one clever one.** Multiple slots could mean a shared buffer
feeding a parallel process, or simply *n* independent furnaces sharing a housing.
This is the second. Both the implementation and what the player sees stay simple,
and simple is what survives being changed later.

**Speed is a factor on ticks per item, and fuel moves with it.** If a faster
furnace burned the same coal for the same number of items, the upgrade would
quietly be a fuel-efficiency upgrade as well, which is a different thing and a
larger one. What is bought here is time.

The one thing worth being careful about is the recipe lookup: asking the recipe
manager for a match every tick means walking every smelting recipe every tick.
The game provides a cached check for exactly this, and it is used.

No dependency on [Accumulator](https://github.com/Capsicum0907/Accumulator). If
running on Forge Energy is ever wanted, it arrives later as an optional dependency
— tying the two together at the start would mean neither stands alone.

## Build

```
run.bat                   # compile and launch a dev client - double-clickable
gradlew build             # produce the jar
gradlew runGameTestServer # run every game test, headless, then exit
gradlew runData           # regenerate models, recipes and language
```

`JAVA_HOME` must point at a JDK 21, or `java` must be on `PATH`.

## Roadmap

- [x] **0** — scaffold; the mod loads
- [ ] **1** — the feature above, in a form that can be watched
- [ ] **2** — checked by game tests rather than by eye

## Related

One of a set of small, independent mods, each doing one thing and depending on
none of the others: [Fodina](https://github.com/Capsicum0907/Fodina),
[Trivium](https://github.com/Capsicum0907/Trivium),
[Magnes](https://github.com/Capsicum0907/Magnes),
[Cella](https://github.com/Capsicum0907/Cella),
[Acervus](https://github.com/Capsicum0907/Acervus),
[Fornax](https://github.com/Capsicum0907/Fornax),
[Accumulator](https://github.com/Capsicum0907/Accumulator).

## License

Not decided yet. Until it is, the metadata says All Rights Reserved.
