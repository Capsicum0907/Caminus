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
import io.github.capsicum0907.fornax.Tier;

@EventBusSubscriber(modid = Fornax.MODID, value = { Dist.CLIENT, Dist.DEDICATED_SERVER })
public final class FornaxDataGen {
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
        for (Tier tier : Tier.values()) {
            for (Skins.Face face : Skins.Face.values()) {
                helper.trackGenerated(block(Skins.name(tier, face, false)), texture);
                if (Skins.lights(face)) {
                    helper.trackGenerated(block(Skins.name(tier, face, true)), texture);
                }
            }
        }

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
        for (Tier tier : Tier.values()) {
            blocks.add(FornaxRegistry.furnace(tier).get());
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
            for (Tier tier : Tier.values()) {
                for (Skins.Face face : Skins.Face.values()) {
                    draw(output, writing, Skins.skin(tier, face, false), Skins.name(tier, face, false));
                    if (Skins.lights(face)) {
                        draw(output, writing, Skins.skin(tier, face, true), Skins.name(tier, face, true));
                    }
                }
            }
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
            for (Tier tier : Tier.values()) {
                String cold = FornaxRegistry.id(tier);
                ModelFile unlit = model(tier, cold, false);
                ModelFile lit = model(tier, cold + "_on", true);
                horizontalBlock(FornaxRegistry.furnace(tier).get(),
                        state -> state.getValue(FurnaceBlock.LIT) ? lit : unlit);
                itemModels().withExistingParent(cold, modLoc("block/" + cold));
            }
        }

        private ModelFile model(Tier tier, String name, boolean lit) {
            return models().orientableWithBottom(name,
                    modLoc("block/" + Skins.name(tier, Skins.Face.SIDE, false)),
                    modLoc("block/" + Skins.name(tier, Skins.Face.FRONT, lit)),
                    modLoc("block/" + Skins.name(tier, Skins.Face.TOP, false)),
                    modLoc("block/" + Skins.name(tier, Skins.Face.TOP, false)));
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
            }
            add("itemGroup." + Fornax.MODID, "Fornax");
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
            }
            add("itemGroup." + Fornax.MODID, "Fornax");
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
        }
    }

    private static class Recipes extends RecipeProvider {
        Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected void buildRecipes(RecipeOutput output) {
            for (Tier tier : Tier.values()) {
                if (!tier.compressed()) {
                    continue;
                }
                ItemLike under = FornaxRegistry.furnace(tier.under()).get();
                ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, FornaxRegistry.furnace(tier).get())
                        .pattern("PPP")
                        .pattern("PMP")
                        .pattern("PPP")
                        .define('P', under)
                        .define('M', medium(tier))
                        .unlockedBy("has_" + FornaxRegistry.id(tier.under()), has(under))
                        .save(output);
            }
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
