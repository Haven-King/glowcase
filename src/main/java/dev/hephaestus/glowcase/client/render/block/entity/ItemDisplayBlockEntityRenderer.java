package dev.hephaestus.glowcase.client.render.block.entity;

import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;

public class ItemDisplayBlockEntityRenderer extends BlockEntityRenderer<ItemDisplayBlockEntity> {
	public ItemDisplayBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public void render(ItemDisplayBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		PlayerEntity player = MinecraftClient.getInstance().player;

		if (player == null) return;

		matrices.push();
		matrices.translate(0.5D, 0D, 0.5D);

		float yaw = 0F;
		float pitch = 0F;

		switch (entity.rotationType) {
			case TRACKING:
				Vec2f pitchAndYaw = ItemDisplayBlockEntity.getPitchAndYaw(player, entity.getPos());
				pitch = pitchAndYaw.x;
				yaw = pitchAndYaw.y;
				matrices.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(yaw));
				break;
			case HORIZONTAL:
				float rotation = -(entity.getCachedState().get(Properties.ROTATION) * 360) / 16.0F;
				matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(rotation));
				break;
			case LOCKED:
				pitch = entity.pitch;
				yaw = entity.yaw;
				matrices.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(yaw));
				break;
		}

		ItemStack stack = entity.getUseStack();
		Text name;
		if (stack.getItem() instanceof SpawnEggItem) {
			matrices.push();
			Entity renderEntity = entity.getDisplayEntity();
			if (renderEntity != null) {
				if (stack.hasCustomName()) {
					name = stack.getName();
				} else {
					name = renderEntity.getName();
				}

				float scale = renderEntity.getHeight() > renderEntity.getWidth() ? 1F / renderEntity.getHeight() : 0.5F;
				matrices.scale(scale, scale, scale);

				renderEntity.pitch = -pitch * 57.2957763671875F;
				renderEntity.setHeadYaw(yaw);

				matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180));
				EntityRenderer<? super Entity> entityRenderer = MinecraftClient.getInstance().getEntityRenderManager().getRenderer(renderEntity);
				entityRenderer.render(renderEntity, 0, tickDelta, matrices, vertexConsumers, light);
			} else {
				name = LiteralText.EMPTY;
			}
			matrices.pop();
			matrices.translate(0, 0.125F, 0);
			matrices.scale(0.5F, 0.5F, 0.5F);
		} else {
			name = stack.isEmpty() ? new TranslatableText("gui.glowcase.none") : (new LiteralText("")).append(stack.getName()).formatted(stack.getRarity().formatting);
			matrices.translate(0, 0.5, 0);
			matrices.scale(0.5F, 0.5F, 0.5F);
			matrices.multiply(Vector3f.POSITIVE_X.getRadialQuaternion(pitch));
			MinecraftClient.getInstance().getItemRenderer().renderItem(entity.getUseStack(), ModelTransformation.Mode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers);
		}

		if (entity.showName) {
			HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
			if (hitResult instanceof BlockHitResult && ((BlockHitResult) hitResult).getBlockPos().equals(entity.getPos())) {
				matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
				matrices.translate(0, 0, -0.4);

				float scale = 0.025F;
				matrices.scale(scale, scale, scale);

				int color = name.getStyle().getColor() == null ? 0xFFFFFF : name.getStyle().getColor().getRgb();
				matrices.translate(-MinecraftClient.getInstance().textRenderer.getWidth(name) / 2F, -4, 0);
				MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, name, 0, 0, color);
			}
		}

		matrices.pop();
	}
}
