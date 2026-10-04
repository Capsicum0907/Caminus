package io.github.capsicum0907.fornax.client;

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
    private static final int NARROW_BELOW = 379;

    private final SmeltingRecipeBookComponent recipeBook = new SmeltingRecipeBookComponent();
    private boolean widthTooNarrow;
    private boolean book;

    public FurnaceScreen(FurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
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
        float progress;
        if (layout.vanilla()) {
            graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
            progress = menu.progress(0);
        } else {
            panel(graphics);
            for (Slot slot : menu.slots) {
                graphics.blit(TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1, SLOT_FRAME_U, SLOT_FRAME_V,
                        Layout.SLOT, Layout.SLOT);
            }
            graphics.blit(TEXTURE, leftPos + layout.flameX(), topPos + layout.flameY(), EMPTY_FLAME_U,
                    EMPTY_FLAME_V, Layout.FLAME, Layout.FLAME);
            graphics.blit(TEXTURE, leftPos + layout.arrowX(), topPos + layout.arrowY(), EMPTY_ARROW_U,
                    EMPTY_ARROW_V, Layout.ARROW_WIDTH, Layout.ARROW_HEIGHT);
            progress = menu.progress();
        }
        int arrow = Mth.ceil(progress * Layout.ARROW_WIDTH);
        graphics.blitSprite(ARROW, Layout.ARROW_WIDTH, Layout.ARROW_HEIGHT, 0, 0,
                leftPos + layout.arrowX(), topPos + layout.arrowY(), arrow, Layout.ARROW_HEIGHT);
        if (menu.burning()) {
            int flame = Mth.ceil(menu.burned() * (Layout.FLAME - 1)) + 1;
            graphics.blitSprite(FLAME, Layout.FLAME, Layout.FLAME, 0, Layout.FLAME - flame,
                    leftPos + layout.flameX(), topPos + layout.flameY() + Layout.FLAME - flame,
                    Layout.FLAME, flame);
        }
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
