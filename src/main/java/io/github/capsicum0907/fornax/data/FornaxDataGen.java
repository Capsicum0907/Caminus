package io.github.capsicum0907.fornax.data;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.common.hash.Hashing;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import io.github.capsicum0907.fornax.Fornax;
import io.github.capsicum0907.fornax.FornaxRegistry;
import io.github.capsicum0907.fornax.FurnaceBlock;
import io.github.capsicum0907.fornax.Kind;
import io.github.capsicum0907.fornax.Rung;
import io.github.capsicum0907.fornax.Tier;

@EventBusSubscriber(modid = Fornax.MODID, value = { Dist.CLIENT, Dist.DEDICATED_SERVER })
public final class FornaxDataGen {
    private static final float OVERLAY_LIFT = 0.01F;
    private FornaxDataGen() {
    }

    @SubscribeEvent
    public static void gather(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        ExistingFileHelper helper = event.getExistingFileHelper();
        ExistingFileHelper.ResourceType texture =
                new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".png", "textures");
        for (Rung rung : Rung.all()) {
            if (rung.vanilla()) {
                continue;
            }
            for (Skins.Face face : Skins.Face.values()) {
                helper.trackGenerated(block(Skins.name(rung, face, false)), texture);
                if (Skins.lights(face)) {
                    helper.trackGenerated(block(Skins.name(rung, face, true)), texture);
                }
            }
        }
        helper.trackGenerated(block(Skins.overlayName(false)), texture);
        helper.trackGenerated(block(Skins.overlayName(true)), texture);

        generator.addProvider(event.includeClient(), new Textures(output));
        generator.addProvider(event.includeClient(), new Models(output, helper));
        generator.addProvider(event.includeClient(), new English(output));
        generator.addProvider(event.includeClient(), new Japanese(output));

        generator.addProvider(event.includeServer(), new Loot(output, lookup));
        generator.addProvider(event.includeServer(), new Recipes(output, lookup));
        generator.addProvider(event.includeServer(), new Tags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new TestStructures(output));
    }

    private static ResourceLocation block(String name) {
        return ResourceLocation.fromNamespaceAndPath(Fornax.MODID, "block/" + name);
    }

    private static List<Block> ours() {
        List<Block> blocks = new ArrayList<>();
        for (Rung rung : Rung.all()) {
            blocks.add(FornaxRegistry.block(rung).get());
        }
        return blocks;
    }

    private static class Textures implements DataProvider {
        private final PackOutput.PathProvider textures;

        Textures(PackOutput output) {
            this.textures = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "textures/block");
        }

        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            List<CompletableFuture<?>> writing = new ArrayList<>();
            for (Rung rung : Rung.all()) {
                if (rung.vanilla()) {
                    continue;
                }
                for (Skins.Face face : Skins.Face.values()) {
                    draw(output, writing, Skins.skin(rung, face, false), Skins.name(rung, face, false));
                    if (Skins.lights(face)) {
                        draw(output, writing, Skins.skin(rung, face, true), Skins.name(rung, face, true));
                    }
                }
            }
            draw(output, writing, Skins.overlaySkin(false), Skins.overlayName(false));
            draw(output, writing, Skins.overlaySkin(true), Skins.overlayName(true));
            return CompletableFuture.allOf(writing.toArray(CompletableFuture[]::new));
        }

        private void draw(CachedOutput output, List<CompletableFuture<?>> writing, int[][] pixels, String name) {
            Path target = textures.file(ResourceLocation.fromNamespaceAndPath(Fornax.MODID, name), "png");
            writing.add(CompletableFuture.runAsync(() -> write(output, pixels, target), Util.backgroundExecutor()));
        }

        @SuppressWarnings("deprecation")
        private static void write(CachedOutput output, int[][] pixels, Path target) {
            try {
                byte[] bytes = Png.encode(pixels);
                output.writeIfNeeded(target, bytes, Hashing.sha1().hashBytes(bytes));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public String getName() {
            return "Fornax textures";
        }
    }

    private static class Models extends BlockStateProvider {
        Models(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, Fornax.MODID, existingFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
            for (Rung rung : Rung.all()) {
                String cold = rung.id();
                ModelFile unlit = model(rung, cold, false);
                ModelFile lit = model(rung, cold + "_on", true);
                horizontalBlock(FornaxRegistry.block(rung).get(),
                        state -> state.getValue(FurnaceBlock.LIT) ? lit : unlit);
                itemModels().withExistingParent(cold, modLoc("block/" + cold));
            }
        }

        private ModelFile model(Rung rung, String name, boolean lit) {
            ResourceLocation vanillaSide = mcLoc("block/furnace_side");
            ResourceLocation vanillaTop = mcLoc("block/furnace_top");
            boolean fire = lit && rung.kind() == Kind.FUEL;
            ResourceLocation vanillaFront = mcLoc(fire ? "block/furnace_front_on" : "block/furnace_front");
            if (rung.vanilla()) {
                return layered(name, vanillaSide, vanillaTop, vanillaFront, modLoc("block/" + Skins.overlayName(lit)));
            }
            ResourceLocation side = modLoc("block/" + Skins.name(rung, Skins.Face.SIDE, false));
            ResourceLocation top = modLoc("block/" + Skins.name(rung, Skins.Face.TOP, false));
            ResourceLocation front = modLoc("block/" + Skins.name(rung, Skins.Face.FRONT, lit));
            return layered(name, side, top, vanillaFront, front);
        }

        private ModelFile layered(String name, ResourceLocation side, ResourceLocation top, ResourceLocation under,
                ResourceLocation over) {
            var model = models().withExistingParent(name, mcLoc("block/block"))
                    .renderType("cutout")
                    .texture("particle", side)
                    .texture("side", side)
                    .texture("top", top)
                    .texture("under", under)
                    .texture("over", over);
            model.element().from(0, 0, 0).to(16, 16, 16)
                    .face(Direction.NORTH).texture("#under").cullface(Direction.NORTH).end()
                    .face(Direction.SOUTH).texture("#side").cullface(Direction.SOUTH).end()
                    .face(Direction.EAST).texture("#side").cullface(Direction.EAST).end()
                    .face(Direction.WEST).texture("#side").cullface(Direction.WEST).end()
                    .face(Direction.UP).texture("#top").cullface(Direction.UP).end()
                    .face(Direction.DOWN).texture("#top").cullface(Direction.DOWN).end()
                    .end();
            model.element().from(0, 0, -OVERLAY_LIFT).to(16, 16, 0)
                    .face(Direction.NORTH).texture("#over").cullface(Direction.NORTH).end()
                    .end();
            return model;
        }
    }

    private static class English extends LanguageProvider {
        English(PackOutput output) {
            super(output, Fornax.MODID, "en_us");
        }

        @Override
        protected void addTranslations() {
            for (Tier tier : Tier.values()) {
                add(FornaxRegistry.furnace(tier).get(), "Furnace (Tier " + tier.rung() + ")");
                add(FornaxRegistry.electric(tier).get(), "Electric Furnace (Tier " + tier.rung() + ")");
            }
            add(FornaxRegistry.electric(null).get(), "Electric Furnace");
            add("itemGroup." + Fornax.MODID, "Fornax");
            add("gui.fornax.energy", "%s / %s FE");
        }
    }

    private static class Japanese extends LanguageProvider {
        Japanese(PackOutput output) {
            super(output, Fornax.MODID, "ja_jp");
        }

        @Override
        protected void addTranslations() {
            for (Tier tier : Tier.values()) {
                add(FornaxRegistry.furnace(tier).get(), "かまど (Tier " + tier.rung() + ")");
                add(FornaxRegistry.electric(tier).get(), "電気かまど (Tier " + tier.rung() + ")");
            }
            add(FornaxRegistry.electric(null).get(), "電気かまど");
            add("itemGroup." + Fornax.MODID, "Fornax");
            add("gui.fornax.energy", "%s / %s FE");
        }
    }

    private static class Loot extends LootTableProvider {
        Loot(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, Set.of(),
                    List.of(new LootTableProvider.SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK)),
                    registries);
        }

        private static class Blocks extends BlockLootSubProvider {
            Blocks(HolderLookup.Provider registries) {
                super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
            }

            @Override
            protected void generate() {
                ours().forEach(this::dropSelf);
            }

            @Override
            protected Iterable<Block> getKnownBlocks() {
                return ours();
            }
        }
    }

    private static class Tags extends BlockTagsProvider {
        Tags(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                ExistingFileHelper existingFileHelper) {
            super(output, registries, Fornax.MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
            ours().forEach(pickaxe::add);
            for (Rung rung : Rung.all()) {
                if (!rung.vanilla()) {
                    tag(tool(rung.tier())).add(FornaxRegistry.block(rung).get());
                }
            }
        }

        private static TagKey<Block> tool(Tier tier) {
            return switch (tier) {
                case COPPER, IRON -> BlockTags.NEEDS_STONE_TOOL;
                case GOLD, DIAMOND -> BlockTags.NEEDS_IRON_TOOL;
                case NETHERITE, NETHER_STAR, COMPRESSED_NETHER_STAR, SUPER_COMPRESSED_NETHER_STAR ->
                        BlockTags.NEEDS_DIAMOND_TOOL;
            };
        }
    }

    private static class Recipes extends RecipeProvider {
        Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected void buildRecipes(RecipeOutput output) {
            for (Rung rung : Rung.all()) {
                if (rung.vanilla()) {
                    vanillaRung(output, rung);
                } else if (rung.tier().compressed()) {
                    compressed(output, rung);
                } else {
                    laddered(output, rung);
                }
            }
        }

        private static ItemLike under(Rung rung) {
            Rung below = rung.under();
            return below == null ? Items.FURNACE : FornaxRegistry.block(below).get();
        }

        private static ItemLike core(Kind kind) {
            return switch (kind) {
                case FUEL -> Items.COAL_BLOCK;
                case ELECTRIC -> Items.REDSTONE_BLOCK;
            };
        }

        private void vanillaRung(RecipeOutput output, Rung rung) {
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, FornaxRegistry.block(rung).get())
                    .pattern("CCC")
                    .pattern("CFC")
                    .pattern("CRC")
                    .define('C', Items.COBBLESTONE)
                    .define('F', Items.FURNACE)
                    .define('R', core(rung.kind()))
                    .unlockedBy("has_under", has(Items.FURNACE))
                    .save(output);
        }

        private void laddered(RecipeOutput output, Rung rung) {
            ItemLike under = under(rung);
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, FornaxRegistry.block(rung).get())
                    .pattern("MMM")
                    .pattern("MFM")
                    .pattern("MCM")
                    .define('M', material(rung.tier()))
                    .define('F', under)
                    .define('C', core(rung.kind()))
                    .unlockedBy("has_under", has(under))
                    .save(output);
        }

        private void compressed(RecipeOutput output, Rung rung) {
            Tier tier = rung.tier();
            ItemLike under = under(rung);
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, FornaxRegistry.block(rung).get())
                    .pattern("PPP")
                    .pattern("PMP")
                    .pattern("PPP")
                    .define('P', under)
                    .define('M', medium(tier))
                    .unlockedBy("has_under", has(under))
                    .save(output);
        }

        private static ItemLike material(Tier tier) {
            return switch (tier) {
                case COPPER -> Items.COPPER_INGOT;
                case IRON -> Items.IRON_INGOT;
                case GOLD -> Items.GOLD_INGOT;
                case DIAMOND -> Items.DIAMOND;
                case NETHERITE -> Items.NETHERITE_INGOT;
                case NETHER_STAR -> Items.NETHER_STAR;
                case COMPRESSED_NETHER_STAR, SUPER_COMPRESSED_NETHER_STAR ->
                        throw new IllegalStateException(tier.id() + " is made of the tier below");
            };
        }

        private static ItemLike medium(Tier tier) {
            return switch (tier) {
                case COMPRESSED_NETHER_STAR -> Items.GHAST_TEAR;
                case SUPER_COMPRESSED_NETHER_STAR -> Items.TOTEM_OF_UNDYING;
                case COPPER, IRON, GOLD, DIAMOND, NETHERITE, NETHER_STAR ->
                        throw new IllegalStateException(tier.id() + " is not compressed");
            };
        }
    }
}
