package io.github.capsicum0907.fornax.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import io.github.capsicum0907.fornax.FurnaceMenu;
import io.github.capsicum0907.fornax.Layout;

public class FurnaceScreen extends AbstractContainerScreen<FurnaceMenu> {
    private static final ResourceLocation FLAME =
            ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");

    public FurnaceScreen(FurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        Layout layout = menu.layout();
        this.imageWidth = Layout.WIDTH;
        this.imageHeight = layout.height();
        this.inventoryLabelY = layout.inventoryLabelY();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        panel(graphics, leftPos, topPos, imageWidth, imageHeight);
        for (Slot slot : menu.slots) {
            well(graphics, leftPos + slot.x - 1, topPos + slot.y - 1);
        }
        Layout layout = menu.layout();
        for (int line = 0; line < menu.lines(); line++) {
            int x = leftPos + layout.inputX(line);
            int y = topPos + layout.inputY(line) + Layout.SLOT - 2;
            int filled = Math.round((Layout.SLOT - 2) * menu.progress(line));
            graphics.fill(x, y - Layout.BAR, x + Layout.SLOT - 2, y, Palette.PROGRESS_BACK);
            if (filled > 0) {
                graphics.fill(x, y - Layout.BAR, x + filled, y, Palette.PROGRESS);
            }
        }
        if (menu.burning()) {
            int flame = Math.max(1, Math.round(Layout.FLAME * menu.burned()));
            graphics.blitSprite(FLAME, Layout.FLAME, Layout.FLAME, 0, Layout.FLAME - flame,
                    leftPos + layout.flameX(), topPos + layout.flameY() + Layout.FLAME - flame,
                    Layout.FLAME, flame);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, Palette.OUTLINE);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, Palette.PANEL_LIGHT);
        graphics.fill(x + 3, y + 3, x + width - 1, y + height - 1, Palette.PANEL_DARK);
        graphics.fill(x + 3, y + 3, x + width - 3, y + height - 3, Palette.PANEL);
    }

    private static void well(GuiGraphics graphics, int x, int y) {
        int size = Layout.SLOT;
        graphics.fill(x, y, x + size, y + size, Palette.WELL_LIGHT);
        graphics.fill(x, y, x + size - 1, y + size - 1, Palette.WELL_SHADOW);
        graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, Palette.WELL);
    }
}
