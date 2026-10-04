package io.github.capsicum0907.caminus;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class FurnaceBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public static final MapCodec<FurnaceBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Kind.CODEC.fieldOf("kind").forGetter(block -> block.rung().kind()),
                    Tier.CODEC.optionalFieldOf("tier").forGetter(block -> Optional.ofNullable(block.rung().tier())),
                    propertiesCodec())
                    .apply(instance, (kind, tier, properties) -> new FurnaceBlock(new Rung(kind, tier.orElse(null)), properties)));

    private static final double CRACKLE_CHANCE = 0.1;
    private static final double FACE_OFFSET = 0.52;
    private static final double SPREAD = 0.6;
    private static final double MOUTH_HEIGHT = 6.0 / 16.0;

    private final Rung rung;

    public FurnaceBlock(Rung rung, Properties properties) {
        super(properties);
        this.rung = rung;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, Boolean.FALSE));
    }

    public Rung rung() {
        return rung;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof FurnaceBlockEntity furnace) {
            player.openMenu(furnace, buffer -> {
                buffer.writeVarInt(furnace.rung().kind().ordinal());
                buffer.writeVarInt(furnace.rung().vanilla() ? -1 : furnace.rung().tier().ordinal());
                buffer.writeVarInt(furnace.batch());
            });
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState now, boolean moved) {
        if (!state.is(now.getBlock()) && level.getBlockEntity(pos) instanceof FurnaceBlockEntity furnace) {
            if (level instanceof ServerLevel server) {
                Containers.dropContents(level, pos, furnace.spillable());
                furnace.awardExperience(server, pos.getCenter(), null);
            }
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, now, moved);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (rung.kind() != Kind.FUEL || !state.getValue(LIT)) {
            return;
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;
        if (random.nextDouble() < CRACKLE_CHANCE) {
            level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
        Direction facing = state.getValue(FACING);
        double along = random.nextDouble() * SPREAD - SPREAD / 2;
        double dx = facing.getAxis() == Direction.Axis.X ? facing.getStepX() * FACE_OFFSET : along;
        double dy = random.nextDouble() * MOUTH_HEIGHT;
        double dz = facing.getAxis() == Direction.Axis.Z ? facing.getStepZ() * FACE_OFFSET : along;
        level.addParticle(ParticleTypes.SMOKE, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.FLAME, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FurnaceBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : createTickerHelper(type, CaminusRegistry.FURNACE_ENTITY.get(), FurnaceBlockEntity::serverTick);
    }
}
