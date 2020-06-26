package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;

public class ItemDisplayBlockEntity extends BlockEntity implements BlockEntityClientSerializable, Tickable {
	private ItemStack stack = ItemStack.EMPTY;
	private Entity displayEntity = null;
	private boolean tracking = true;

	public ItemDisplayBlockEntity() {
		super(Glowcase.ITEM_DISPLAY_BLOCK_ENTITY);
	}

	@Override
	public void fromTag(BlockState state, CompoundTag tag) {
		super.fromTag(state, tag);

		this.stack = ItemStack.fromTag(tag.getCompound("item"));
		this.tracking = tag.getBoolean("tracking");
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		tag.put("item", this.stack.toTag(new CompoundTag()));
		tag.putBoolean("tracking", this.tracking);

		return super.toTag(tag);
	}

	@Override
	public void fromClientTag(CompoundTag compoundTag) {
		this.fromTag(null, compoundTag);
		this.setDisplayEntity();
	}

	@Override
	public CompoundTag toClientTag(CompoundTag compoundTag) {
		return this.toTag(compoundTag);
	}

	public boolean hasItem() {
		return this.stack != null && !this.stack.isEmpty();
	}

	public void setStack(ItemStack stack) {
		this.stack = stack;

		this.setDisplayEntity();

		this.sync();
	}

	private void setDisplayEntity() {
		if (this.stack.getItem() instanceof SpawnEggItem) {
			this.displayEntity = ((SpawnEggItem) this.stack.getItem()).getEntityType(this.stack.getTag()).create(this.world);
		} else {
			this.displayEntity = null;
		}
	}

	public Entity getDisplayEntity() {
		return this.displayEntity;
	}

	public ItemStack getUseStack() {
		return this.stack;
	}

	public boolean isTracking() {
		return this.tracking;
	}

	@Override
	public void tick() {
		if (this.displayEntity != null) {
			this.displayEntity.tick();
			++displayEntity.age;
		}
	}

	public static void save(PacketContext context, PacketByteBuf packetByteBuf) {
		BlockPos pos = packetByteBuf.readBlockPos();
		boolean tracking = packetByteBuf.readBoolean();

		context.getTaskQueue().execute(() -> {
			BlockEntity blockEntity = context.getPlayer().getEntityWorld().getBlockEntity(pos);
			if (blockEntity instanceof ItemDisplayBlockEntity) {
				((ItemDisplayBlockEntity) blockEntity).tracking = tracking;
				((ItemDisplayBlockEntity) blockEntity).sync();
			}
		});
	}

	public void toggleTracking() {
		this.tracking = !this.tracking;
		this.sync();
	}
}
