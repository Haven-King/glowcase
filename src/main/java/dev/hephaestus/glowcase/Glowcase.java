package dev.hephaestus.glowcase;

import dev.hephaestus.glowcase.block.HyperlinkBlock;
import dev.hephaestus.glowcase.block.ItemDisplayBlock;
import dev.hephaestus.glowcase.block.MailboxBlock;
import dev.hephaestus.glowcase.block.TextBlock;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.item.GlowcaseItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Glowcase implements ModInitializer {
	public static final String MODID = "glowcase";

	public static final Block TEXT_BLOCK = Registry.register(Registry.BLOCK, id("text_block"), new TextBlock());
	public static final Item TEXT_BLOCK_ITEM = Registry.register(Registry.ITEM, id("text_block"), new GlowcaseItem(TEXT_BLOCK));
	public static final BlockEntityType<TextBlockEntity> TEXT_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("text_block"), BlockEntityType.Builder.create(TextBlockEntity::new, TEXT_BLOCK).build(null));

	public static final Block HYPERLINK_BLOCK = Registry.register(Registry.BLOCK, id("hyperlink_block"), new HyperlinkBlock());
	public static final Item HYPERLINK_BLOCK_ITEM = Registry.register(Registry.ITEM, id("hyperlink_block"), new GlowcaseItem(HYPERLINK_BLOCK));
	public static final BlockEntityType<HyperlinkBlockEntity> HYPERLINK_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("hyperlink_block"), BlockEntityType.Builder.create(HyperlinkBlockEntity::new, HYPERLINK_BLOCK).build(null));

	public static final Block ITEM_DISPLAY_BLOCK = Registry.register(Registry.BLOCK, id("item_display_block"), new ItemDisplayBlock());
	public static final Item ITEM_DISPLAY_BLOCK_ITEM = Registry.register(Registry.ITEM, id("item_display_block"), new GlowcaseItem(ITEM_DISPLAY_BLOCK));
	public static final BlockEntityType<ItemDisplayBlockEntity> ITEM_DISPLAY_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("item_display_block"), BlockEntityType.Builder.create(ItemDisplayBlockEntity::new, ITEM_DISPLAY_BLOCK).build(null));

//	public static final Block MAILBOX_BLOCK = Registry.register(Registry.BLOCK, id("mailbox_block"), new MailboxBlock());
//	public static final Item MAILBOX_BLOCK_ITEM = Registry.register(Registry.ITEM, id("mailbox_block"), new GlowcaseItem(MAILBOX_BLOCK));

	public static Identifier id(String... path) {
		return new Identifier(MODID, String.join(".", path));
	}

	@Override
	public void onInitialize() {
	}
}
