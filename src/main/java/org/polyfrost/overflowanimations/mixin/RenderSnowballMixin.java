package org.polyfrost.overflowanimations.mixin;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import org.polyfrost.overflowanimations.config.MainModSettings;
import org.polyfrost.overflowanimations.config.OldAnimationsSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSnowball.class)
public abstract class RenderSnowballMixin<T extends Entity> extends Render<T> {

    public RenderSnowballMixin(RenderManager renderManager) {
        super(renderManager);
    }

    @ModifyArg(
            method = "doRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V",
                    ordinal = 0
            ),
            index = 0
    )
    private float overflowAnimations$fixRotationY(float angle) {
        OldAnimationsSettings settings = MainModSettings.INSTANCE.getOldSettings();
        if (!settings.enabled) { return angle; }
        if (settings.getItemSprites() || settings.getOldProjectiles()) {
            return 180.0F + angle;
        }
        return angle;
    }

    @ModifyArg(
            method = "doRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V",
                    ordinal = 1
            ),
            index = 0
    )
    private float overflowAnimations$fixRotationX(float original) {
        OldAnimationsSettings settings = MainModSettings.INSTANCE.getOldSettings();
        if (!settings.enabled) { return original; }
        return (settings.getItemSprites() || settings.getOldProjectiles() ? -1F : 1F) * original;
    }

    @Inject(
            method = "doRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V"
            )
    )
    public void overflowAnimations$shiftProjectile(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (!MainModSettings.INSTANCE.getOldSettings().getOldProjectiles() || !MainModSettings.INSTANCE.getOldSettings().enabled) { return; }
        GlStateManager.translate(0.0F, 0.25F, 0.0F);
    }

}
