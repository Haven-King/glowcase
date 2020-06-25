package dev.hephaestus.glowcase.block;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.item.GlowcaseItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class HyperlinkBlock extends GlowcaseBlock implements BlockEntityProvider {
	private static final VoxelShape OUTLINE = VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return OUTLINE;
	}

	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return new HyperlinkBlockEntity();
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		if (!world.isClient) {
			if ((placer instanceof ServerPlayerEntity) && ((ServerPlayerEntity) placer).isCreative() && world.canPlayerModifyAt((PlayerEntity) placer, pos)) {
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeBlockPos(pos);

				ServerSidePacketRegistry.INSTANCE.sendToPlayer((PlayerEntity) placer, Glowcase.OPEN_HYPERLINK_SCREEN, buf);
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
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof HyperlinkBlockEntity) {
				String url = ((HyperlinkBlockEntity) blockEntity).url;

				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeString(url);

				ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Glowcase.OPEN_HYPERLINK_CONFIRMATION, buf);

				return ActionResult.SUCCESS;
			} else {
				return ActionResult.CONSUME;
			}
		}
	}

}
