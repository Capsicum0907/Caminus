package io.github.capsicum0907.fornax.client;

import java.util.List;

import net.minecraft.client.gui.screens.recipebook.SmeltingRecipeBookComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import io.github.capsicum0907.fornax.FurnaceMenu;

public class ElectricRecipeBook extends SmeltingRecipeBookComponent {
    @Override
    public void setupGhostRecipe(RecipeHolder<?> recipe, List<Slot> slots) {
        ItemStack result = recipe.value().getResultItem(minecraft.level.registryAccess());
        ghostRecipe.setRecipe(recipe);
        Slot output = slots.get(FurnaceMenu.RESULT_SLOT);
        ghostRecipe.addIngredient(Ingredient.of(result), output.x, output.y);
        Slot input = slots.get(FurnaceMenu.INGREDIENT_SLOT);
        recipe.value().getIngredients().stream()
                .filter(ingredient -> !ingredient.isEmpty())
                .findFirst()
                .ifPresent(ingredient -> ghostRecipe.addIngredient(ingredient, input.x, input.y));
    }
}
