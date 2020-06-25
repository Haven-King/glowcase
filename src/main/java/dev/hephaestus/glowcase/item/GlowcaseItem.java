package dev.hephaestus.glowcase.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class GlowcaseItem extends BlockItem {
	public GlowcaseItem(Block block) {
		super(block , new Item.Settings().group(ItemGroup.BUILDING_BLOCKS));
	}
}
