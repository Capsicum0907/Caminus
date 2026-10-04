package io.github.capsicum0907.fornax.client;

import com.mojang.math.Axis;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.gui.screens.recipebook.SmeltingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

import io.github.capsicum0907.fornax.FurnaceMenu;
import io.github.capsicum0907.fornax.Layout;

public class FurnaceScreen extends AbstractContainerScreen<FurnaceMenu> implements RecipeUpdateListener {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final ResourceLocation FLAME =
            ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");
    private static final ResourceLocation ARROW =
            ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");
    private static final int CORNER = 4;
    private static final int RIGHT_EDGE_U = 172;
    private static final int BOTTOM_EDGE_V = 162;
    private static final int FILL_U = 8;
    private static final int FILL_V = 10;
    private static final int SLOT_FRAME_U = 55;
    private static final int SLOT_FRAME_V = 16;
    private static final int EMPTY_FLAME_U = 56;
    private static final int EMPTY_FLAME_V = 36;
    private static final int EMPTY_ARROW_U = 79;
    private static final int EMPTY_ARROW_V = 34;
    private static final int TEXTURE_SIZE = 256;
    private static final float QUARTER_TURN = 90.0F;
    private static final int COUNT_ROOM = 14;
    private static final int COUNT_CORNER = 17;
    private static final float ABOVE_ITEM = 200.0F;
    private static final int COUNT_COLOUR = 0xFFFFFF;
    private static final int RESULT_FRAME_U = 111;
    private static final int RESULT_FRAME_V = 30;
    private static final int RESULT_FRAME = 26;
    private static final int RESULT_FRAME_INSET = 5;
    private static final int FRAME_EDGE = 1;
    private static final int CELL_HEIGHT = 3;
    private static final int CELL_INSET = 1;
    private static final int CELL_ON = 0xFFFF5A3C;
    private static final int CELL_ON_DARK = 0xFFBE3226;
    private static final int CELL_OFF = 0xFF3C1612;
    private static final int CELL_GAP = 0xFF222222;
    private static final int NARROW_BELOW = 379;

    private final SmeltingRecipeBookComponent recipeBook;
    private boolean widthTooNarrow;
    private boolean book;

    public FurnaceScreen(FurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.recipeBook = menu.electric() ? new ElectricRecipeBook() : new SmeltingRecipeBookComponent();
        Layout layout = menu.layout();
        this.imageWidth = layout.width();
        this.imageHeight = layout.height();
        this.inventoryLabelX = layout.inventoryX();
        this.inventoryLabelY = layout.inventoryLabelY();
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        book = !FornaxClientConfig.hideRecipeBook();
        if (!book) {
            return;
        }
        widthTooNarrow = width < NARROW_BELOW;
        recipeBook.init(width, height, minecraft, widthTooNarrow, menu);
        leftPos = recipeBook.updateScreenPosition(width, imageWidth);
        Layout layout = menu.layout();
        int bookY = layout.bookY(height, topPos);
        addRenderableWidget(new ImageButton(leftPos + layout.bookX(), bookY,
                Layout.BOOK_WIDTH, Layout.BOOK_HEIGHT, RecipeBookComponent.RECIPE_BUTTON_SPRITES, button -> {
                    recipeBook.toggleVisibility();
                    leftPos = recipeBook.updateScreenPosition(width, imageWidth);
                    button.setPosition(leftPos + layout.bookX(), bookY);
                }));
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (book) {
            recipeBook.tick();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!book) {
            super.render(graphics, mouseX, mouseY, partialTick);
            renderTooltip(graphics, mouseX, mouseY);
            return;
        }
        if (recipeBook.isVisible() && widthTooNarrow) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
            recipeBook.render(graphics, mouseX, mouseY, partialTick);
        } else {
            super.render(graphics, mouseX, mouseY, partialTick);
            recipeBook.render(graphics, mouseX, mouseY, partialTick);
            recipeBook.renderGhostRecipe(graphics, leftPos, topPos, true, partialTick);
        }
        renderTooltip(graphics, mouseX, mouseY);
        recipeBook.renderTooltip(graphics, leftPos, topPos, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        Layout layout = menu.layout();
        if (layout.vanilla() && !layout.electric()) {
            graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        } else {
            panel(graphics);
            for (Slot slot : menu.slots) {
                if (!slot.isActive()) {
                    continue;
                }
                if (layout.vanilla() && slot.index == FurnaceMenu.RESULT_SLOT) {
                    graphics.blit(TEXTURE, leftPos + slot.x - RESULT_FRAME_INSET, topPos + slot.y - RESULT_FRAME_INSET,
                            RESULT_FRAME_U, RESULT_FRAME_V, RESULT_FRAME, RESULT_FRAME);
                } else {
                    graphics.blit(TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1, SLOT_FRAME_U, SLOT_FRAME_V,
                            Layout.SLOT, Layout.SLOT);
                }
            }
            if (!layout.electric()) {
                graphics.blit(TEXTURE, leftPos + layout.flameX(), topPos + layout.flameY(), EMPTY_FLAME_U,
                        EMPTY_FLAME_V, Layout.FLAME, Layout.FLAME);
            }
            if (layout.vanilla()) {
                graphics.blit(TEXTURE, leftPos + layout.arrowX(), topPos + layout.arrowY(), EMPTY_ARROW_U,
                        EMPTY_ARROW_V, Layout.ARROW_WIDTH, Layout.ARROW_HEIGHT);
            }
        }
        arrow(graphics, layout, layout.vanilla() ? menu.progress(0) : menu.progress());
        if (layout.electric()) {
            bar(graphics, layout);
        } else if (menu.burning()) {
            int flame = Mth.ceil(menu.burned() * (Layout.FLAME - 1)) + 1;
            graphics.blitSprite(FLAME, Layout.FLAME, Layout.FLAME, 0, Layout.FLAME - flame,
                    leftPos + layout.flameX(), topPos + layout.flameY() + Layout.FLAME - flame,
                    Layout.FLAME, flame);
        }
    }

    private void bar(GuiGraphics graphics, Layout layout) {
        int x = leftPos + layout.barX();
        int y = topPos + layout.barY();
        int width = layout.barWidth();
        int height = layout.barHeight();
        int inner = Layout.SLOT - FRAME_EDGE * 2;
        graphics.blit(TEXTURE, x, y, SLOT_FRAME_U, SLOT_FRAME_V, width, FRAME_EDGE);
        graphics.blit(TEXTURE, x, y + height - FRAME_EDGE, SLOT_FRAME_U, SLOT_FRAME_V + Layout.SLOT - FRAME_EDGE,
                width, FRAME_EDGE);
        stretch(graphics, x, y + FRAME_EDGE, width, height - FRAME_EDGE * 2, SLOT_FRAME_U, SLOT_FRAME_V + FRAME_EDGE,
                Layout.SLOT, inner);
        int left = x + FRAME_EDGE;
        int top = y + FRAME_EDGE;
        int bottom = y + height - FRAME_EDGE;
        graphics.fill(left, top, left + inner, bottom, CELL_GAP);
        int lit = Math.round((bottom - top) * menu.charged());
        for (int row = 0; row < bottom - top; row++) {
            int step = row % CELL_HEIGHT;
            if (step == CELL_HEIGHT - 1) {
                continue;
            }
            int colour = row >= lit ? CELL_OFF : step == CELL_HEIGHT - 2 ? CELL_ON : CELL_ON_DARK;
            int at = bottom - 1 - row;
            graphics.fill(left + CELL_INSET, at, left + inner - CELL_INSET, at + 1, colour);
        }
    }

    private boolean overBar(int mouseX, int mouseY) {
        Layout layout = menu.layout();
        int x = leftPos + layout.barX();
        int y = topPos + layout.barY();
        return layout.electric() && mouseX >= x && mouseX < x + layout.barWidth()
                && mouseY >= y && mouseY < y + layout.barHeight();
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && overBar(mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("gui.fornax.energy",
                    String.format("%,d", menu.energy()), String.format("%,d", menu.capacity())), mouseX, mouseY);
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    private void arrow(GuiGraphics graphics, Layout layout, float progress) {
        int filled = Mth.ceil(progress * Layout.ARROW_WIDTH);
        graphics.pose().pushPose();
        if (layout.arrowDown()) {
            graphics.pose().translate(leftPos + layout.arrowX() + Layout.ARROW_HEIGHT, topPos + layout.arrowY(), 0.0F);
            graphics.pose().mulPose(Axis.ZP.rotationDegrees(QUARTER_TURN));
            graphics.blit(TEXTURE, 0, 0, EMPTY_ARROW_U, EMPTY_ARROW_V, Layout.ARROW_WIDTH, Layout.ARROW_HEIGHT);
        } else {
            graphics.pose().translate(leftPos + layout.arrowX(), topPos + layout.arrowY(), 0.0F);
        }
        graphics.blitSprite(ARROW, Layout.ARROW_WIDTH, Layout.ARROW_HEIGHT, 0, 0, 0, 0, filled, Layout.ARROW_HEIGHT);
        graphics.pose().popPose();
    }

    private void panel(GuiGraphics graphics) {
        int x = leftPos;
        int y = topPos;
        int innerWidth = imageWidth - CORNER * 2;
        int innerHeight = imageHeight - CORNER * 2;
        int right = x + imageWidth - CORNER;
        int bottom = y + imageHeight - CORNER;
        graphics.blit(TEXTURE, x, y, 0, 0, CORNER, CORNER);
        graphics.blit(TEXTURE, right, y, RIGHT_EDGE_U, 0, CORNER, CORNER);
        graphics.blit(TEXTURE, x, bottom, 0, BOTTOM_EDGE_V, CORNER, CORNER);
        graphics.blit(TEXTURE, right, bottom, RIGHT_EDGE_U, BOTTOM_EDGE_V, CORNER, CORNER);
        stretch(graphics, x + CORNER, y, innerWidth, CORNER, FILL_U, 0, 1, CORNER);
        stretch(graphics, x + CORNER, bottom, innerWidth, CORNER, FILL_U, BOTTOM_EDGE_V, 1, CORNER);
        stretch(graphics, x, y + CORNER, CORNER, innerHeight, 0, FILL_V, CORNER, 1);
        stretch(graphics, right, y + CORNER, CORNER, innerHeight, RIGHT_EDGE_U, FILL_V, CORNER, 1);
        stretch(graphics, x + CORNER, y + CORNER, innerWidth, innerHeight, FILL_U, FILL_V, 1, 1);
    }

    private static void stretch(GuiGraphics graphics, int x, int y, int width, int height, int u, int v,
            int uWidth, int vHeight) {
        graphics.blit(TEXTURE, x, y, width, height, u, v, uWidth, vHeight, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    protected void renderSlotContents(GuiGraphics graphics, ItemStack stack, Slot slot, String countString) {
        String count = String.valueOf(stack.getCount());
        int width = font.width(count);
        if (countString != null || !(slot instanceof SlotItemHandler) || stack.getCount() == 1 || width <= COUNT_ROOM) {
            super.renderSlotContents(graphics, stack, slot, countString);
            return;
        }
        super.renderSlotContents(graphics, stack, slot, "");
        float scale = COUNT_ROOM / (float) width;
        graphics.pose().pushPose();
        graphics.pose().translate(slot.x + COUNT_CORNER, slot.y + COUNT_CORNER, ABOVE_ITEM);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, count, -width, -(font.lineHeight - 1), COUNT_COLOUR, true);
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!book) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (recipeBook.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return widthTooNarrow && recipeBook.isVisible() || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slot, slotId, mouseButton, type);
        if (book) {
            recipeBook.slotClicked(slot);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return book && recipeBook.keyPressed(keyCode, scanCode, modifiers)
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        if (!book) {
            return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton);
        }
        boolean outside = mouseX < guiLeft || mouseY < guiTop
                || mouseX >= guiLeft + imageWidth || mouseY >= guiTop + imageHeight;
        return recipeBook.hasClickedOutside(mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight, mouseButton)
                && outside;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return book && recipeBook.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    @Override
    public void recipesUpdated() {
        if (book) {
            recipeBook.recipesUpdated();
        }
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return recipeBook;
    }
}
