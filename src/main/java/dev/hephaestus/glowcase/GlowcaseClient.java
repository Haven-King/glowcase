package dev.hephaestus.glowcase;

import dev.hephaestus.glowcase.client.gui.screen.ingame.HyperlinkBlockEditScreen;
import dev.hephaestus.glowcase.client.gui.screen.ingame.TextBlockEditScreen;
import dev.hephaestus.glowcase.client.render.block.entity.HyperlinkBlockEntityRenderer;
import dev.hephaestus.glowcase.client.render.block.entity.ItemDisplayBlockEntityRenderer;
import dev.hephaestus.glowcase.client.render.block.entity.TextBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;

@Environment(EnvType.CLIENT)
public class GlowcaseClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientSidePacketRegistry.INSTANCE.register(Glowcase.OPEN_TEXT_BLOCK_SCREEN, TextBlockEditScreen::open);
		ClientSidePacketRegistry.INSTANCE.register(Glowcase.OPEN_HYPERLINK_SCREEN, HyperlinkBlockEditScreen::open);
		ClientSidePacketRegistry.INSTANCE.register(Glowcase.OPEN_HYPERLINK_CONFIRMATION, HyperlinkBlockEditScreen::openUrl);
		BlockEntityRendererRegistry.INSTANCE.register(Glowcase.TEXT_BLOCK_ENTITY, TextBlockEntityRenderer::new);
		BlockEntityRendererRegistry.INSTANCE.register(Glowcase.HYPERLINK_BLOCK_ENTITY, HyperlinkBlockEntityRenderer::new);
		BlockEntityRendererRegistry.INSTANCE.register(Glowcase.ITEM_DISPLAY_BLOCK_ENTITY, ItemDisplayBlockEntityRenderer::new);
	}
}
