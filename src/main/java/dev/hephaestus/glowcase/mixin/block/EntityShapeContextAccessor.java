package dev.hephaestus.glowcase.mixin.block;

import net.minecraft.block.EntityShapeContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityShapeContext.class)
public interface EntityShapeContextAccessor {
	@Accessor("heldItem")
	ItemStack heldItem();
}
