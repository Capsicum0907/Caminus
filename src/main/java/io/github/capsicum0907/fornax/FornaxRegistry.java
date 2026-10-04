package io.github.capsicum0907.fornax;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FornaxRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Fornax.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Fornax.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Fornax.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Fornax.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Fornax.MODID);

    private static final int LIT_LIGHT = 13;
    private static final float HARDNESS = 3.5F;

    private static final Map<Tier, DeferredBlock<FurnaceBlock>> FURNACES = new EnumMap<>(Tier.class);
    private static final List<DeferredItem<BlockItem>> ITEM_ORDER = new ArrayList<>();

    static {
        for (Tier tier : Tier.values()) {
            DeferredBlock<FurnaceBlock> block = BLOCKS.registerBlock(id(tier),
                    properties -> new FurnaceBlock(tier, properties),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(HARDNESS)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)
                            .lightLevel(state -> state.getValue(FurnaceBlock.LIT) ? LIT_LIGHT : 0));
            FURNACES.put(tier, block);
            ITEM_ORDER.add(ITEMS.registerSimpleBlockItem(block));
        }
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FurnaceBlockEntity>> FURNACE_ENTITY =
            BLOCK_ENTITIES.register("furnace", () -> BlockEntityType.Builder.of(FurnaceBlockEntity::new,
                    FURNACES.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<FurnaceMenu>> FURNACE_MENU =
            MENUS.register("furnace", () -> IMenuTypeExtension.create(FurnaceMenu::new));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            TABS.register(Fornax.MODID, () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Fornax.MODID))
                    .icon(() -> new ItemStack(ITEM_ORDER.getFirst().get()))
                    .displayItems((parameters, output) -> ITEM_ORDER.forEach(item -> output.accept(item.get())))
                    .build());

    private FornaxRegistry() {
    }

    public static String id(Tier tier) {
        return tier.id() + "_furnace";
    }

    public static DeferredBlock<FurnaceBlock> furnace(Tier tier) {
        return FURNACES.get(tier);
    }

    public static List<DeferredItem<BlockItem>> items() {
        return ITEM_ORDER;
    }
}
