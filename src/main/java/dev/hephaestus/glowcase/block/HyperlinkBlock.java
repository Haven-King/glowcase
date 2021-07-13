package dev.hephaestus.glowcase.block;

import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.item.GlowcaseItem;
import dev.hephaestus.glowcase.networking.HyperlinkChannel;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HyperlinkBlock extends GlowcaseBlock implements BlockEntityProvider {
	private static final VoxelShape OUTLINE = VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return OUTLINE;
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		if (!world.isClient) {
			if (placer instanceof ServerPlayerEntity player && player.isCreative() && world.canPlayerModifyAt(player, pos)) {
				HyperlinkChannel.openScreen(player, pos);
			}
		}
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		} else if (player.getStackInHand(hand).getItem() instanceof GlowcaseItem) {
			this.onPlaced(world, pos, state, player, null);
			return ActionResult.SUCCESS;
		} else {
			if (world.getBlockEntity(pos) instanceof HyperlinkBlockEntity be) {
				String url = be.url;

				HyperlinkChannel.confirm((ServerPlayerEntity) player, url);

				return ActionResult.SUCCESS;
			} else {
				return ActionResult.CONSUME;
			}
		}
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new HyperlinkBlockEntity(pos, state);
	}
}
