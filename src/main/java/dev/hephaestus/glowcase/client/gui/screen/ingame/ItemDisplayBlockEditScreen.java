package dev.hephaestus.glowcase.client.gui.screen.ingame;

import dev.hephaestus.glowcase.GlowcaseNetworking;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.state.property.Properties;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;

@Environment(EnvType.CLIENT)
public class ItemDisplayBlockEditScreen extends GlowcaseScreen {
	private final ItemDisplayBlockEntity displayBlock;

	private ButtonWidget givesItemButtom;
	private ButtonWidget rotationTypeButton;
	private ButtonWidget showNameButton;

	protected ItemDisplayBlockEditScreen(ItemDisplayBlockEntity displayBlock) {
		this.displayBlock = displayBlock;
	}

	@Override
	public void init() {
		super.init();

		if (this.client != null) {
			int padding = width / 100;
			int individualPadding = padding / 2;
			int centerW = width / 2;
			int centerH = height / 2;

			this.givesItemButtom = new ButtonWidget(centerW - 75, centerH - 30 - individualPadding, 150, 20, new TranslatableText("gui.glowcase.gives_item", this.displayBlock.givesItem), (action) -> {
				this.displayBlock.givesItem = !this.displayBlock.givesItem;
				this.givesItemButtom.setMessage(new TranslatableText("gui.glowcase.gives_item", this.displayBlock.givesItem));
				this.sync(true);
			});

			this.rotationTypeButton = new ButtonWidget(centerW - 75, centerH - 10, 150, 20, new TranslatableText("gui.glowcase.rotation_type", this.displayBlock.rotationType), (action) -> {
				this.displayBlock.cycleRotationType(client.player);
				this.rotationTypeButton.setMessage(new TranslatableText("gui.glowcase.rotation_type", this.displayBlock.rotationType));
				this.sync(true);
			});

			this.showNameButton = new ButtonWidget(centerW - 75, centerH + 10 + individualPadding, 150, 20, new TranslatableText("gui.glowcase.show_name", this.displayBlock.showName), (action) -> {
				this.displayBlock.showName = !displayBlock.showName;
				this.showNameButton.setMessage(new TranslatableText("gui.glowcase.show_name", this.displayBlock.showName));
				this.sync(false);
			});

			this.addDrawableChild(this.givesItemButtom);
			this.addDrawableChild(this.rotationTypeButton);
			this.addDrawableChild(this.showNameButton);
		}
	}

	private void sync(boolean updatePitchAndYaw) {
		if (MinecraftClient.getInstance().player != null) {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeBlockPos(this.displayBlock.getPos());
			buf.writeEnumConstant(this.displayBlock.rotationType);
			buf.writeBoolean(this.displayBlock.givesItem);
			buf.writeVarInt(this.displayBlock.getCachedState().get(Properties.ROTATION));
			buf.writeBoolean(this.displayBlock.showName);

			if (updatePitchAndYaw) {
				Vec2f pitchAndYaw = ItemDisplayBlockEntity.getPitchAndYaw(MinecraftClient.getInstance().player, this.displayBlock.getPos());
				this.displayBlock.pitch = pitchAndYaw.x;
				this.displayBlock.yaw = pitchAndYaw.y;
			}

			buf.writeFloat(displayBlock.pitch);
			buf.writeFloat(displayBlock.yaw);

			ClientSidePacketRegistry.INSTANCE.sendToServer(GlowcaseNetworking.SAVE_ITEM_DISPLAY, buf);
		}
	}

	public static void open(PacketContext context, PacketByteBuf buf) {
		BlockPos pos = buf.readBlockPos();

		context.getTaskQueue().execute(() -> {
			BlockEntity blockEntity = context.getPlayer().getEntityWorld().getBlockEntity(pos);

			if (blockEntity instanceof ItemDisplayBlockEntity) {
				MinecraftClient.getInstance().openScreen(new ItemDisplayBlockEditScreen((ItemDisplayBlockEntity) blockEntity));
			}
		});
	}
}
