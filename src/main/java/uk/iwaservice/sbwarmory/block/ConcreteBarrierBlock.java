package uk.iwaservice.sbwarmory.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Concrete (Jersey/T-wall) barrier. One block, two shapes:
 *  - TOP=false : nothing (or another block) below -> Jersey-profile base
 *  - TOP=true  : same block below -> straight, thinner wall that continues upward
 * AXIS is the horizontal axis the wall runs along (set from the placer's facing).
 */
public class ConcreteBarrierBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty TOP = BooleanProperty.create("top");

    // Shapes for a wall running along Z (x = across). Must match assets/.../models/block/*.json
    private static final VoxelShape BASE_Z = Shapes.or(
            Block.box(1, 0, 0, 15, 4, 16), Block.box(2, 4, 0, 14, 7, 16),
            Block.box(3, 7, 0, 13, 11, 16), Block.box(4, 11, 0, 12, 14, 16),
            Block.box(5, 14, 0, 11, 16, 16));
    private static final VoxelShape TOP_Z = Block.box(5, 0, 0, 11, 16, 16);
    private static final VoxelShape BASE_X = rotate(BASE_Z);
    private static final VoxelShape TOP_X = rotate(TOP_Z);

    private static VoxelShape rotate(VoxelShape z) {
        VoxelShape[] out = {Shapes.empty()};
        z.forAllBoxes((x1, y1, z1, x2, y2, z2) -> out[0] = Shapes.or(out[0], Shapes.box(z1, y1, x1, z2, y2, x2)));
        return out[0];
    }

    public ConcreteBarrierBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.Z).setValue(TOP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(AXIS, TOP);
    }

    private boolean isBarrier(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(this);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction.Axis axis = ctx.getHorizontalDirection().getAxis();
        BlockState below = ctx.getLevel().getBlockState(ctx.getClickedPos().below());
        // keep the same run direction when stacking on another barrier
        if (below.is(this)) axis = below.getValue(AXIS);
        return defaultBlockState().setValue(AXIS, axis).setValue(TOP, below.is(this));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (dir == Direction.DOWN) return state.setValue(TOP, neighbor.is(this));
        return super.updateShape(state, dir, neighbor, level, pos, neighborPos);
    }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter g, BlockPos p, CollisionContext c) {
        boolean z = s.getValue(AXIS) == Direction.Axis.Z;
        return s.getValue(TOP) ? (z ? TOP_Z : TOP_X) : (z ? BASE_Z : BASE_X);
    }

    @Override
    public BlockState rotate(BlockState s, Rotation r) {
        return r == Rotation.CLOCKWISE_90 || r == Rotation.COUNTERCLOCKWISE_90
                ? s.setValue(AXIS, s.getValue(AXIS) == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z) : s;
    }
}
