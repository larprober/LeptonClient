package dev.lepton.mixin;

import dev.lepton.Lepton;
import dev.lepton.event.events.Render3DEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * World-space render hook.
 *
 * <p>Minecraft 1.21.11's {@code WorldRenderer.render} no longer receives a MatrixStack --
 * it drives the GPU command pipeline directly. {@code renderTargetBlockOutline} is the
 * nearest call that still carries both a live {@link MatrixStack} and an
 * {@link VertexConsumerProvider.Immediate}, which is exactly the pair needed to emit
 * arbitrary geometry. Injecting at HEAD (not TAIL) matters: the method returns early when
 * the player is not looking at a block, and a TAIL injection would silently stop firing.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "renderTargetBlockOutline", at = @At("HEAD"))
    private void onRenderWorld(VertexConsumerProvider.Immediate vertexConsumers, MatrixStack matrices,
                               boolean translucent, WorldRenderState worldRenderState, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null || mc.player == null) return;

        Camera camera = mc.gameRenderer.getCamera();
        if (camera == null || !camera.isReady()) return;

        Vec3d cameraPos = camera.getCameraPos();
        float tickDelta = mc.getRenderTickCounter().getTickProgress(false);

        Lepton.EVENTS.post(Render3DEvent.get(matrices, vertexConsumers, cameraPos, tickDelta));
    }
}
