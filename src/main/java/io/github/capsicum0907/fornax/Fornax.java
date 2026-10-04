package io.github.capsicum0907.fornax;

import com.mojang.logging.LogUtils;

import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;

import org.slf4j.Logger;

import io.github.capsicum0907.fornax.client.FornaxClient;

@Mod(Fornax.MODID)
public class Fornax {
    public static final String MODID = "fornax";

    private static final Logger LOGGER = LogUtils.getLogger();

    public Fornax(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, FornaxConfig.SPEC);

        FornaxRegistry.BLOCKS.register(modEventBus);
        FornaxRegistry.ITEMS.register(modEventBus);
        FornaxRegistry.BLOCK_ENTITIES.register(modEventBus);
        FornaxRegistry.MENUS.register(modEventBus);
        FornaxRegistry.TABS.register(modEventBus);

        modEventBus.addListener(Fornax::registerCapabilities);

        LOGGER.info("Fornax {} loaded.", modContainer.getModInfo().getVersion());
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FornaxRegistry.FURNACE_ENTITY.get(),
                Fornax::face);
    }

    public static IItemHandler face(FurnaceBlockEntity furnace, Direction side) {
        if (side == Direction.UP || side == null) {
            return furnace.inputView();
        }
        return side == Direction.DOWN ? furnace.outputView() : furnace.fuel();
    }

    @Mod(value = MODID, dist = Dist.CLIENT)
    public static class Client {
        public Client(IEventBus modEventBus, ModContainer modContainer) {
            modEventBus.addListener(FornaxClient::registerScreens);
        }
    }
}
