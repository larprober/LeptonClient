package dev.lepton.event.events;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

/**
 * Fired once per frame while the world is being drawn, with a live matrix stack and
 * vertex consumer. Coordinates handed to the Renderer3D helpers are world-space; the
 * camera offset is applied for you.
 */
public class Render3DEvent {
    private static final Render3DEvent INSTANCE = new Render3DEvent();

    public MatrixStack matrices;
    public VertexConsumerProvider.Immediate vertexConsumers;
    public Vec3d cameraPos;
    public float tickDelta;

    public static Render3DEvent get(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers,
                                    Vec3d cameraPos, float tickDelta) {
        INSTANCE.matrices = matrices;
        INSTANCE.vertexConsumers = vertexConsumers;
        INSTANCE.cameraPos = cameraPos;
        INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }
}
