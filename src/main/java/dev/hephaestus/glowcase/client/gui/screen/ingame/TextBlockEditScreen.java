package dev.hephaestus.glowcase.client.gui.screen.ingame;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.GlowcaseNetworking;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class TextBlockEditScreen extends GlowcaseScreen {
	private final TextBlockEntity textBlockEntity;

	private SelectionManager selectionManager;
	private int currentRow;
	private long ticksSinceOpened = 0;
	private ButtonWidget changeAlignment;
	private TextFieldWidget colorEntryWidget;
	private ButtonWidget zOffsetToggle;
	private ButtonWidget shadowToggle;

	public TextBlockEditScreen(TextBlockEntity textBlockEntity) {
		this.textBlockEntity = textBlockEntity;
	}

	@Override
	public void init(MinecraftClient client, int width, int height) {
		super.init(client, width, height);

		int innerPadding = width / 100;

		if (this.client != null) {
			this.client.keyboard.enableRepeatEvents(true);
		}

		this.selectionManager = new SelectionManager(
				() -> this.textBlockEntity.lines.get(this.currentRow).getString(),
				(string) -> textBlockEntity.lines.set(this.currentRow, new LiteralText(string)),
				SelectionManager.makeClipboardGetter(this.client),
				SelectionManager.makeClipboardSetter(this.client),
				(string) -> true);

		ButtonWidget decreaseSize = new ButtonWidget(80, 0, 20, 20, new LiteralText("-"), action -> {
			this.textBlockEntity.scale -= (float) Math.max(0, 0.125);
		});

		ButtonWidget increaseSize = new ButtonWidget(100, 0, 20, 20, new LiteralText("+"), action -> {
			this.textBlockEntity.scale += 0.125;
		});

		this.changeAlignment = new ButtonWidget(120 + innerPadding, 0, 160, 20, new TranslatableText("gui.glowcase.alignment", this.textBlockEntity.textAlignment), action -> {
			switch (textBlockEntity.textAlignment) {
				case LEFT:      textBlockEntity.textAlignment = TextBlockEntity.TextAlignment.CENTER;   break;
				case CENTER:    textBlockEntity.textAlignment = TextBlockEntity.TextAlignment.RIGHT;    break;
				case RIGHT:     textBlockEntity.textAlignment = TextBlockEntity.TextAlignment.LEFT;     break;
			}

			this.changeAlignment.setMessage(new TranslatableText("gui.glowcase.alignment", this.textBlockEntity.textAlignment));
		});

		this.shadowToggle = new ButtonWidget(120 + innerPadding, 20 + innerPadding, 160, 20, new TranslatableText("gui.glowcase.shadow_type", this.textBlockEntity.shadowType), action -> {
			switch (textBlockEntity.shadowType) {
				case DROP:      textBlockEntity.shadowType = TextBlockEntity.ShadowType.PLATE;   break;
				case PLATE:     textBlockEntity.shadowType = TextBlockEntity.ShadowType.NONE;    break;
				case NONE:      textBlockEntity.shadowType = TextBlockEntity.ShadowType.DROP;     break;
			}

			this.shadowToggle.setMessage(new TranslatableText("gui.glowcase.shadow_type", this.textBlockEntity.shadowType));
		});

		this.colorEntryWidget = new TextFieldWidget(this.client.textRenderer, 280 + innerPadding * 2, 0, 50, 20, LiteralText.EMPTY);
		this.colorEntryWidget.setText("#" + Integer.toHexString(this.textBlockEntity.color & 0x00FFFFFF));
		this.colorEntryWidget.setChangedListener(string -> {
			TextColor color = TextColor.parse(this.colorEntryWidget.getText());
			this.textBlockEntity.color = color == null ? 0xFFFFFFFF : color.getRgb() | 0xFF000000;
		});

		this.zOffsetToggle = new ButtonWidget(330 + innerPadding * 3, 0, 80, 20, new LiteralText(this.textBlockEntity.zOffset.name()), action -> {
			switch (textBlockEntity.zOffset) {
				case FRONT:    textBlockEntity.zOffset = TextBlockEntity.ZOffset.CENTER;    break;
				case CENTER:   textBlockEntity.zOffset = TextBlockEntity.ZOffset.BACK;      break;
				case BACK:     textBlockEntity.zOffset = TextBlockEntity.ZOffset.FRONT;     break;
			}

			this.zOffsetToggle.setMessage(new LiteralText(this.textBlockEntity.zOffset.name()));
		});

		this.addButton(increaseSize);
		this.addButton(decreaseSize);
		this.addButton(this.changeAlignment);
		this.addButton(this.shadowToggle);
		this.addButton(this.zOffsetToggle);
		this.addChild(this.colorEntryWidget);
	}

	@Override
	public void tick() {
		++this.ticksSinceOpened;
	}

	@Override
	public void onClose() {
		this.save();
		super.onClose();
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (this.client != null) {
			super.render(matrices, mouseX, mouseY, delta);
			matrices.push();

			matrices.translate(0, 40 + 2 * this.width / 100F, 0);
			for (int i = 0; i < this.textBlockEntity.lines.size(); ++i) {
				switch (this.textBlockEntity.textAlignment) {
					case LEFT:
						this.client.textRenderer.drawWithShadow(matrices, this.textBlockEntity.lines.get(i), this.width / 10F, i * 12, this.textBlockEntity.color);
						break;
					case CENTER:
						this.client.textRenderer.drawWithShadow(matrices, this.textBlockEntity.lines.get(i), this.width / 2F - this.textRenderer.getWidth(this.textBlockEntity.lines.get(i)) / 2F, i * 12, this.textBlockEntity.color);
						break;
					case RIGHT:
						this.client.textRenderer.drawWithShadow(matrices, this.textBlockEntity.lines.get(i), this.width - this.width / 10F - this.textRenderer.getWidth(this.textBlockEntity.lines.get(i)), i * 12, this.textBlockEntity.color);
						break;
				}
			}

			int caretStart = this.selectionManager.getSelectionStart();
			int caretEnd = this.selectionManager.getSelectionEnd();

			if (caretStart >= 0) {
				int selectionStart = Math.min(caretStart, caretEnd);
				int selectionEnd = Math.max(caretStart, caretEnd);

				String line = this.textBlockEntity.lines.get(this.currentRow).getString();
				String preSelection = line.substring(0, MathHelper.clamp(line.length(), 0, selectionStart));
				int startX = this.client.textRenderer.getWidth(preSelection);

				float push;
				switch (this.textBlockEntity.textAlignment) {
					case LEFT:
						push = this.width / 10F;
						break;
					case CENTER:
						push = this.width / 2F - this.textRenderer.getWidth(line) / 2F;
						break;
					case RIGHT:
						push = this.width - this.width / 10F - this.textRenderer.getWidth(line);
						break;
					default:
						push = 0;
				}

				startX += push;


				int caretStartY = this.currentRow * 12;
				int caretEndY = this.currentRow * 12 + 9;
				if (this.ticksSinceOpened / 6 % 2 == 0 && !this.colorEntryWidget.isActive()) {
					if (selectionStart < line.length()) {
						fill(matrices, startX, caretStartY, startX + 1, caretEndY, 0xCCFFFFFF);
					} else {
						this.client.textRenderer.draw(matrices, "_", startX, this.currentRow * 12, 0xFFFFFFFF);
					}
				}

				if (caretStart != caretEnd) {
					int endX = startX + this.client.textRenderer.getWidth(line.substring(selectionStart, selectionEnd));

					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder bufferBuilder = tessellator.getBuffer();
					RenderSystem.disableTexture();
					RenderSystem.enableColorLogicOp();
					RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
					bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
					bufferBuilder.vertex(matrices.peek().getModel(), startX, caretEndY, 0.0F).color(0, 0, 255, 255).next();
					bufferBuilder.vertex(matrices.peek().getModel(), endX, caretEndY, 0.0F).color(0, 0, 255, 255).next();
					bufferBuilder.vertex(matrices.peek().getModel(), endX, caretStartY, 0.0F).color(0, 0, 255, 255).next();
					bufferBuilder.vertex(matrices.peek().getModel(), startX, caretStartY, 0.0F).color(0, 0, 255, 255).next();
					bufferBuilder.end();
					BufferRenderer.draw(bufferBuilder);
					RenderSystem.disableColorLogicOp();
					RenderSystem.enableTexture();
				}
			}

			matrices.pop();
			this.client.textRenderer.drawWithShadow(matrices, new TranslatableText("gui.glowcase.scale", this.textBlockEntity.scale), 7, 7, 0xFFFFFFFF);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean charTyped(char chr, int keyCode) {
		if (this.colorEntryWidget.isActive()) {
			return this.colorEntryWidget.charTyped(chr, keyCode);
		} else {
			this.selectionManager.insert(chr);
			return true;
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.colorEntryWidget.isActive()) {
			return this.colorEntryWidget.keyPressed(keyCode, scanCode, modifiers);
		} else {
			this.focusOn(null);
			if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
				this.textBlockEntity.lines.add(this.currentRow + 1, new LiteralText(
						this.textBlockEntity.lines.get(this.currentRow).getString().substring(
								MathHelper.clamp(this.selectionManager.getSelectionStart(), 0, this.textBlockEntity.lines.get(this.currentRow).getString().length()))
				));
				this.textBlockEntity.lines.set(this.currentRow, new LiteralText(
						this.textBlockEntity.lines.get(this.currentRow).getString().substring(0, this.selectionManager.getSelectionStart())
				));
				++this.currentRow;
				this.selectionManager.moveCaretToEnd();
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_UP) {
				this.currentRow = Math.max(this.currentRow - 1, 0);
				this.selectionManager.moveCaretToEnd();
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_DOWN) {
				this.currentRow = Math.min(this.currentRow + 1, (this.textBlockEntity.lines.size() - 1));
				this.selectionManager.moveCaretToEnd();
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && this.currentRow > 0 && this.textBlockEntity.lines.size() > 1 && this.selectionManager.getSelectionStart() == 0 && this.selectionManager.getSelectionEnd() == this.selectionManager.getSelectionStart()) {
				--this.currentRow;
				this.selectionManager.moveCaretToEnd();
				this.textBlockEntity.lines.set(this.currentRow,
						this.textBlockEntity.lines.get(this.currentRow).append(this.textBlockEntity.lines.get(this.currentRow + 1))
				);
				this.textBlockEntity.lines.remove(this.currentRow + 1);
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_DELETE && this.currentRow < this.textBlockEntity.lines.size() - 1 && this.selectionManager.getSelectionEnd() == this.textBlockEntity.lines.get(this.currentRow).getString().length()) {
				this.textBlockEntity.lines.set(this.currentRow,
						this.textBlockEntity.lines.get(this.currentRow).append(this.textBlockEntity.lines.get(this.currentRow + 1))
				);

				this.textBlockEntity.lines.remove(this.currentRow + 1);
				return true;
			} else {
				try {
					return this.selectionManager.handleSpecialKey(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
				} catch (StringIndexOutOfBoundsException e) {
					e.printStackTrace();
					MinecraftClient.getInstance().openScreen(null);
					return false;
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int topOffset = (int) (40 + 2 * this.width / 100F);
		if (mouseY > topOffset) {
			this.currentRow = MathHelper.clamp((int) (mouseY - topOffset) / 12, 0, this.textBlockEntity.lines.size() - 1);
			this.setFocused(null);
			return true;
		} else {
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}

	private void save() {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBlockPos(this.textBlockEntity.getPos());
		buf.writeFloat(this.textBlockEntity.scale);
		buf.writeVarInt(this.textBlockEntity.lines.size());
		buf.writeEnumConstant(this.textBlockEntity.textAlignment);
		buf.writeVarInt(this.textBlockEntity.color);
		buf.writeEnumConstant(this.textBlockEntity.zOffset);
		buf.writeEnumConstant(this.textBlockEntity.shadowType);

		for (MutableText text : this.textBlockEntity.lines) {
			buf.writeText(text);
		}

		ClientSidePacketRegistry.INSTANCE.sendToServer(GlowcaseNetworking.SAVE_TEXT_BLOCK, buf);
	}

	public static void open(PacketContext context, PacketByteBuf buf) {
		BlockPos pos = buf.readBlockPos();

		context.getTaskQueue().execute(() -> {
			BlockEntity blockEntity = context.getPlayer().getEntityWorld().getBlockEntity(pos);

			if (blockEntity instanceof TextBlockEntity) {
				MinecraftClient.getInstance().openScreen(new TextBlockEditScreen((TextBlockEntity) blockEntity));
			}
		});
	}
}
