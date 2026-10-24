package com.example.nametagcustomizer.mixin;

import com.example.nametagcustomizer.render.NametagRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void nametagcustomizer$renderLabel(EntityRenderState state, Text text, MatrixStack matrices,
                                               VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (NametagRenderer.render(state, text, matrices, vertexConsumers, light)) {
            ci.cancel();
        }
    }
}
