package io.github.capsicum0907.caminus.client;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import io.github.capsicum0907.caminus.CaminusRegistry;

public final class CaminusClient {
    private CaminusClient() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CaminusRegistry.FURNACE_MENU.get(), FurnaceScreen::new);
    }
}
