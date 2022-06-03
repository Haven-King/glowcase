package dev.hephaestus.glowcase.client.gui.screen.ingame;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.networking.TextBlockChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
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
	public void init() {
		super.init();

		int innerPadding = width / 100;

		if (this.client != null) {
			this.client.keyboard.setRepeatEvents(true);
		}

		this.selectionManager = new SelectionManager(
				() -> this.textBlockEntity.lines.get(this.currentRow).getString(),
				(string) -> {
					textBlockEntity.lines.set(this.currentRow, new LiteralText(string));
					this.textBlockEntity.renderDirty = true;
				},
				SelectionManager.makeClipboardGetter(this.client),
				SelectionManager.makeClipboardSetter(this.client),
				(string) -> true);

		ButtonWidget decreaseSize = new ButtonWidget(80, 0, 20, 20, new LiteralText("-"), action -> {
			this.textBlockEntity.scale -= (float) Math.max(0, 0.125);
			this.textBlockEntity.renderDirty = true;
		});

		ButtonWidget increaseSize = new ButtonWidget(100, 0, 20, 20, new LiteralText("+"), action -> {
			this.textBlockEntity.scale += 0.125;
			this.textBlockEntity.renderDirty = true;
		});

		this.changeAlignment = new ButtonWidget(120 + innerPadding, 0, 160, 20, new TranslatableText("gui.glowcase.alignment", this.textBlockEntity.textAlignment), action -> {
			switch (textBlockEntity.textAlignment) {
				case LEFT -> textBlockEntity.textAlignment = TextBlockEntity.TextAlignment.CENTER;
				case CENTER -> textBlockEntity.textAlignment = TextBlockEntity.TextAlignment.RIGHT;
				case RIGHT -> textBlockEntity.textAlignment = TextBlockEntity.TextAlignment.LEFT;
			}
			this.textBlockEntity.renderDirty = true;

			this.changeAlignment.setMessage(new TranslatableText("gui.glowcase.alignment", this.textBlockEntity.textAlignment));
		});

		this.shadowToggle = new ButtonWidget(120 + innerPadding, 20 + innerPadding, 160, 20, new TranslatableText("gui.glowcase.shadow_type", this.textBlockEntity.shadowType), action -> {
			switch (textBlockEntity.shadowType) {
				case DROP -> textBlockEntity.shadowType = TextBlockEntity.ShadowType.PLATE;
				case PLATE -> textBlockEntity.shadowType = TextBlockEntity.ShadowType.NONE;
				case NONE -> textBlockEntity.shadowType = TextBlockEntity.ShadowType.DROP;
			}
			this.textBlockEntity.renderDirty = true;

			this.shadowToggle.setMessage(new TranslatableText("gui.glowcase.shadow_type", this.textBlockEntity.shadowType));
		});

		this.colorEntryWidget = new TextFieldWidget(this.client.textRenderer, 280 + innerPadding * 2, 0, 50, 20, LiteralText.EMPTY);
		this.colorEntryWidget.setText("#" + Integer.toHexString(this.textBlockEntity.color & 0x00FFFFFF));
		this.colorEntryWidget.setChangedListener(string -> {
			TextColor color = TextColor.parse(this.colorEntryWidget.getText());
			this.textBlockEntity.color = color == null ? 0xFFFFFFFF : color.getRgb() | 0xFF000000;
			this.textBlockEntity.renderDirty = true;
		});

		this.zOffsetToggle = new ButtonWidget(330 + innerPadding * 3, 0, 80, 20, new LiteralText(this.textBlockEntity.zOffset.name()), action -> {
			switch (textBlockEntity.zOffset) {
				case FRONT -> textBlockEntity.zOffset = TextBlockEntity.ZOffset.CENTER;
				case CENTER -> textBlockEntity.zOffset = TextBlockEntity.ZOffset.BACK;
				case BACK -> textBlockEntity.zOffset = TextBlockEntity.ZOffset.FRONT;
			}
			this.textBlockEntity.renderDirty = true;

			this.zOffsetToggle.setMessage(new LiteralText(this.textBlockEntity.zOffset.name()));
		});

		this.addDrawableChild(increaseSize);
		this.addDrawableChild(decreaseSize);
		this.addDrawableChild(this.changeAlignment);
		this.addDrawableChild(this.shadowToggle);
		this.addDrawableChild(this.zOffsetToggle);
		this.addDrawableChild(this.colorEntryWidget);
	}

	@Override
	public void tick() {
		++this.ticksSinceOpened;
	}

	@Override
	public void close() {
		TextBlockChannel.sync(this.textBlockEntity);
		super.close();
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (this.client != null) {
			super.render(matrices, mouseX, mouseY, delta);
			matrices.push();

			matrices.translate(0, 40 + 2 * this.width / 100F, 0);
			for (int i = 0; i < this.textBlockEntity.lines.size(); ++i) {
				switch (this.textBlockEntity.textAlignment) {
					case LEFT -> this.client.textRenderer.drawWithShadow(matrices, this.textBlockEntity.lines.get(i), this.width / 10F, i * 12, this.textBlockEntity.color);
					case CENTER -> this.client.textRenderer.drawWithShadow(matrices, this.textBlockEntity.lines.get(i), this.width / 2F - this.textRenderer.getWidth(this.textBlockEntity.lines.get(i)) / 2F, i * 12, this.textBlockEntity.color);
					case RIGHT -> this.client.textRenderer.drawWithShadow(matrices, this.textBlockEntity.lines.get(i), this.width - this.width / 10F - this.textRenderer.getWidth(this.textBlockEntity.lines.get(i)), i * 12, this.textBlockEntity.color);
				}
			}

			int caretStart = this.selectionManager.getSelectionStart();
			int caretEnd = this.selectionManager.getSelectionEnd();

			if (caretStart >= 0) {
				String line = this.textBlockEntity.lines.get(this.currentRow).getString();
				int selectionStart = MathHelper.clamp(Math.min(caretStart, caretEnd), 0, line.length());
				int selectionEnd = MathHelper.clamp(Math.max(caretStart, caretEnd), 0, line.length());

				String preSelection = line.substring(0, MathHelper.clamp(line.length(), 0, selectionStart));
				int startX = this.client.textRenderer.getWidth(preSelection);

				float push = switch (this.textBlockEntity.textAlignment) {
					case LEFT -> this.width / 10F;
					case CENTER -> this.width / 2F - this.textRenderer.getWidth(line) / 2F;
					case RIGHT -> this.width - this.width / 10F - this.textRenderer.getWidth(line);
				};

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
					bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
					bufferBuilder.vertex(matrices.peek().getPositionMatrix(), startX, caretEndY, 0.0F).color(0, 0, 255, 255).next();
					bufferBuilder.vertex(matrices.peek().getPositionMatrix(), endX, caretEndY, 0.0F).color(0, 0, 255, 255).next();
					bufferBuilder.vertex(matrices.peek().getPositionMatrix(), endX, caretStartY, 0.0F).color(0, 0, 255, 255).next();
					bufferBuilder.vertex(matrices.peek().getPositionMatrix(), startX, caretStartY, 0.0F).color(0, 0, 255, 255).next();
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
	public boolean shouldPause() {
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
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				this.close();
				return true;
			} else {
				return this.colorEntryWidget.keyPressed(keyCode, scanCode, modifiers);
			}
		} else {
			this.focusOn(null);
			if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
				this.textBlockEntity.lines.add(this.currentRow + 1, new LiteralText(
						this.textBlockEntity.lines.get(this.currentRow).getString().substring(
								MathHelper.clamp(this.selectionManager.getSelectionStart(), 0, this.textBlockEntity.lines.get(this.currentRow).getString().length()))
				));
				this.textBlockEntity.lines.set(this.currentRow, new LiteralText(
						this.textBlockEntity.lines.get(this.currentRow).getString().substring(0, MathHelper.clamp(this.selectionManager.getSelectionStart(), 0, this.textBlockEntity.lines.get(this.currentRow).getString().length()))
				));
				this.textBlockEntity.renderDirty = true;
				++this.currentRow;
				this.selectionManager.moveCursorToEnd(false);
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_UP) {
				this.currentRow = Math.max(this.currentRow - 1, 0);
				this.selectionManager.moveCursorToEnd(false);
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_DOWN) {
				this.currentRow = Math.min(this.currentRow + 1, (this.textBlockEntity.lines.size() - 1));
				this.selectionManager.moveCursorToEnd(false);
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && this.currentRow > 0 && this.textBlockEntity.lines.size() > 1 && this.selectionManager.getSelectionStart() == 0 && this.selectionManager.getSelectionEnd() == this.selectionManager.getSelectionStart()) {
				--this.currentRow;
				this.selectionManager.moveCursorToEnd(false);
				this.textBlockEntity.lines.set(this.currentRow,
						this.textBlockEntity.lines.get(this.currentRow).append(this.textBlockEntity.lines.get(this.currentRow + 1))
				);
				this.textBlockEntity.lines.remove(this.currentRow + 1);
				this.textBlockEntity.renderDirty = true;
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_DELETE && this.currentRow < this.textBlockEntity.lines.size() - 1 && this.selectionManager.getSelectionEnd() == this.textBlockEntity.lines.get(this.currentRow).getString().length()) {
				this.textBlockEntity.lines.set(this.currentRow,
						this.textBlockEntity.lines.get(this.currentRow).append(this.textBlockEntity.lines.get(this.currentRow + 1))
				);

				this.textBlockEntity.lines.remove(this.currentRow + 1);
				this.textBlockEntity.renderDirty = true;
				return true;
			} else {
				try {
					return this.selectionManager.handleSpecialKey(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
				} catch (StringIndexOutOfBoundsException e) {
					e.printStackTrace();
					MinecraftClient.getInstance().setScreen(null);
					return false;
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int topOffset = (int) (40 + 2 * this.width / 100F);
		if (!this.colorEntryWidget.mouseClicked(mouseX, mouseY, button)) {
			this.colorEntryWidget.setTextFieldFocused(false);
		}
		if (mouseY > topOffset) {
			this.currentRow = MathHelper.clamp((int) (mouseY - topOffset) / 12, 0, this.textBlockEntity.lines.size() - 1);
			this.setFocused(null);
			return true;
		} else {
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}
}
