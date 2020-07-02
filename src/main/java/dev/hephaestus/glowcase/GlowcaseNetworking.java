package dev.hephaestus.glowcase;

import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.client.gui.screen.ingame.HyperlinkBlockEditScreen;
import dev.hephaestus.glowcase.client.gui.screen.ingame.ItemDisplayBlockEditScreen;
import dev.hephaestus.glowcase.client.gui.screen.ingame.TextBlockEditScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.util.Identifier;

public class GlowcaseNetworking implements ModInitializer, ClientModInitializer {
	public static final Identifier OPEN_TEXT_BLOCK_SCREEN = Glowcase.id("packet", "text_block", "open");
	public static final Identifier SAVE_TEXT_BLOCK = Glowcase.id("packet", "text_block", "save");

	public static final Identifier OPEN_HYPERLINK_SCREEN = Glowcase.id("packet", "hyperlink_block", "open");
	public static final Identifier SAVE_HYPERLINK = Glowcase.id("packet", "hyperlink_block", "save");
	public static final Identifier OPEN_HYPERLINK_CONFIRMATION = Glowcase.id("packet", "hyperlink_block", "confirmation");

	public static final Identifier OPEN_ITEM_DISPLAY_SCREEN = Glowcase.id("packet", "item_display_block", "open");
	public static final Identifier SAVE_ITEM_DISPLAY = Glowcase.id("packet", "item_display_block", "save");

	@Override
	public void onInitializeClient() {
		ClientSidePacketRegistry.INSTANCE.register(OPEN_TEXT_BLOCK_SCREEN, TextBlockEditScreen::open);
		ClientSidePacketRegistry.INSTANCE.register(OPEN_HYPERLINK_SCREEN, HyperlinkBlockEditScreen::open);
		ClientSidePacketRegistry.INSTANCE.register(OPEN_ITEM_DISPLAY_SCREEN, ItemDisplayBlockEditScreen::open);

		ClientSidePacketRegistry.INSTANCE.register(OPEN_HYPERLINK_CONFIRMATION, HyperlinkBlockEditScreen::openUrl);
	}

	@Override
	public void onInitialize() {
		ServerSidePacketRegistry.INSTANCE.register(SAVE_TEXT_BLOCK, TextBlockEntity::save);
		ServerSidePacketRegistry.INSTANCE.register(SAVE_HYPERLINK, HyperlinkBlockEntity::save);
		ServerSidePacketRegistry.INSTANCE.register(SAVE_ITEM_DISPLAY, ItemDisplayBlockEntity::save);
	}
}
