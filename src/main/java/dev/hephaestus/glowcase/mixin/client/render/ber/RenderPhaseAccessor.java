package dev.hephaestus.glowcase.mixin.client.render.ber;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(RenderPhase.class)
public interface RenderPhaseAccessor {
	@Accessor
	static RenderPhase.TextureBase getNO_TEXTURE() {
		throw new RuntimeException("Mixin not applied");
	}

	@Accessor
	static RenderPhase.Transparency getTRANSLUCENT_TRANSPARENCY() {
		throw new RuntimeException("Mixin not applied");
	}

	@Accessor
	static RenderPhase.Shader getCOLOR_SHADER() {
		throw new RuntimeException("Mixin not applied");
	}
}
