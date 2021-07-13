package dev.hephaestus.glowcase.client.gui.screen.ingame;

import dev.hephaestus.glowcase.GlowcaseNetworking;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class HyperlinkBlockEditScreen extends GlowcaseScreen {
	private final HyperlinkBlockEntity hyperlinkBlockEntity;

	private TextFieldWidget urlEntryWidget;

	protected HyperlinkBlockEditScreen(HyperlinkBlockEntity hyperlinkBlockEntity) {
		this.hyperlinkBlockEntity = hyperlinkBlockEntity;
	}

	@Environment(EnvType.CLIENT)
	public static void openUrl(PacketContext context, PacketByteBuf buf) {
		String url = buf.readString();

		context.getTaskQueue().execute(() -> {
			MinecraftClient.getInstance().openScreen(new ConfirmChatLinkScreen(
					bl -> {
						if (bl) {
							Util.getOperatingSystem().open(url);
						}

						MinecraftClient.getInstance().openScreen(null);
					},
					url,
					false));
		});
	}

	@Override
	public void init() {
		this.client.keyboard.setRepeatEvents(true);

		this.urlEntryWidget = new TextFieldWidget(this.client.textRenderer, width / 10, height / 2 - 10, 8 * width / 10, 20, LiteralText.EMPTY);
		this.urlEntryWidget.setText(this.hyperlinkBlockEntity.url);
		this.urlEntryWidget.setMaxLength(Integer.MAX_VALUE);

		this.urlEntryWidget.setMaxLength(Integer.MAX_VALUE);

		this.addDrawableChild(this.urlEntryWidget);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.onClose();
			return true;
		} else if (this.urlEntryWidget.isActive()) {
			return this.urlEntryWidget.keyPressed(keyCode, scanCode, modifiers);
		}else {
			return false;
		}
	}

	@Override
	public void onClose() {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBlockPos(this.hyperlinkBlockEntity.getPos());
		buf.writeString(this.urlEntryWidget.getText());
		ClientSidePacketRegistry.INSTANCE.sendToServer(GlowcaseNetworking.SAVE_HYPERLINK, buf);
		super.onClose();
	}

	public static void open(PacketContext context, PacketByteBuf buf) {
		BlockPos pos = buf.readBlockPos();

		context.getTaskQueue().execute(() -> {
			BlockEntity blockEntity = context.getPlayer().getEntityWorld().getBlockEntity(pos);

			if (blockEntity instanceof HyperlinkBlockEntity) {
				MinecraftClient.getInstance().openScreen(new HyperlinkBlockEditScreen((HyperlinkBlockEntity) blockEntity));
			}
		});
	}
}
