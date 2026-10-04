package io.github.capsicum0907.caminus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

import io.github.capsicum0907.caminus.data.TestStructures;

@GameTestHolder(Caminus.MODID)
@PrefixGameTestTemplate(false)
public final class CaminusTests {
    private static final BlockPos WHERE = new BlockPos(2, 1, 2);
    private static final int VANILLA_TIMEOUT = 300;
    private static final int LEFTOVER_TIMEOUT = 1_800;
    private static final ResourceLocation RAW_IRON_RECIPE =
            ResourceLocation.withDefaultNamespace("iron_ingot_from_smelting_raw_iron");

    private CaminusTests() {
    }

    private static void check(boolean held, String what) {
        if (!held) {
            throw new GameTestAssertException(what);
        }
    }

    private static FurnaceBlockEntity place(GameTestHelper helper, Tier tier) {
        helper.setBlock(WHERE, CaminusRegistry.furnace(tier).get());
        return (FurnaceBlockEntity) helper.getBlockEntity(WHERE);
    }

    private static IItemHandler face(GameTestHelper helper, Direction side) {
        return helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(WHERE), side);
    }

    private static void expect(Tier tier, int ticks, int batch, int lines) {
        check(CaminusConfig.ticks(tier) == ticks, tier.id() + " should take " + ticks + " ticks, not " + CaminusConfig.ticks(tier));
        check(CaminusConfig.batch(tier) == batch, tier.id() + " should smelt " + batch + " at once, not " + CaminusConfig.batch(tier));
        check(tier.lines() == lines, tier.id() + " should have " + lines + " lines, not " + tier.lines());
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void theTiersMatchTheTable(GameTestHelper helper) {
        expect(Tier.COPPER, 83, 1, 1);
        expect(Tier.IRON, 34, 1, 1);
        expect(Tier.GOLD, 14, 1, 1);
        expect(Tier.DIAMOND, 6, 4, 1);
        expect(Tier.NETHERITE, 2, 16, 1);
        expect(Tier.NETHER_STAR, 1, 64, 1);
        expect(Tier.COMPRESSED_NETHER_STAR, 1, 128, 4);
        expect(Tier.SUPER_COMPRESSED_NETHER_STAR, 1, 256, 16);
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void copperSmeltsOneAfterEightyThreeTicks(GameTestHelper helper) {
        FurnaceBlockEntity furnace = place(helper, Tier.COPPER);
        furnace.inputs().put(0, new ItemStack(Items.RAW_IRON, 2));
        furnace.fuel().setStackInSlot(0, new ItemStack(Items.COAL));
        helper.runAtTickTime(3, () -> {
            check(furnace.fuel().getStackInSlot(0).isEmpty(), "the coal is lit as soon as smelting starts");
            check(furnace.getBlockState().getValue(FurnaceBlock.LIT), "and the furnace is lit");
        });
        helper.runAtTickTime(80, () -> check(furnace.outputs().getStackInSlot(0).isEmpty(),
                "nothing should be done before 83 ticks"));
        helper.runAtTickTime(90, () -> {
            ItemStack out = furnace.outputs().getStackInSlot(0);
            check(out.is(Items.IRON_INGOT) && out.getCount() == 1, "one should come out, not " + out);
            check(furnace.inputs().getStackInSlot(0).getCount() == 1, "and the other should still be waiting");
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void oneCoalSmeltsExactlyEight(GameTestHelper helper) {
        FurnaceBlockEntity furnace = place(helper, Tier.DIAMOND);
        furnace.inputs().put(0, new ItemStack(Items.RAW_IRON, 16));
        furnace.fuel().setStackInSlot(0, new ItemStack(Items.COAL));
        helper.runAtTickTime(60, () -> {
            check(furnace.outputs().getStackInSlot(0).getCount() == 8,
                    "one coal is eight items, not " + furnace.outputs().getStackInSlot(0).getCount());
            check(furnace.inputs().getStackInSlot(0).getCount() == 8, "the other eight stay where they are");
            check(furnace.fuel().getStackInSlot(0).isEmpty(), "the coal is gone");
            check(furnace.heat() == 0, "with nothing left over, not " + furnace.heat());
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void aShortBatchSmeltsWhatIsThere(GameTestHelper helper) {
        FurnaceBlockEntity furnace = place(helper, Tier.NETHER_STAR);
        furnace.inputs().put(0, new ItemStack(Items.RAW_IRON, 3));
        furnace.fuel().setStackInSlot(0, new ItemStack(Items.COAL));
        helper.runAtTickTime(10, () -> {
            check(furnace.outputs().getStackInSlot(0).getCount() == 3, "three in, three out");
            check(furnace.used(RAW_IRON_RECIPE) == 3, "and counted for experience");
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void aSlotHoldsTwoBatchesOrAStack(GameTestHelper helper) {
        FurnaceBlockEntity copper = place(helper, Tier.COPPER);
        check(copper.inputs().capacity(new ItemStack(Items.ENDER_PEARL)) == 16,
                "copper takes a stack of pearls, sixteen, not " + copper.inputs().capacity(new ItemStack(Items.ENDER_PEARL)));
        check(copper.inputs().capacity(new ItemStack(Items.RAW_IRON)) == 64, "and a stack of raw iron, sixty-four");
        FurnaceBlockEntity star = place(helper, Tier.NETHER_STAR);
        check(star.inputs().capacity(new ItemStack(Items.ENDER_PEARL)) == 128, "a nether star tier takes two batches of pearls");
        ItemStack left = face(helper, Direction.UP).insertItem(0, new ItemStack(Items.RAW_IRON, 200), false);
        check(star.inputs().getStackInSlot(0).getCount() == 128, "two batches go in, not " + star.inputs().getStackInSlot(0).getCount());
        check(left.getCount() == 72, "and the rest is handed back, not " + left.getCount());
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void moreThanAStackSurvivesSaving(GameTestHelper helper) {
        FurnaceBlockEntity furnace = place(helper, Tier.NETHER_STAR);
        furnace.inputs().put(0, new ItemStack(Items.RAW_IRON, 128));
        furnace.outputs().put(0, new ItemStack(Items.IRON_INGOT, 100));
        var registries = helper.getLevel().registryAccess();
        CompoundTag saved = furnace.saveWithoutMetadata(registries);
        FurnaceBlockEntity loaded = new FurnaceBlockEntity(furnace.getBlockPos(), furnace.getBlockState());
        loaded.loadWithComponents(saved, registries);
        check(loaded.inputs().getStackInSlot(0).getCount() == 128,
                "128 should come back, not " + loaded.inputs().getStackInSlot(0).getCount());
        check(loaded.outputs().getStackInSlot(0).getCount() == 100, "and so should 100 ingots");
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void compressedRunsAllFourLines(GameTestHelper helper) {
        FurnaceBlockEntity furnace = place(helper, Tier.COMPRESSED_NETHER_STAR);
        for (int line = 0; line < 4; line++) {
            furnace.inputs().put(line, new ItemStack(Items.RAW_IRON, 128));
        }
        furnace.fuel().setStackInSlot(0, new ItemStack(Items.COAL, 64));
        helper.runAtTickTime(5, () -> {
            for (int line = 0; line < 4; line++) {
                ItemStack out = furnace.outputs().getStackInSlot(line);
                check(out.getCount() == 128, "line " + line + " should have made 128, not " + out.getCount());
            }
            check(furnace.fuel().getStackInSlot(0).isEmpty(), "with exactly sixty-four coal");
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void theTopSpreadsAcrossLines(GameTestHelper helper) {
        FurnaceBlockEntity furnace = place(helper, Tier.COMPRESSED_NETHER_STAR);
        IItemHandler top = face(helper, Direction.UP);
        for (int item = 0; item < 4; item++) {
            top.insertItem(0, new ItemStack(Items.RAW_IRON), false);
        }
        for (int line = 0; line < 4; line++) {
            check(furnace.inputs().getStackInSlot(line).getCount() == 1, "one each, but line " + line + " has "
                    + furnace.inputs().getStackInSlot(line).getCount());
        }
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void theSidesTakeFuelAndTheBottomGivesBack(GameTestHelper helper) {
        FurnaceBlockEntity furnace = place(helper, Tier.COPPER);
        IItemHandler side = face(helper, Direction.NORTH);
        check(!side.insertItem(0, new ItemStack(Items.RAW_IRON), false).isEmpty(), "the side refuses what does not burn");
        check(side.insertItem(0, new ItemStack(Items.LAVA_BUCKET), false).isEmpty(), "and takes what does");
        face(helper, Direction.UP).insertItem(0, new ItemStack(Items.RAW_IRON), false);
        helper.runAtTickTime(90, () -> {
            IItemHandler bottom = face(helper, Direction.DOWN);
            check(bottom.extractItem(0, 64, false).is(Items.IRON_INGOT), "the bottom gives the ingot");
            check(bottom.extractItem(1, 1, false).is(Items.BUCKET), "and the empty bucket");
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void everyTierHasARecipe(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        for (Tier tier : Tier.values()) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Caminus.MODID, CaminusRegistry.id(tier));
            check(recipes.byKey(id).isPresent(), tier.id() + " should have a recipe");
        }
        helper.succeed();
    }

    private static FurnaceBlockEntity placeElectric(GameTestHelper helper, Tier tier) {
        helper.setBlock(WHERE, CaminusRegistry.electric(tier).get());
        return (FurnaceBlockEntity) helper.getBlockEntity(WHERE);
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void theElectricBufferMatchesTheTable(GameTestHelper helper) {
        check(placeElectric(helper, null).energy().getMaxEnergyStored() == 8_000, "the vanilla rung holds 8,000 FE");
        check(placeElectric(helper, Tier.COPPER).energy().getMaxEnergyStored() == 9_600, "copper holds 9,600 FE");
        check(placeElectric(helper, Tier.SUPER_COMPRESSED_NETHER_STAR).energy().getMaxEnergyStored() == Integer.MAX_VALUE,
                "the top tier holds as much as an int can");
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR, timeoutTicks = VANILLA_TIMEOUT)
    public static void oneSmeltCostsFourThousandFe(GameTestHelper helper) {
        FurnaceBlockEntity furnace = placeElectric(helper, null);
        furnace.inputs().put(0, new ItemStack(Items.RAW_IRON));
        furnace.energy().receiveEnergy(4_000, false);
        helper.runAtTickTime(195, () -> check(furnace.outputs().getStackInSlot(0).isEmpty(),
                "the vanilla rung takes 200 ticks"));
        helper.runAtTickTime(210, () -> {
            check(furnace.outputs().getStackInSlot(0).getCount() == 1, "one ingot after 200 ticks");
            check(furnace.energy().getEnergyStored() == 0, "for exactly 4,000 FE, leaving " + furnace.energy().getEnergyStored());
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.FLOOR, timeoutTicks = VANILLA_TIMEOUT)
    public static void oneFeShortSmeltsNothing(GameTestHelper helper) {
        FurnaceBlockEntity furnace = placeElectric(helper, null);
        furnace.inputs().put(0, new ItemStack(Items.RAW_IRON));
        furnace.energy().receiveEnergy(3_999, false);
        helper.runAtTickTime(220, () -> {
            check(furnace.outputs().getStackInSlot(0).isEmpty(), "3,999 FE is not enough for one smelt");
            check(furnace.energy().getEnergyStored() == 3_999, "and none of it is spent");
            check(!furnace.getBlockState().getValue(FurnaceBlock.LIT), "and the furnace is not lit");
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void theBatchStopsWhereTheEnergyDoes(GameTestHelper helper) {
        FurnaceBlockEntity furnace = placeElectric(helper, Tier.DIAMOND);
        furnace.inputs().put(0, new ItemStack(Items.RAW_IRON, 16));
        furnace.energy().receiveEnergy(8_000, false);
        helper.runAtTickTime(40, () -> {
            check(furnace.outputs().getStackInSlot(0).getCount() == 2, "8,000 FE smelts two, not "
                    + furnace.outputs().getStackInSlot(0).getCount());
            check(furnace.inputs().getStackInSlot(0).getCount() == 14, "and the rest waits");
            check(furnace.energy().getEnergyStored() == 0, "with nothing left");
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void energySurvivesSaving(GameTestHelper helper) {
        FurnaceBlockEntity furnace = placeElectric(helper, Tier.COPPER);
        furnace.energy().receiveEnergy(5_432, false);
        var registries = helper.getLevel().registryAccess();
        CompoundTag saved = furnace.saveWithoutMetadata(registries);
        FurnaceBlockEntity loaded = new FurnaceBlockEntity(furnace.getBlockPos(), furnace.getBlockState());
        loaded.loadWithComponents(saved, registries);
        check(loaded.energy().getEnergyStored() == 5_432, "5,432 FE should come back, not " + loaded.energy().getEnergyStored());
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void everyFaceTakesEnergy(GameTestHelper helper) {
        placeElectric(helper, Tier.COPPER);
        var level = helper.getLevel();
        BlockPos at = helper.absolutePos(WHERE);
        check(level.getCapability(Capabilities.EnergyStorage.BLOCK, at, null) != null, "with no side");
        for (Direction side : Direction.values()) {
            check(level.getCapability(Capabilities.EnergyStorage.BLOCK, at, side) != null, "from " + side);
        }
        place(helper, Tier.COPPER);
        check(level.getCapability(Capabilities.EnergyStorage.BLOCK, at, Direction.NORTH) == null,
                "a fuel furnace takes no energy");
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void anElectricFurnaceHasNoFuelSlot(GameTestHelper helper) {
        FurnaceBlockEntity furnace = placeElectric(helper, Tier.COPPER);
        check(furnace.fuel() == null, "there is nowhere to put fuel");
        check(face(helper, Direction.NORTH) == null, "so the sides take energy and no items");
        check(face(helper, Direction.UP) != null, "while the top still takes things to smelt");
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void everyElectricRungHasARecipe(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        check(recipes.byKey(ResourceLocation.fromNamespaceAndPath(Caminus.MODID, "electric_furnace")).isPresent(),
                "the vanilla rung should have a recipe");
        for (Tier tier : Tier.values()) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Caminus.MODID, tier.id() + "_electric_furnace");
            check(recipes.byKey(id).isPresent(), id + " should have a recipe");
        }
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR, timeoutTicks = LEFTOVER_TIMEOUT)
    public static void leftoverFireBurnsAwayLikeVanilla(GameTestHelper helper) {
        FurnaceBlockEntity furnace = place(helper, Tier.COPPER);
        furnace.inputs().put(0, new ItemStack(Items.RAW_IRON));
        furnace.fuel().setStackInSlot(0, new ItemStack(Items.COAL));
        helper.runAtTickTime(100, () -> {
            check(furnace.outputs().getStackInSlot(0).getCount() == 1, "the iron is done");
            check(furnace.getBlockState().getValue(FurnaceBlock.LIT), "and the rest of the coal is still burning");
            check(furnace.heat() < 1_400, "and burning down, at " + furnace.heat());
        });
        helper.runAtTickTime(1_700, () -> {
            check(furnace.heat() == 0, "the coal has burnt out, not " + furnace.heat());
            check(!furnace.getBlockState().getValue(FurnaceBlock.LIT), "and the furnace has gone out");
            helper.succeed();
        });
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void higherTiersNeedBetterPickaxes(GameTestHelper helper) {
        check(CaminusRegistry.furnace(Tier.IRON).get().defaultBlockState().is(BlockTags.NEEDS_STONE_TOOL), "iron needs stone");
        check(CaminusRegistry.furnace(Tier.GOLD).get().defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL), "gold needs iron");
        check(CaminusRegistry.electric(Tier.NETHERITE).get().defaultBlockState().is(BlockTags.NEEDS_DIAMOND_TOOL),
                "netherite needs diamond");
        var vanilla = CaminusRegistry.electric(null).get().defaultBlockState();
        check(!vanilla.is(BlockTags.NEEDS_STONE_TOOL) && !vanilla.is(BlockTags.NEEDS_IRON_TOOL)
                && !vanilla.is(BlockTags.NEEDS_DIAMOND_TOOL), "the plain electric furnace needs any pickaxe");
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void noSideShowsEverything(GameTestHelper helper) {
        FurnaceBlockEntity furnace = place(helper, Tier.COPPER);
        furnace.inputs().put(0, new ItemStack(Items.RAW_IRON, 5));
        furnace.fuel().setStackInSlot(0, new ItemStack(Items.COAL, 3));
        furnace.outputs().put(0, new ItemStack(Items.IRON_INGOT, 7));
        IItemHandler whole = face(helper, null);
        check(whole.getSlots() == 3, "input, fuel and output, not " + whole.getSlots() + " slots");
        check(whole.getStackInSlot(0).is(Items.RAW_IRON) && whole.getStackInSlot(0).getCount() == 5, "the input");
        check(whole.getStackInSlot(1).is(Items.COAL) && whole.getStackInSlot(1).getCount() == 3, "the fuel");
        check(whole.getStackInSlot(2).is(Items.IRON_INGOT) && whole.getStackInSlot(2).getCount() == 7, "the output");
        check(whole.extractItem(0, 5, true).isEmpty(), "the input cannot be pulled out");
        check(whole.extractItem(1, 3, true).isEmpty(), "nor can burning fuel");
        check(whole.extractItem(2, 7, true).getCount() == 7, "the output can");
        whole.insertItem(0, new ItemStack(Items.COAL), false);
        check(furnace.fuel().getStackInSlot(0).getCount() == 4, "coal put in goes to the fuel");
        whole.insertItem(0, new ItemStack(Items.RAW_IRON), false);
        check(furnace.inputs().getStackInSlot(0).getCount() == 6, "raw iron put in goes to the input");
        placeElectric(helper, Tier.COPPER);
        check(face(helper, null).getSlots() == 2, "an electric furnace shows input and output only");
        helper.succeed();
    }
}
