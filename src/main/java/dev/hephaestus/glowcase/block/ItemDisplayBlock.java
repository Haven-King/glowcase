package dev.hephaestus.glowcase.block;

import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.item.GlowcaseItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ItemDisplayBlock extends GlowcaseBlock implements BlockEntityProvider {
	private static final VoxelShape OUTLINE = VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);

	public ItemDisplayBlock() {
		super();
		this.setDefaultState(this.getDefaultState().with(Properties.ROTATION, 0));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(Properties.ROTATION);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(Properties.ROTATION, MathHelper.floor((double)((ctx.getPlayerYaw()) * 16.0F / 360.0F) + 0.5D) & 15);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return OUTLINE;
	}

	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return new ItemDisplayBlockEntity();
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			ItemStack handStack = player.getStackInHand(hand);
			if (blockEntity instanceof ItemDisplayBlockEntity) {
				if (((ItemDisplayBlockEntity) blockEntity).hasItem() && handStack.isEmpty()) {
					player.setStackInHand(hand, ((ItemDisplayBlockEntity) blockEntity).getUseStack().copy());
					return ActionResult.SUCCESS;
				} else if (!((ItemDisplayBlockEntity) blockEntity).hasItem() && !handStack.isEmpty()) {
					((ItemDisplayBlockEntity) blockEntity).setStack(handStack.copy());
					return ActionResult.SUCCESS;
				} else if (((ItemDisplayBlockEntity) blockEntity).hasItem() && handStack.getItem() instanceof GlowcaseItem) {
					((ItemDisplayBlockEntity) blockEntity).setStack(ItemStack.EMPTY);
					return ActionResult.SUCCESS;
				} else if (((ItemDisplayBlockEntity) blockEntity).hasItem() && ((ItemDisplayBlockEntity) blockEntity).getUseStack().isItemEqualIgnoreDamage(handStack)) {
					((ItemDisplayBlockEntity) blockEntity).toggleTracking();
					return ActionResult.SUCCESS;
				}
			}

			return ActionResult.PASS;
		}

		return ActionResult.SUCCESS;
	}
}
