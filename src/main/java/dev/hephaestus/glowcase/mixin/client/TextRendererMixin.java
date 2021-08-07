package dev.hephaestus.glowcase.mixin.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextRenderer.class)
public class TextRendererMixin {
    // These addToLastColumn calls are originally used for offsetting the text
    // shadow, however simply adding to the last column of the matrix to produce
    // an offset is wrong since it will ignore any previous transformations made
    // and will only work correctly in special cases (the transformation matrix
    // having no rotation).
    // So since what's actually wanted here is a translation, that's what we do.
    // Another option would be to modify addToLastColumn directly because these
    // two lines of code are the only thing that ever calls that method, but
    // then its name would be wrong.

    @Redirect(
        method = "drawInternal(Ljava/lang/String;FFIZLnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZIIZ)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Matrix4f;addToLastColumn(Lnet/minecraft/util/math/Vec3f;)V")
    )
    private void fixTranslation1(Matrix4f matrix4f, Vec3f vector) {
        matrix4f.multiplyByTranslation(vector.getX(), vector.getY(), vector.getZ());
    }

    @Redirect(
        method = "drawInternal(Lnet/minecraft/text/OrderedText;FFIZLnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZII)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Matrix4f;addToLastColumn(Lnet/minecraft/util/math/Vec3f;)V")
    )
    private void fixTranslation2(Matrix4f matrix4f, Vec3f vector) {
        matrix4f.multiplyByTranslation(vector.getX(), vector.getY(), vector.getZ());
    }
}
