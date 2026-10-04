package io.github.capsicum0907.fornax;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

import io.github.capsicum0907.fornax.data.TestStructures;

@GameTestHolder(Fornax.MODID)
@PrefixGameTestTemplate(false)
public final class FornaxTests {
    private static final BlockPos WHERE = new BlockPos(2, 1, 2);
    private static final ResourceLocation RAW_IRON_RECIPE =
            ResourceLocation.withDefaultNamespace("iron_ingot_from_smelting_raw_iron");

    private FornaxTests() {
    }

    private static void check(boolean held, String what) {
        if (!held) {
            throw new GameTestAssertException(what);
        }
    }

    private static FurnaceBlockEntity place(GameTestHelper helper, Tier tier) {
        helper.setBlock(WHERE, FornaxRegistry.furnace(tier).get());
        return (FurnaceBlockEntity) helper.getBlockEntity(WHERE);
    }

    private static IItemHandler face(GameTestHelper helper, Direction side) {
        return helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(WHERE), side);
    }

    private static void expect(Tier tier, int ticks, int batch, int lines) {
        check(FornaxConfig.ticks(tier) == ticks, tier.id() + " should take " + ticks + " ticks, not " + FornaxConfig.ticks(tier));
        check(FornaxConfig.batch(tier) == batch, tier.id() + " should smelt " + batch + " at once, not " + FornaxConfig.batch(tier));
        check(tier.lines() == lines, tier.id() + " should have " + lines + " lines, not " + tier.lines());
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void theTiersMatchTheTable(GameTestHelper helper) {
        expect(Tier.COPPER, 83, 2, 1);
        expect(Tier.IRON, 34, 4, 1);
        expect(Tier.GOLD, 14, 8, 1);
        expect(Tier.DIAMOND, 6, 16, 1);
        expect(Tier.NETHERITE, 2, 32, 1);
        expect(Tier.NETHER_STAR, 1, 64, 1);
        expect(Tier.COMPRESSED_NETHER_STAR, 1, 128, 4);
        expect(Tier.SUPER_COMPRESSED_NETHER_STAR, 1, 256, 16);
        helper.succeed();
    }

    @GameTest(template = TestStructures.FLOOR)
    public static void copperSmeltsTwoAtOnceAfterEightyThreeTicks(GameTestHelper helper) {
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
            check(out.is(Items.IRON_INGOT) && out.getCount() == 2, "both should come out together, not " + out);
            check(furnace.inputs().getStackInSlot(0).isEmpty(), "and the input should be used up");
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
            check(furnace.heat() == 1000, "and five items of burning are kept, not " + furnace.heat());
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
}
