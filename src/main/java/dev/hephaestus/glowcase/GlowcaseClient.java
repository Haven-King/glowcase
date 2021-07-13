package dev.hephaestus.glowcase;

import dev.hephaestus.glowcase.client.render.block.entity.BakedBlockEntityRenderer;
import dev.hephaestus.glowcase.client.render.block.entity.HyperlinkBlockEntityRenderer;
import dev.hephaestus.glowcase.client.render.block.entity.ItemDisplayBlockEntityRenderer;
import dev.hephaestus.glowcase.client.render.block.entity.TextBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

@Environment(EnvType.CLIENT)
public class GlowcaseClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererRegistry.INSTANCE.register(Glowcase.TEXT_BLOCK_ENTITY, TextBlockEntityRenderer::new);
		BlockEntityRendererRegistry.INSTANCE.register(Glowcase.HYPERLINK_BLOCK_ENTITY, HyperlinkBlockEntityRenderer::new);
		BlockEntityRendererRegistry.INSTANCE.register(Glowcase.ITEM_DISPLAY_BLOCK_ENTITY, ItemDisplayBlockEntityRenderer::new);

		WorldRenderEvents.AFTER_ENTITIES.register(ctx ->
			BakedBlockEntityRenderer.VertexBufferManager.INSTANCE.render(ctx.matrixStack(), ctx.projectionMatrix(), ctx.camera()));
	}
}
