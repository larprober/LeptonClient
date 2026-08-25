package dev.lepton.mixin;

import dev.lepton.systems.modules.movement.NoFall;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Reaches the integrated server's copy of the player.
 *
 * <p>In singleplayer the server runs in this JVM, so this injection fires for the
 * ServerPlayerEntity that actually applies fall damage. On a real server that object
 * lives on another machine and this code never runs there -- which is exactly why
 * NoFall is a singleplayer-only capability.
 */
@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void onHandleFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource,
                                    CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        if (!NoFall.shouldCancelFallDamage(self)) return;

        // false means "no damage was taken".
        cir.setReturnValue(false);
    }
}
