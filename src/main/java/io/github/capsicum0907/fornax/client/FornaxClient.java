package io.github.capsicum0907.fornax.client;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import io.github.capsicum0907.fornax.FornaxRegistry;

public final class FornaxClient {
    private FornaxClient() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(FornaxRegistry.FURNACE_MENU.get(), FurnaceScreen::new);
    }
}
