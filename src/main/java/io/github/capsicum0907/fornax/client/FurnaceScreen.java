package io.github.capsicum0907.fornax.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import io.github.capsicum0907.fornax.FurnaceMenu;
import io.github.capsicum0907.fornax.Layout;

public class FurnaceScreen extends AbstractContainerScreen<FurnaceMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final ResourceLocation FLAME =
            ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");
    private static final ResourceLocation ARROW =
            ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");
    private static final float ABOVE_ITEMS = 300.0F;

    public FurnaceScreen(FurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        Layout layout = menu.layout();
        this.imageWidth = Layout.WIDTH;
        this.imageHeight = layout.height();
        this.inventoryLabelY = layout.inventoryLabelY();
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        Layout layout = menu.layout();
        if (layout.vanilla()) {
            graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
            int arrow = Mth.ceil(menu.progress(0) * Layout.ARROW_WIDTH);
            graphics.blitSprite(ARROW, Layout.ARROW_WIDTH, Layout.ARROW_HEIGHT, 0, 0,
                    leftPos + layout.arrowX(), topPos + layout.arrowY(), arrow, Layout.ARROW_HEIGHT);
        } else {
            panel(graphics, leftPos, topPos, imageWidth, imageHeight);
            for (Slot slot : menu.slots) {
                well(graphics, leftPos + slot.x - 1, topPos + slot.y - 1);
            }
        }
        if (menu.burning()) {
            int flame = Mth.ceil(menu.burned() * (Layout.FLAME - 1)) + 1;
            graphics.blitSprite(FLAME, Layout.FLAME, Layout.FLAME, 0, Layout.FLAME - flame,
                    leftPos + layout.flameX(), topPos + layout.flameY() + Layout.FLAME - flame,
                    Layout.FLAME, flame);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        Layout layout = menu.layout();
        if (layout.vanilla()) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, ABOVE_ITEMS);
        for (int line = 0; line < menu.lines(); line++) {
            int x = layout.inputX(line);
            int y = layout.inputY(line) + Layout.SLOT - 2;
            int filled = Math.round((Layout.SLOT - 2) * menu.progress(line));
            graphics.fill(x, y - Layout.BAR, x + Layout.SLOT - 2, y, Palette.PROGRESS_BACK);
            if (filled > 0) {
                graphics.fill(x, y - Layout.BAR, x + filled, y, Palette.PROGRESS);
            }
        }
        graphics.pose().popPose();
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
