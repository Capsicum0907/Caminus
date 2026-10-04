# Fornax

English | [日本語](README.ja.md)

Furnaces in eight tiers. Each tier smelts faster and more items at once.

*Fornax* is Latin for a furnace.

> **Status: in development.**

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
| 1 | Copper | 83 | 1 | 1 | 0.24 |
| 2 | Iron | 34 | 1 | 1 | 0.59 |
| 3 | Gold | 14 | 1 | 1 | 1.4 |
| 4 | Diamond | 6 | 4 | 1 | 13 |
| 5 | Netherite | 2 | 16 | 1 | 160 |
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

Tier 1 to 6:

```
M S M
M F M
M C M
```

M: the tier's material. S: Sugar. F: the furnace one tier below (a vanilla Furnace for Tier 1). C: Block of Coal.

| Tier | Material |
|---|---|
| 1 | Copper Ingot |
| 2 | Iron Ingot |
| 3 | Gold Ingot |
| 4 | Diamond |
| 5 | Netherite Ingot |
| 6 | Nether Star |

Tier 7 and 8:

| Tier | Recipe |
|---|---|
| 7 | 8 × Tier 6 around a Ghast Tear |
| 8 | 8 × Tier 7 around a Totem of Undying |

## Config

`config/fornax-server.toml`, per tier: `ticks` and `batch` (items per smelt).
`capacityBatches` sets how many smelts a slot holds.

`config/fornax-client.toml`: `hideRecipeBook` hides the recipe book button.

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
