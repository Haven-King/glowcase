package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import java.util.regex.Pattern;

public class HyperlinkBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
	public String url = "";

	public HyperlinkBlockEntity(BlockPos pos, BlockState state) {
		super(Glowcase.HYPERLINK_BLOCK_ENTITY, pos, state);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		super.writeNbt(tag);

		tag.putString("url", this.url);

		return tag;
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);

		this.url = tag.getString("url");
	}

	@Override
	public void fromClientTag(NbtCompound NbtCompound) {
		this.readNbt(NbtCompound);
	}

	@Override
	public NbtCompound toClientTag(NbtCompound NbtCompound) {
		return this.writeNbt(NbtCompound);
	}
}
