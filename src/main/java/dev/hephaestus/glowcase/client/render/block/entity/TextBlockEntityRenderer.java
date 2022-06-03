package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.mixin.client.render.ber.RenderPhaseAccessor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public class TextBlockEntityRenderer extends BakedBlockEntityRenderer<TextBlockEntity> {
	public TextBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
		super(context);
	}

	@Override
	public void renderUnbaked(TextBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if (entity.renderDirty) {
			entity.renderDirty = false;
			invalidateSelf(entity);
		}
	}

	@Override
	public void renderBaked(TextBlockEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		matrices.push();
		matrices.translate(0.5D, 0.5D, 0.5D);

		float rotation = -(blockEntity.getCachedState().get(Properties.ROTATION) * 360) / 16.0F;
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotation));

		switch (blockEntity.zOffset) {
			case FRONT -> matrices.translate(0D, 0D, 0.4D);
			case BACK -> matrices.translate(0D, 0D, -0.4D);
		}

		float scale = 0.010416667F * blockEntity.scale;
		matrices.scale(scale, -scale, scale);
		TextRenderer textRenderer = this.context.getTextRenderer();

		double maxLength = 0;
		double minLength = Double.MAX_VALUE;
		for (int i = 0; i < blockEntity.lines.size(); ++i) {
			maxLength = Math.max(maxLength, textRenderer.getWidth(blockEntity.lines.get(i)));
			minLength = Math.min(minLength, textRenderer.getWidth(blockEntity.lines.get(i)));
		}

		matrices.translate(0,  -((blockEntity.lines.size() - 0.25) * 12) / 2D, 0D);
		for (int i = 0; i < blockEntity.lines.size(); ++i) {
			double width = textRenderer.getWidth(blockEntity.lines.get(i));
			double dX = switch (blockEntity.textAlignment) {
				case LEFT -> -maxLength / 2D;
				case CENTER -> (maxLength - width) / 2D - maxLength / 2D;
				case RIGHT -> maxLength - width - maxLength / 2D;
			};

			matrices.push();
			matrices.translate(dX, 0, 0);

			if (blockEntity.shadowType == TextBlockEntity.ShadowType.PLATE && width > 0) {
				matrices.translate(0, 0, -0.025D);
				drawFillRect(matrices, vertexConsumers, (int) width + 5, (i + 1) * 12 - 2, -5, i * 12 - 2, 0x44000000);
				matrices.translate(0, 0, 0.025D);
			}

			if (blockEntity.shadowType == TextBlockEntity.ShadowType.DROP) {
				// Don't use the vanilla shadow rendering - it breaks when you try to use it in 3D
				int shadowColor = 0x88000000;
				matrices.translate(0, 0, -0.025D);
				textRenderer.draw(blockEntity.lines.get(i), 1, (i * 12) + 1, shadowColor, false, matrices.peek().getPositionMatrix(), vertexConsumers, false, 0, 15728880);
				matrices.translate(0, 0, 0.025D);
			}

			textRenderer.draw(blockEntity.lines.get(i), 0, i * 12, blockEntity.color, false, matrices.peek().getPositionMatrix(), vertexConsumers, false, 0, 15728880);

			matrices.pop();
		}

		matrices.pop();
	}

	// Use a custom render layer to render the text plate - mimics DrawableHelper's RenderSystem calls
	// TODO: This causes issues with transparency - not sure if these can be fixed?
	private final RenderLayer plateRenderLayer = RenderLayer.of("glowcase_text_plate", VertexFormats.POSITION_COLOR,
		VertexFormat.DrawMode.QUADS, 256, true, true, RenderLayer.MultiPhaseParameters.builder()
			.texture(RenderPhaseAccessor.getNO_TEXTURE())
			.transparency(RenderPhaseAccessor.getTRANSLUCENT_TRANSPARENCY())
			.shader(RenderPhaseAccessor.getCOLOR_SHADER())
			.build(false));

	@SuppressWarnings("SameParameterValue")
	private void drawFillRect(MatrixStack matrices, VertexConsumerProvider vcp, int x1, int y1, int x2, int y2, int color) {
		float red = (float)(color >> 16 & 255) / 255.0F;
		float green = (float)(color >> 8 & 255) / 255.0F;
		float blue = (float)(color & 255) / 255.0F;
		float alpha = (float)(color >> 24 & 255) / 255.0F;
		VertexConsumer consumer = vcp.getBuffer(plateRenderLayer);
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		consumer.vertex(matrix, x1, y2, 0.0f)
			.color(red, green, blue, alpha).next();
		consumer.vertex(matrix, x2, y2, 0.0f)
			.color(red, green, blue, alpha).next();
		consumer.vertex(matrix, x2, y1, 0.0f)
			.color(red, green, blue, alpha).next();
		consumer.vertex(matrix, x1, y1, 0.0f)
			.color(red, green, blue, alpha).next();
	}
}
