package dev.lepton.mixin;

import dev.lepton.systems.modules.render.Fullbright;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Substitutes the brightness value on read for Fullbright.
 *
 * <p>This fires for every option read in the game, so the guard on the other side is a
 * single volatile boolean check before anything else happens.
 */
@Mixin(SimpleOption.class)
public class SimpleOptionMixin {
    @Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
    private void onGetValue(CallbackInfoReturnable<Object> cir) {
        Double override = Fullbright.brightnessOverride(this);
        if (override != null) cir.setReturnValue(override);
    }
}
