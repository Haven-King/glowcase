package dev.hephaestus.glowcase.mixin.client.render.ber;

import dev.hephaestus.glowcase.client.render.block.entity.BakedBlockEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	/**
	 * Inject into rendering after all other BERs have been rendered (and as such all BERs have populated the BufferBuilders)
	 * and draw the buffers to the screen.
	 * TODO: If FREX/Fabric Rendering API implements a callback for batched BER rendering, conditionally enable this
	 *     mixin and use the callback if it exists.
	 */
	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;checkEmpty(Lnet/minecraft/client/util/math/MatrixStack;)V", ordinal = 0),
		slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"))
	)
	public void afterRenderBlockEntities(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		BakedBlockEntityRenderer.VertexBufferManager.INSTANCE.render(matrices, camera);
	}

	@Inject(method = "setWorld", at = @At("HEAD"))
	public void onSetWorld(ClientWorld clientWorld, CallbackInfo ci) {
		BakedBlockEntityRenderer.VertexBufferManager.INSTANCE.setWorld(clientWorld);
	}

}