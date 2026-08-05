package com.scrowl.gradienttext.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.scrowl.gradienttext.config.GradientConfig;
import com.scrowl.gradienttext.gradient.GradientData;
import com.scrowl.gradienttext.gradient.GradientEngine;
import com.scrowl.gradienttext.handler.AnimationHandler;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Unique
    private static final ThreadLocal<float[]> gradienttext$pendingGlintColor = new ThreadLocal<>();

    @Inject(method = "render", at = @At("HEAD"))
    private void gradienttext$beforeRender(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, com.mojang.blaze3d.vertex.PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, BakedModel model, CallbackInfo ci) {
        gradienttext$pendingGlintColor.remove();

        if (!GradientConfig.get().isGradientGlowEnabled()) return;
        if (!stack.isEnchanted()) return;
        if (!GradientData.hasGradient(stack)) return;

        GradientData data = GradientData.fromItemStack(stack);
        if (data == null) return;

        int[] colors = data.getColors();
        if (colors == null || colors.length == 0) return;

        long time = AnimationHandler.getAnimationTime();
        float speed = data.getSpeed();
        float timeOffset = (time * speed * 0.01f) % 1.0f;

        int color = GradientEngine.getInterpolatedColor(colors, timeOffset);

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        gradienttext$pendingGlintColor.set(new float[]{r / 255.0f, g / 255.0f, b / 255.0f});
    }

    @Inject(method = "getFoilBuffer", at = @At("HEAD"), cancellable = true)
    private static void gradienttext$onGetFoilBuffer(MultiBufferSource bufferSource, RenderType renderType, boolean hasGlint, boolean isItem, CallbackInfoReturnable<VertexConsumer> cir) {
        float[] color = gradienttext$pendingGlintColor.get();
        if (color == null || !hasGlint) return;

        VertexConsumer original = cir.getReturnValue();
        if (original == null) return;

        VertexConsumer wrapped = (VertexConsumer) Proxy.newProxyInstance(
            VertexConsumer.class.getClassLoader(),
            new Class<?>[]{VertexConsumer.class},
            new ColorTintingHandler(original, color)
        );
        cir.setReturnValue(wrapped);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void gradienttext$afterRender(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, com.mojang.blaze3d.vertex.PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, BakedModel model, CallbackInfo ci) {
        gradienttext$pendingGlintColor.remove();
    }

    @Unique
    private static class ColorTintingHandler implements InvocationHandler {
        private final VertexConsumer delegate;
        private final float[] color;

        ColorTintingHandler(VertexConsumer delegate, float[] color) {
            this.delegate = delegate;
            this.color = color;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (args == null) args = new Object[0];

            String name = method.getName();

            if (name.equals("color") && args.length == 4 && args[0] instanceof Integer) {
                int r = (int) ((int) args[0] * color[0]);
                int g = (int) ((int) args[1] * color[1]);
                int b = (int) ((int) args[2] * color[2]);
                return method.invoke(delegate, r, g, b, args[3]);
            }

            if (name.equals("defaultColor") && args.length == 4 && args[0] instanceof Integer) {
                int r = (int) ((int) args[0] * color[0]);
                int g = (int) ((int) args[1] * color[1]);
                int b = (int) ((int) args[2] * color[2]);
                return method.invoke(delegate, r, g, b, args[3]);
            }

            return method.invoke(delegate, args);
        }
    }
}
