package dev.lepton.mixin;

import dev.lepton.systems.modules.Modules;
import dev.lepton.systems.modules.render.Xray;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private static void onShouldDrawSide(BlockState state, BlockState otherState, Direction direction,
                                         CallbackInfoReturnable<Boolean> cir) {
        Modules modules = Modules.get();
        if (modules == null) return;

        Xray xray = modules.get(Xray.class);
        if (xray == null || !xray.isActive()) return;

        // Draw a face only when the block owning it is on the visible list. Everything
        // else loses all six faces and effectively disappears.
        cir.setReturnValue(xray.isVisible(state));
    }
}
