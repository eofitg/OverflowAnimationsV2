package org.polyfrost.overflowanimations.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import org.polyfrost.overflowanimations.config.OldAnimationsSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ItemStack.class)
public class ItemStackMixin {

    //todo: there is a better way

    @Redirect(method = "hasEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;hasEffect(Lnet/minecraft/item/ItemStack;)Z"))
    public boolean disablePotionGlint(Item instance, ItemStack stack) {
        if (OldAnimationsSettings.potionGlint && OldAnimationsSettings.INSTANCE.enabled && stack.getItem() instanceof ItemPotion) {
            return false;
        }
        return instance.hasEffect(stack);
    }

}