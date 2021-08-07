package dev.hephaestus.glowcase.mixin.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;

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
    // One option here would be to simply use normal translation, but I believe
    // the reason for using addToLastColumn here is to keep the offset constant
    // and not make it increase when the matrix scales up coordinates.
    // So, what we do here is get the axis unit vectors stored in the first
    // three columns of the matrix and use it to calculate a constant offset.
    // This way, we keep as close to the original behavior as possible.
    // Technically, only handling the Z axis vector should be required here
    // since the original code always passes in (0, 0, 0.03) as the vector, but
    // let's make it correct.
    // Another option would be to modify addToLastColumn directly because these
    // two lines of code are the only thing that ever calls that method, but
    // then its name would be wrong.

    private void translateUnscaled(Matrix4f mat, Vec3f vec) {
        // the matrix doesn't provide us with a way of accessing its data, so
        // this will have to do
        Vector4f x = new Vector4f(1, 0, 0, 0);
        Vector4f y = new Vector4f(0, 1, 0, 0);
        Vector4f z = new Vector4f(0, 0, 1, 0);
        x.transform(mat);
        y.transform(mat);
        z.transform(mat);
        Vec3f x1 = new Vec3f(x);
        Vec3f y1 = new Vec3f(y);
        Vec3f z1 = new Vec3f(z);
        x1.normalize();
        y1.normalize();
        z1.normalize();
        mat.addToLastColumn(new Vec3f(
            x1.getX() * vec.getX() + y1.getX() * vec.getY() + z1.getX() * vec.getZ(),
            x1.getY() * vec.getX() + y1.getY() * vec.getY() + z1.getY() * vec.getZ(),
            x1.getZ() * vec.getX() + y1.getZ() * vec.getY() + z1.getZ() * vec.getZ()
        ));
    }

    @Redirect(
        method = "drawInternal(Ljava/lang/String;FFIZLnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZIIZ)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Matrix4f;addToLastColumn(Lnet/minecraft/util/math/Vec3f;)V")
    )
    private void fixTranslation1(Matrix4f matrix4f, Vec3f vector) {
        this.translateUnscaled(matrix4f, vector);
    }

    @Redirect(
        method = "drawInternal(Lnet/minecraft/text/OrderedText;FFIZLnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZII)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Matrix4f;addToLastColumn(Lnet/minecraft/util/math/Vec3f;)V")
    )
    private void fixTranslation2(Matrix4f matrix4f, Vec3f vector) {
        this.translateUnscaled(matrix4f, vector);
    }
}
