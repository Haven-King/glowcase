package dev.hephaestus.glowcase.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import java.util.regex.Pattern;

public class HyperlinkBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
	private static final Pattern URL = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	public String url = "";

	public HyperlinkBlockEntity() {
		super(Glowcase.HYPERLINK_BLOCK_ENTITY);
	}

	public static void save(PacketContext context, PacketByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		String url = buf.readString(32767);

		context.getTaskQueue().execute(() -> {
			BlockEntity blockEntity = context.getPlayer().getEntityWorld().getBlockEntity(pos);

			if (blockEntity instanceof HyperlinkBlockEntity && URL.matcher(url).matches()) {
				((HyperlinkBlockEntity) blockEntity).url = url;
				((HyperlinkBlockEntity) blockEntity).sync();
			}
		});
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		super.toTag(tag);

		tag.putString("url", this.url);

		return tag;
	}

	@Override
	public void fromTag(BlockState state, CompoundTag tag) {
		super.fromTag(state, tag);

		this.url = tag.getString("url");
	}

	@Override
	public void fromClientTag(CompoundTag compoundTag) {
		this.fromTag(Glowcase.HYPERLINK_BLOCK.getDefaultState(), compoundTag);
	}

	@Override
	public CompoundTag toClientTag(CompoundTag compoundTag) {
		return this.toTag(compoundTag);
	}
}
