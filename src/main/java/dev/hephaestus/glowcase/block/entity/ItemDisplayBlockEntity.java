package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ItemDisplayBlockEntity extends BlockEntity {
	private ItemStack stack = ItemStack.EMPTY;
	private Entity displayEntity = null;

	public RotationType rotationType = RotationType.TRACKING;
	public GivesItem givesItem = GivesItem.YES;
	public boolean showName = true;
	public float pitch;
	public float yaw;
	public Set<UUID> givenTo = new HashSet<>();

	public ItemDisplayBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.ITEM_DISPLAY_BLOCK_ENTITY, pos, state);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = super.toInitialChunkDataNbt();
		writeNbt(tag);
		return tag;
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		tag.put("item", this.stack.writeNbt(new NbtCompound()));
		tag.putString("rotation_type", this.rotationType.name());
		tag.putFloat("pitch", this.pitch);
		tag.putFloat("yaw", this.yaw);
		tag.putBoolean("show_name", this.showName);
		tag.putString("gives_item", this.givesItem.name());
		NbtList given = new NbtList();
		for (UUID id : givenTo) {
			NbtCompound givenTag = new NbtCompound();
			givenTag.putUuid("id", id);
			given.add(givenTag);
		}
		tag.put("given_to", given);
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);

		this.stack = ItemStack.fromNbt(tag.getCompound("item"));

		if (tag.contains("tracking")) {
			this.rotationType = tag.getBoolean("tracking") ? RotationType.TRACKING : RotationType.LOCKED;
		} else if (tag.contains("rotation_type")) {
			this.rotationType = RotationType.valueOf(tag.getString("rotation_type"));
		} else {
			this.rotationType = RotationType.TRACKING;
		}

		if (tag.contains("gives_item")) {
			this.givesItem = GivesItem.valueOf(tag.getString("gives_item"));
		} else {
			this.givesItem = GivesItem.YES;
		}

		if (tag.contains("pitch")) {
			this.pitch = tag.getFloat("pitch");
			this.yaw = tag.getFloat("yaw");
		}

		if (tag.contains("show_name")) {
			this.showName = tag.getBoolean("show_name");
		}

		givenTo.clear();
		if (tag.contains("given_to")) {
			NbtList given = tag.getList("given_to", NbtElement.COMPOUND_TYPE);
			for (NbtElement elem : given) {
				NbtCompound comp = ((NbtCompound) elem);
				givenTo.add(comp.getUuid("id"));
			}
		}
	}

	@Override
	public void markDirty() {
		PlayerLookup.tracking(this).forEach(player -> player.networkHandler.sendPacket(toUpdatePacket()));
		super.markDirty();
	}


	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	public boolean hasItem() {
		return this.stack != null && !this.stack.isEmpty();
	}

	public void setStack(ItemStack stack) {
		this.stack = stack;

		this.setDisplayEntity();

		this.markDirty();
	}

	private void setDisplayEntity() {
		if (this.stack.getItem() instanceof SpawnEggItem) {
			this.displayEntity = ((SpawnEggItem) this.stack.getItem()).getEntityType(this.stack.getNbt()).create(this.world);
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

	public void cycleRotationType(PlayerEntity playerEntity) {
		switch (this.rotationType) {
			case TRACKING -> {
				this.rotationType = RotationType.HORIZONTAL;
				if (this.world != null) {
					this.world.setBlockState(this.pos, this.getCachedState().with(Properties.ROTATION, MathHelper.floor((double) ((playerEntity.getYaw()) * 16.0F / 360.0F) + 0.5D) & 15));
				}
			}
			case HORIZONTAL -> this.rotationType = RotationType.LOCKED;
			case LOCKED -> this.rotationType = RotationType.TRACKING;
		}
	}

	public void cycleGiveType() {
		switch (this.givesItem) {
			case YES -> this.givesItem = GivesItem.NO;
			case NO -> this.givesItem = GivesItem.ONCE;
			case ONCE -> this.givesItem = GivesItem.YES;
		}
	}

	@Environment(EnvType.CLIENT)
	public static Vec2f getPitchAndYaw(PlayerEntity player, BlockPos pos) {
		double d = pos.getX() - player.getPos().x + 0.5;
		double e = pos.getY() - player.getEyeY() + 0.5;
		double f = pos.getZ() - player.getPos().z + 0.5;
		double g = MathHelper.sqrt((float) (d * d + f * f));

		float pitch = (float) ((-MathHelper.atan2(e, g)));
		float yaw = (float) (-MathHelper.atan2(f, d) + Math.PI / 2);

		return new Vec2f(pitch, yaw);
	}

	public static void tick(World world, BlockPos blockPos, BlockState state, ItemDisplayBlockEntity blockEntity) {
		if (blockEntity.displayEntity != null) {
			blockEntity.displayEntity.tick();
			++blockEntity.displayEntity.age;
		}
	}

	public enum RotationType {
		LOCKED, TRACKING, HORIZONTAL
	}

	public enum GivesItem {
		YES, NO, ONCE
	}
}
