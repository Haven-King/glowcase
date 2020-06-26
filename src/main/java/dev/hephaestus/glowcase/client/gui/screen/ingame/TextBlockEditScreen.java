package dev.hephaestus.glowcase.client.gui.screen.ingame;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
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
public class TextBlockEditScreen extends Screen {
	private final TextBlockEntity textBlockEntity;

	private SelectionManager selectionManager;
	private int currentRow;
	private long ticksSinceOpened = 0;
	private ButtonWidget increaseSize;
	private ButtonWidget decreaseSize;
	private ButtonWidget changeAlignment;
	private TextFieldWidget colorEntryWidget;
	private ButtonWidget zOffsetToggle;

	public TextBlockEditScreen(TextBlockEntity textBlockEntity) {
		super(LiteralText.EMPTY);
		this.textBlockEntity = textBlockEntity;
	}

	@Override
	public void init(MinecraftClient client, int width, int height) {
		super.init(client, width, height);
		this.client.keyboard.enableRepeatEvents(true);

		this.selectionManager = new SelectionManager(
				() -> this.textBlockEntity.lines.get(this.currentRow).getString(),
				(string) -> textBlockEntity.lines.set(this.currentRow, new LiteralText(string)),
				SelectionManager.makeClipboardGetter(this.client),
				SelectionManager.makeClipboardSetter(this.client),
				(string) -> true);

		this.increaseSize = new ButtonWidget(100, 0, 20, 20, new LiteralText("+"), action -> {
			this.textBlockEntity.scale += 0.125;
			this.save();
		});

		this.decreaseSize = new ButtonWidget(80, 0, 20, 20, new LiteralText("-"), action -> {
			this.textBlockEntity.scale -= (float) Math.max(0, 0.125);
			this.save();
		});

		this.changeAlignment = new ButtonWidget(130, 0, 160, 20, new TranslatableText("gui.glowcase.alignment", this.textBlockEntity.textAlignment), action -> {
			switch (textBlockEntity.textAlignment) {
				case LEFT:      textBlockEntity.textAlignment = TextBlockEntity.TextAlignment.CENTER;   break;
				case CENTER:    textBlockEntity.textAlignment = TextBlockEntity.TextAlignment.RIGHT;    break;
				case RIGHT:     textBlockEntity.textAlignment = TextBlockEntity.TextAlignment.LEFT;     break;
			}

			this.changeAlignment.setMessage(new TranslatableText("gui.glowcase.alignment", this.textBlockEntity.textAlignment));
			this.save();
		});

		this.colorEntryWidget = new TextFieldWidget(this.client.textRenderer, 300, 0, 50, 20, LiteralText.EMPTY);
		this.colorEntryWidget.setText("#" + Integer.toHexString(this.textBlockEntity.color & 0x00FFFFFF));
		this.colorEntryWidget.setChangedListener(string -> {
			TextColor color = TextColor.parse(this.colorEntryWidget.getText());
			this.textBlockEntity.color = color == null ? 0xFFFFFFFF : color.getRgb() | 0xFF000000;
			this.save();
		});

		this.zOffsetToggle = new ButtonWidget(360, 0, 80, 20, new LiteralText(this.textBlockEntity.zOffset.name()), action -> {
			switch (textBlockEntity.zOffset) {
				case FRONT:    textBlockEntity.zOffset = TextBlockEntity.ZOffset.CENTER;    break;
				case CENTER:   textBlockEntity.zOffset = TextBlockEntity.ZOffset.BACK;      break;
				case BACK:     textBlockEntity.zOffset = TextBlockEntity.ZOffset.FRONT;     break;
			}

			this.zOffsetToggle.setMessage(new LiteralText(this.textBlockEntity.zOffset.name()));
			this.save();
		});

		this.addButton(this.increaseSize);
		this.addButton(this.decreaseSize);
		this.addButton(this.changeAlignment);
		this.addButton(this.zOffsetToggle);
		this.addChild(this.colorEntryWidget);
	}

	@Override
	public void tick() {
		++this.ticksSinceOpened;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (this.client != null) {
			fill(matrices, 0, 0, this.client.getWindow().getFramebufferWidth(),  this.client.getWindow().getFramebufferHeight(), 0x88000000);

			matrices.push();

			for (int i = 0; i < this.textBlockEntity.lines.size(); ++i) {
				switch(this.textBlockEntity.textAlignment) {
					case LEFT: 		this.client.textRenderer.drawWithShadow(matrices, this.textBlockEntity.lines.get(i), this.width / 10F, this.width / 10F + i * 12, this.textBlockEntity.color); break;
					case CENTER:	this.client.textRenderer.drawWithShadow(matrices, this.textBlockEntity.lines.get(i), this.width / 2F - this.textRenderer.getWidth(this.textBlockEntity.lines.get(i)) / 2F, this.width / 10F + i * 12, this.textBlockEntity.color); break;
					case RIGHT: 	this.client.textRenderer.drawWithShadow(matrices, this.textBlockEntity.lines.get(i), this.width - this.width / 10F - this.textRenderer.getWidth(this.textBlockEntity.lines.get(i)), this.width / 10F + i * 12, this.textBlockEntity.color); break;
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

				if (this.ticksSinceOpened / 6 % 2 == 0 && !this.colorEntryWidget.isActive()) {
					if (selectionStart < line.length()) {
						fill(matrices, startX, this.currentRow * 12, startX + 1, this.currentRow * 12 + 9, 0xCCFFFFFF);
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
					bufferBuilder.vertex(matrices.peek().getModel(), startX, this.currentRow * 12 + 9, 0.0F).color(0, 0, 255, 255).next();
					bufferBuilder.vertex(matrices.peek().getModel(), endX, this.currentRow * 12 + 9, 0.0F).color(0, 0, 255, 255).next();
					bufferBuilder.vertex(matrices.peek().getModel(), endX, this.currentRow * 12, 0.0F).color(0, 0, 255, 255).next();
					bufferBuilder.vertex(matrices.peek().getModel(), startX, this.currentRow * 12, 0.0F).color(0, 0, 255, 255).next();
					bufferBuilder.end();
					BufferRenderer.draw(bufferBuilder);
					RenderSystem.disableColorLogicOp();
					RenderSystem.enableTexture();
				}
			}

			matrices.pop();
			this.client.textRenderer.drawWithShadow(matrices, new TranslatableText("gui.glowcase.scale", this.textBlockEntity.scale), 7, 7, 0xFFFFFFFF);
			this.increaseSize.render(matrices, mouseX, mouseY, delta);
			this.decreaseSize.render(matrices, mouseX, mouseY, delta);
			this.changeAlignment.render(matrices, mouseX, mouseY, delta);
			this.zOffsetToggle.render(matrices, mouseX, mouseY, delta);
			this.colorEntryWidget.render(matrices, mouseX, mouseY, delta);
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
			this.save();
			return true;
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.colorEntryWidget.isActive()) {
			return this.colorEntryWidget.keyPressed(keyCode, scanCode, modifiers);
		} else {
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
				this.save();
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
				this.save();
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_DELETE && this.currentRow < this.textBlockEntity.lines.size() - 1 && this.selectionManager.getSelectionEnd() == this.textBlockEntity.lines.get(this.currentRow).getString().length()) {
				this.textBlockEntity.lines.set(this.currentRow,
						this.textBlockEntity.lines.get(this.currentRow).append(this.textBlockEntity.lines.get(this.currentRow + 1))
				);

				this.textBlockEntity.lines.remove(this.currentRow + 1);
				return true;
			} else {
				return this.selectionManager.handleSpecialKey(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
			}
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

		for (MutableText text : this.textBlockEntity.lines) {
			buf.writeText(text);
		}

		ClientSidePacketRegistry.INSTANCE.sendToServer(Glowcase.SAVE_TEXT_BLOCK_CHANGE, buf);
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
