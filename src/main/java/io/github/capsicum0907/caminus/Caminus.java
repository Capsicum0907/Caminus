package io.github.capsicum0907.caminus;

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

import io.github.capsicum0907.caminus.client.CaminusClient;
import io.github.capsicum0907.caminus.client.CaminusClientConfig;

@Mod(Caminus.MODID)
public class Caminus {
    public static final String MODID = "caminus";

    private static final Logger LOGGER = LogUtils.getLogger();

    public Caminus(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, CaminusConfig.SPEC);

        CaminusRegistry.BLOCKS.register(modEventBus);
        CaminusRegistry.ITEMS.register(modEventBus);
        CaminusRegistry.BLOCK_ENTITIES.register(modEventBus);
        CaminusRegistry.MENUS.register(modEventBus);
        CaminusRegistry.TABS.register(modEventBus);

        modEventBus.addListener(Caminus::registerCapabilities);

        LOGGER.info("Caminus {} loaded.", modContainer.getModInfo().getVersion());
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CaminusRegistry.FURNACE_ENTITY.get(),
                Caminus::face);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, CaminusRegistry.FURNACE_ENTITY.get(),
                (furnace, side) -> furnace.energy());
    }

    public static IItemHandler face(FurnaceBlockEntity furnace, Direction side) {
        if (side == null) {
            return furnace.wholeView();
        }
        if (side == Direction.DOWN) {
            return furnace.outputView();
        }
        if (side == Direction.UP) {
            return furnace.inputView();
        }
        return furnace.fuel();
    }

    @Mod(value = MODID, dist = Dist.CLIENT)
    public static class Client {
        public Client(IEventBus modEventBus, ModContainer modContainer) {
            modContainer.registerConfig(ModConfig.Type.CLIENT, CaminusClientConfig.SPEC);
            modEventBus.addListener(CaminusClient::registerScreens);
        }
    }
}
