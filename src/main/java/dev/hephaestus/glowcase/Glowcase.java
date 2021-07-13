package dev.hephaestus.glowcase;

import dev.hephaestus.glowcase.block.HyperlinkBlock;
import dev.hephaestus.glowcase.block.ItemDisplayBlock;
import dev.hephaestus.glowcase.block.TextBlock;
import dev.hephaestus.glowcase.block.entity.HyperlinkBlockEntity;
import dev.hephaestus.glowcase.block.entity.ItemDisplayBlockEntity;
import dev.hephaestus.glowcase.block.entity.TextBlockEntity;
import dev.hephaestus.glowcase.item.GlowcaseItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Glowcase implements ModInitializer {
	public static final String MODID = "glowcase";

	public static final Block TEXT_BLOCK = Registry.register(Registry.BLOCK, id("text_block"), new TextBlock());
	public static final Item TEXT_BLOCK_ITEM = Registry.register(Registry.ITEM, id("text_block"), new GlowcaseItem(TEXT_BLOCK));
	public static final BlockEntityType<TextBlockEntity> TEXT_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("text_block"), FabricBlockEntityTypeBuilder.create(TextBlockEntity::new, TEXT_BLOCK).build());

	public static final Block HYPERLINK_BLOCK = Registry.register(Registry.BLOCK, id("hyperlink_block"), new HyperlinkBlock());
	public static final Item HYPERLINK_BLOCK_ITEM = Registry.register(Registry.ITEM, id("hyperlink_block"), new GlowcaseItem(HYPERLINK_BLOCK));
	public static final BlockEntityType<HyperlinkBlockEntity> HYPERLINK_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("hyperlink_block"), FabricBlockEntityTypeBuilder.create(HyperlinkBlockEntity::new, HYPERLINK_BLOCK).build());

	public static final Block ITEM_DISPLAY_BLOCK = Registry.register(Registry.BLOCK, id("item_display_block"), new ItemDisplayBlock());
	public static final Item ITEM_DISPLAY_BLOCK_ITEM = Registry.register(Registry.ITEM, id("item_display_block"), new GlowcaseItem(ITEM_DISPLAY_BLOCK));
	public static final BlockEntityType<ItemDisplayBlockEntity> ITEM_DISPLAY_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("item_display_block"), FabricBlockEntityTypeBuilder.create(ItemDisplayBlockEntity::new, ITEM_DISPLAY_BLOCK).build());

	public static Identifier id(String... path) {
		return new Identifier(MODID, String.join(".", path));
	}

	@Override
	public void onInitialize() {
	}
}
