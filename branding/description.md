Caminus adds furnaces in eight tiers, on fuel or on Forge Energy (FE). Each tier smelts faster and more items at once.

## Tiers

| Tier | Material | Ticks per smelt | Items per smelt | Slots | Items per second |
|---|---|---:|---:|---:|---:|
| — | Vanilla furnace | 200 | 1 | 1 | 0.1 |
| 1 | Copper | 83 | 1 | 1 | 0.24 |
| 2 | Iron | 34 | 1 | 1 | 0.59 |
| 3 | Gold | 14 | 1 | 1 | 1.4 |
| 4 | Diamond | 6 | 4 | 1 | 13 |
| 5 | Netherite | 2 | 16 | 1 | 160 |
| 6 | Nether Star | 1 | 64 | 1 | 1,280 |
| 7 | Compressed Nether Star | 1 | 128 | 4 | 10,240 |
| 8 | Super Compressed Nether Star | 1 | 256 | 16 | 81,920 |

Ticks per smelt are for a recipe that takes 200 ticks in a vanilla furnace.

## Furnaces

- Each item costs the same fuel as in a vanilla furnace. One coal smelts 8 items in every tier.
- A slot holds two smelts' worth of items, or one stack, whichever is more.
- The screen is the vanilla furnace's, recipe book included.

## Electric Furnaces

- Each item costs 20 FE per tick of its recipe: 4,000 FE for a recipe that takes 200 ticks.
- A plain Electric Furnace runs at the speed of a vanilla furnace. Tier 1 to 8 match the fuel furnaces.

## Hoppers and pipes

| Face | Furnace | Electric Furnace |
|---|---|---|
| Top | Items to smelt | Items to smelt |
| Sides | Fuel | Energy |
| Bottom | Smelted items, empty buckets | Smelted items |

Energy goes into an Electric Furnace from any face.

## Worth knowing

- Every value can be changed in `config/caminus-server.toml`.
- `hideRecipeBook` in `config/caminus-client.toml` hides the recipe book button.
- Furnaces are mined with a pickaxe: stone for Tier 1 and 2, iron for Tier 3 and 4, diamond above.

## Requirements

NeoForge 21.1.x on Minecraft 1.21.1. MIT licensed.

---

Caminus は、燃料または Forge Energy（FE）で動く8つのティアのかまどを追加します。ティアが上がるほど速く、一度に多く焼けます。

## ティア

| ティア | 素材 | 1回の時間（tick） | 1回に焼く数 | スロット | 1秒あたり |
|---|---|---:|---:|---:|---:|
| — | バニラのかまど | 200 | 1 | 1 | 0.1 |
| 1 | 銅 | 83 | 1 | 1 | 0.24 |
| 2 | 鉄 | 34 | 1 | 1 | 0.59 |
| 3 | 金 | 14 | 1 | 1 | 1.4 |
| 4 | ダイヤモンド | 6 | 4 | 1 | 13 |
| 5 | ネザライト | 2 | 16 | 1 | 160 |
| 6 | ネザースター | 1 | 64 | 1 | 1,280 |
| 7 | 圧縮ネザースター | 1 | 128 | 4 | 10,240 |
| 8 | 超圧縮ネザースター | 1 | 256 | 16 | 81,920 |

1回の時間は、バニラのかまどで 200 tick かかるレシピの場合です。

## かまど

- 1個あたりの燃料はバニラのかまどと同じです。どのティアでも石炭1個で8個焼けます。
- 1つのスロットには「1回に焼く数の2倍」か「1スタック」の多い方まで入ります。
- 画面はバニラのかまどと同じで、レシピ本も使えます。

## 電気かまど

- 1個あたり、レシピの1 tick につき 20 FE 使います。200 tick のレシピなら 4,000 FE です。
- ティアの無い「電気かまど」はバニラのかまどと同じ速さです。Tier 1〜8 は燃料のかまどと同じです。

## ホッパー・パイプ

| 面 | かまど | 電気かまど |
|---|---|---|
| 上 | 焼く物 | 焼く物 |
| 横 | 燃料 | 電力 |
| 下 | 焼けた物・空のバケツ | 焼けた物 |

電気かまどには、どの面からでも電力が入ります。

## 知っておくと便利なこと

- 数値はすべて `config/caminus-server.toml` で変えられます。
- `config/caminus-client.toml` の `hideRecipeBook` でレシピ本ボタンを隠せます。
- 回収にはツルハシが要ります。Tier 1・2 は石、Tier 3・4 は鉄、それ以上はダイヤモンド以上です。

## 動作環境

Minecraft 1.21.1 の NeoForge 21.1.x。MIT ライセンスです。
