package io.github.capsicum0907.fornax;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;

public class FurnaceBlockEntity extends BlockEntity implements MenuProvider {
    private static final String INPUTS = "Inputs";
    private static final String OUTPUTS = "Outputs";
    private static final String PROGRESS = "Progress";
    private static final String USED = "RecipesUsed";
    private static final int COOLING = 2;

    private final Rung rung;
    private final Hold inputs;
    private final Hold outputs;
    private final Power power;
    private final int[] progress;
    private final int[] total;
    private final long[] planned;
    private final List<RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe>> checks = new ArrayList<>();
    private final Object2LongOpenHashMap<ResourceLocation> used = new Object2LongOpenHashMap<>();
    private final FurnaceData data;
    private final InputView inputView;
    private final OutputView outputView;
    private boolean working;

    public FurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(FornaxRegistry.FURNACE_ENTITY.get(), pos, state);
        this.rung = ((FurnaceBlock) state.getBlock()).rung();
        int lines = rung.lines();
        this.inputs = new Hold(lines, this::batch, this::setChanged);
        this.outputs = new Hold(lines, this::batch, this::setChanged);
        this.power = rung.kind() == Kind.ELECTRIC ? new ElectricPower(rung, this::setChanged)
                : new FuelPower(this::setChanged);
        this.progress = new int[lines];
        this.total = new int[lines];
        this.planned = new long[lines];
        for (int line = 0; line < lines; line++) {
            checks.add(RecipeManager.createCheck(RecipeType.SMELTING));
        }
        this.data = new FurnaceData(lines, this::gauge, power::gaugeFull, () -> working ? 1 : 0, this::progressOf);
        this.inputView = new InputView(inputs);
        this.outputView = new OutputView(outputs, power.fuel());
    }

    public Rung rung() {
        return rung;
    }

    public int batch() {
        return rung.batch();
    }

    public Power power() {
        return power;
    }

    public Hold inputs() {
        return inputs;
    }

    public Hold outputs() {
        return outputs;
    }

    public ItemStackHandler fuel() {
        return power.fuel();
    }

    public IEnergyStorage energy() {
        return power.energy();
    }

    public InputView inputView() {
        return inputView;
    }

    public OutputView outputView() {
        return outputView;
    }

    public int heat() {
        return power instanceof FuelPower fuel ? fuel.heat() : 0;
    }

    public long used(ResourceLocation recipe) {
        return used.getLong(recipe);
    }

    public static boolean burns(ItemStack stack) {
        return FuelPower.burns(stack);
    }

    public static int cookTicks(Rung rung, int recipeTicks) {
        return (int) Math.max(1, Math.round(recipeTicks * (double) rung.ticks() / FornaxConfig.STANDARD));
    }

    private int progressOf(int line) {
        return total[line] <= 0 ? 0 : FurnaceData.scaled(progress[line], total[line]);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FurnaceBlockEntity furnace) {
        boolean working = false;
        for (int line = 0; line < furnace.rung.lines(); line++) {
            working |= furnace.work(level, line);
        }
        if (!working) {
            furnace.power.idle();
        }
        boolean lit = working || furnace.power.burning();
        furnace.working = lit;
        if (state.getValue(FurnaceBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(FurnaceBlock.LIT, lit), 3);
        }
    }

    private boolean work(Level level, int line) {
        ItemStack input = inputs.getStackInSlot(line);
        if (input.isEmpty()) {
            idle(line);
            return false;
        }
        Optional<RecipeHolder<SmeltingRecipe>> found =
                checks.get(line).getRecipeFor(new SingleRecipeInput(input), level);
        if (found.isEmpty()) {
            idle(line);
            return false;
        }
        RecipeHolder<SmeltingRecipe> recipe = found.get();
        ItemStack result = recipe.value().assemble(new SingleRecipeInput(input), level.registryAccess());
        if (result.isEmpty() || outputs.room(line, result) < result.getCount()) {
            return false;
        }
        int cost = recipe.value().getCookingTime();
        total[line] = cookTicks(rung, cost);
        if (!power.ready(cost)) {
            progress[line] = Math.max(0, progress[line] - COOLING);
            return false;
        }
        planned[line] = (long) Math.min(batch(), input.getCount()) * cost;
        if (progress[line] < total[line]) {
            progress[line]++;
        }
        if (progress[line] >= total[line]) {
            finish(line, recipe, input, result, cost);
        }
        return true;
    }

    private void idle(int line) {
        progress[line] = 0;
        total[line] = 0;
        planned[line] = 0;
    }

    private int gauge() {
        double spent = 0;
        for (int line = 0; line < progress.length; line++) {
            if (total[line] > 0) {
                spent += planned[line] * (double) progress[line] / total[line];
            }
        }
        return power.gauge(spent);
    }

    private void finish(int line, RecipeHolder<SmeltingRecipe> recipe, ItemStack input, ItemStack result, int cost) {
        int fits = outputs.room(line, result) / result.getCount();
        int wanted = Math.min(Math.min(batch(), input.getCount()), fits);
        int count = power.afford(wanted, cost);
        if (count <= 0) {
            return;
        }
        power.pay(count, cost);
        inputs.put(line, input.copyWithCount(input.getCount() - count));
        ItemStack held = outputs.getStackInSlot(line);
        int made = count * result.getCount();
        outputs.put(line, held.isEmpty() ? result.copyWithCount(made) : held.copyWithCount(held.getCount() + made));
        used.addTo(recipe.id(), count);
        progress[line] = 0;
        setChanged();
    }

    public void awardExperience(ServerLevel level, Vec3 at, ServerPlayer player) {
        List<RecipeHolder<?>> recipes = new ArrayList<>();
        for (Object2LongMap.Entry<ResourceLocation> entry : used.object2LongEntrySet()) {
            level.getRecipeManager().byKey(entry.getKey()).ifPresent(recipe -> {
                recipes.add(recipe);
                if (recipe.value() instanceof AbstractCookingRecipe cooking) {
                    pop(level, at, entry.getLongValue(), cooking.getExperience());
                }
            });
        }
        if (player != null) {
            player.awardRecipes(recipes);
        }
        used.clear();
        setChanged();
    }

    private static void pop(ServerLevel level, Vec3 at, long count, float experience) {
        double points = count * (double) experience;
        long whole = (long) Math.floor(points);
        if (level.random.nextDouble() < points - whole) {
            whole++;
        }
        ExperienceOrb.award(level, at, (int) Math.min(whole, Integer.MAX_VALUE));
    }

    public NonNullList<ItemStack> spillable() {
        NonNullList<ItemStack> all = NonNullList.create();
        for (Hold hold : new Hold[] { inputs, outputs }) {
            for (int slot = 0; slot < hold.size(); slot++) {
                ItemStack stack = hold.getStackInSlot(slot);
                int left = stack.getCount();
                while (left > 0) {
                    int part = Math.min(left, stack.getMaxStackSize());
                    all.add(stack.copyWithCount(part));
                    left -= part;
                }
            }
        }
        power.spill(all);
        return all;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(INPUTS, inputs.serializeNBT(registries));
        tag.put(OUTPUTS, outputs.serializeNBT(registries));
        power.save(tag, registries);
        tag.putIntArray(PROGRESS, progress);
        CompoundTag recipes = new CompoundTag();
        used.forEach((id, count) -> recipes.putLong(id.toString(), count));
        tag.put(USED, recipes);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inputs.deserializeNBT(registries, tag.getCompound(INPUTS));
        outputs.deserializeNBT(registries, tag.getCompound(OUTPUTS));
        power.load(tag, registries);
        int[] saved = tag.getIntArray(PROGRESS);
        System.arraycopy(saved, 0, progress, 0, Math.min(saved.length, progress.length));
        used.clear();
        CompoundTag recipes = tag.getCompound(USED);
        for (String key : recipes.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id != null) {
                used.put(id, recipes.getLong(key));
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FurnaceMenu(id, inventory, this, data);
    }

    public ContainerData data() {
        return data;
    }

    public static float burned(int heat, int burnLength) {
        return burnLength <= 0 ? 0.0F : Mth.clamp(heat / (float) burnLength, 0.0F, 1.0F);
    }
}
