# Fornax

English | [日本語](README.ja.md)

Furnaces in eight tiers. Each tier smelts faster and more items at once.

*Fornax* is Latin for a furnace.

> **Status: in development.** The blocks are only in the creative tab. Tier 1 to 6 have no
> recipe yet.

## Target

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.248 |
| Java | 21 |

## Tiers

| Tier | Material | Ticks per smelt | Items per smelt | Slots | Items per second |
|---|---|---|---|---|---|
| — | Vanilla furnace | 200 | 1 | 1 | 0.1 |
| 1 | Copper | 83 | 2 | 1 | 0.48 |
| 2 | Iron | 34 | 4 | 1 | 2.4 |
| 3 | Gold | 14 | 8 | 1 | 11 |
| 4 | Diamond | 6 | 16 | 1 | 53 |
| 5 | Netherite | 2 | 32 | 1 | 320 |
| 6 | Nether Star | 1 | 64 | 1 | 1,280 |
| 7 | Compressed Nether Star | 1 | 128 | 4 | 10,240 |
| 8 | Super Compressed Nether Star | 1 | 256 | 16 | 81,920 |

Ticks per smelt are for a recipe that takes 200 ticks in a vanilla furnace. Other recipes
take proportionally longer or shorter.

## Fuel

Each item costs the same fuel as in a vanilla furnace. One coal smelts 8 items in every tier.

## Slots

A slot holds two smelts' worth of items, or one stack, whichever is more.

## Hoppers and pipes

| Face | |
|---|---|
| Top | Items to smelt. Spread to the emptiest slot |
| Sides | Fuel |
| Bottom | Smelted items, and empty buckets left by lava |

## Recipes

| Tier | Recipe |
|---|---|
| 7 | 8 × Tier 6 around a Ghast Tear |
| 8 | 8 × Tier 7 around a Totem of Undying |

## Config

`serverconfig/fornax-server.toml`, per tier: `ticks` and `batch` (items per smelt).
`capacityBatches` sets how many smelts a slot holds.

## Build

```
run.bat                   # compile and launch a dev client - double-clickable
gradlew build             # produce the jar
gradlew runGameTestServer # run every game test, headless, then exit
gradlew runData           # regenerate models, textures, recipes and language
```

`JAVA_HOME` must point at a JDK 21, or `java` must be on `PATH`.

## License

MIT. See [LICENSE](LICENSE).
