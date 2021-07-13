package dev.hephaestus.glowcase.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class MailboxBlock extends Block {
	private static final VoxelShape SHAPE_NS = VoxelShapes.cuboid(0.3125, 0.25, 0.0625, 0.6875, 0.75, 0.9375);
	private static final VoxelShape SHAPE_EW = VoxelShapes.cuboid(0.0625, 0.25, 0.3125, 0.9375, 0.75, 0.6875);

	public MailboxBlock() {
		super(FabricBlockSettings.of(Material.METAL).nonOpaque().strength(-1, Integer.MAX_VALUE));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(Properties.HORIZONTAL_FACING);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return switch (state.get(Properties.HORIZONTAL_FACING)) {
			case NORTH, SOUTH -> SHAPE_NS;
			case EAST, WEST -> SHAPE_EW;
			default -> VoxelShapes.fullCube();
		};
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return this.getOutlineShape(state, world, pos, context);
	}
}
