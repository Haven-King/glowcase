package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3f;

public record HyperlinkBlockEntityRenderer(BlockEntityRendererFactory.Context context) implements BlockEntityRenderer<HyperlinkBlockEntity> {
	public static final ItemStack STACK = new ItemStack(Glowcase.HYPERLINK_BLOCK_ITEM);

	public void render(HyperlinkBlockEntity entity, float f, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		Camera camera = context.getRenderDispatcher().camera;
		matrices.push();
		matrices.translate(0.5D, 0.5D, 0.5D);
		matrices.scale(0.5F, 0.5F, 0.5F);
		float n = -camera.getYaw();
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(n));
		matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
		matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
		MinecraftClient.getInstance().getItemRenderer().renderItem(STACK, ModelTransformation.Mode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);

		HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
		if (hitResult instanceof BlockHitResult && ((BlockHitResult) hitResult).getBlockPos().equals(entity.getPos())) {
			float scale = 0.025F;
			matrices.scale(scale, scale, scale);
			matrices.translate(-MinecraftClient.getInstance().textRenderer.getWidth(entity.url) / 2F, -4, 0);

			String name = entity.url;
			int color = -1;

			MinecraftClient.getInstance().textRenderer.draw(name, 0, 0, color, false, matrices.peek().getModel(), vertexConsumers, false, 0, 0xF000F0);

			// doing shadow manually because the way text renderer adds the
			// shadow offset is wrong
			// this is going to slightly break with unicode fonts too since
			// they use an offset of 0.5 instead of 1 but better than nothing
			matrices.translate(1.0, 1.0, 0.03);
			int shadowColor = (int) ((color & 0xFF) * 0.25) | (int) ((color >> 8 & 0xFF) * 0.25) << 8 | (int) ((color >> 16 & 0xFF) * 0.25) << 16 | color & 0xFF000000;
			MinecraftClient.getInstance().textRenderer.draw(name, 0, 0, shadowColor, false, matrices.peek().getModel(), vertexConsumers, false, 0, 0xF000F0);
		}
		matrices.pop();
	}
}
