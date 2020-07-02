package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import net.minecraft.block.SignBlock;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;

public class TextBlockEntityRenderer extends BlockEntityRenderer<TextBlockEntity> {
	public TextBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public void render(TextBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		matrices.push();
		matrices.translate(0.5D, 0.5D, 0.5D);

		float rotation = -(blockEntity.getCachedState().get(Properties.ROTATION) * 360) / 16.0F;
		matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(rotation));

		switch (blockEntity.zOffset) {
			case FRONT: matrices.translate(0D, 0D, 0.4D); break;
			case BACK: matrices.translate(0D, 0D, -0.4D); break;
		}

		float scale = 0.010416667F * blockEntity.scale;
		matrices.scale(scale, -scale, scale);
		TextRenderer textRenderer = dispatcher.getTextRenderer();

		double maxLength = 0;
		double minLength = Double.MAX_VALUE;
		for (int i = 0; i < blockEntity.lines.size(); ++i) {
			maxLength = Math.max(maxLength, textRenderer.getWidth(blockEntity.lines.get(i)));
			minLength = Math.min(minLength, textRenderer.getWidth(blockEntity.lines.get(i)));
		}

		matrices.translate(0,  -((blockEntity.lines.size() - 0.25) * 12) / 2D, 0D);
		for (int i = 0; i < blockEntity.lines.size(); ++i) {
			double dX = 0;

			double width = textRenderer.getWidth(blockEntity.lines.get(i));
			switch (blockEntity.textAlignment) {
				case LEFT:      dX = -maxLength / 2D;                           break;
				case CENTER:    dX = (maxLength - width) / 2D - maxLength / 2D; break;
				case RIGHT:     dX = maxLength - width - maxLength / 2D;        break;
			}

			matrices.push();
			matrices.translate(dX, 0, 0);

			if (blockEntity.shadowType == TextBlockEntity.ShadowType.PLATE && width > 0) {
				DrawableHelper.fill(matrices, -5, i * 12 - 2, (int) width + 5, (i + 1) * 12 - 2, 0x44000000);
			}

			if (blockEntity.shadowType == TextBlockEntity.ShadowType.DROP) {
				textRenderer.drawWithShadow(matrices, blockEntity.lines.get(i), 0, i * 12, blockEntity.color);
			} else {
				textRenderer.draw(matrices, blockEntity.lines.get(i), 0, i * 12, blockEntity.color);
			}

			matrices.pop();
		}

		matrices.pop();
	}
}
