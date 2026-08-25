package dev.lepton.utils.render;

import dev.lepton.event.events.Render3DEvent;
import dev.lepton.utils.render.color.Color;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * World-space drawing helpers.
 *
 * <p>All coordinates passed in are absolute world coordinates -- the camera offset is
 * subtracted here, because the world is drawn with the camera at the origin.
 *
 * <p>Usage inside a {@code @EventHandler} for {@link Render3DEvent}:
 * <pre>
 *   Renderer3D r = Renderer3D.of(event);
 *   r.box(someBox, lineColour, fillColour, ShapeMode.Both);
 *   r.flush();
 * </pre>
 */
public class Renderer3D {
    private static final Renderer3D INSTANCE = new Renderer3D();

    private static final float DEFAULT_LINE_WIDTH = 2.0f;

    private MatrixStack matrices;
    private VertexConsumerProvider.Immediate vertexConsumers;
    private double camX, camY, camZ;
    private float lineWidth = DEFAULT_LINE_WIDTH;

    public static Renderer3D of(Render3DEvent event) {
        INSTANCE.matrices = event.matrices;
        INSTANCE.vertexConsumers = event.vertexConsumers;

        Vec3d cam = event.cameraPos;
        INSTANCE.camX = cam.x;
        INSTANCE.camY = cam.y;
        INSTANCE.camZ = cam.z;
        INSTANCE.lineWidth = DEFAULT_LINE_WIDTH;

        return INSTANCE;
    }

    /** Width of subsequent lines, in pixels. Reset to the default each frame. */
    public Renderer3D lineWidth(float width) {
        this.lineWidth = width;
        return this;
    }

    // -- primitives -----------------------------------------------------------

    public void line(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayers.lines());
        MatrixStack.Entry entry = matrices.peek();

        float ax = (float) (x1 - camX), ay = (float) (y1 - camY), az = (float) (z1 - camZ);
        float bx = (float) (x2 - camX), by = (float) (y2 - camY), bz = (float) (z2 - camZ);

        float nx = bx - ax, ny = by - ay, nz = bz - az;
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);

        if (length < 1.0e-6f) return;

        nx /= length;
        ny /= length;
        nz /= length;

        buffer.vertex(entry, ax, ay, az).color(color.r, color.g, color.b, color.a).normal(entry, nx, ny, nz).lineWidth(lineWidth);
        buffer.vertex(entry, bx, by, bz).color(color.r, color.g, color.b, color.a).normal(entry, nx, ny, nz).lineWidth(lineWidth);
    }

    /** The twelve edges of a box. */
    public void boxLines(Box box, Color color) {
        double x1 = box.minX, y1 = box.minY, z1 = box.minZ;
        double x2 = box.maxX, y2 = box.maxY, z2 = box.maxZ;

        // bottom face
        line(x1, y1, z1, x2, y1, z1, color);
        line(x2, y1, z1, x2, y1, z2, color);
        line(x2, y1, z2, x1, y1, z2, color);
        line(x1, y1, z2, x1, y1, z1, color);

        // top face
        line(x1, y2, z1, x2, y2, z1, color);
        line(x2, y2, z1, x2, y2, z2, color);
        line(x2, y2, z2, x1, y2, z2, color);
        line(x1, y2, z2, x1, y2, z1, color);

        // uprights
        line(x1, y1, z1, x1, y2, z1, color);
        line(x2, y1, z1, x2, y2, z1, color);
        line(x2, y1, z2, x2, y2, z2, color);
        line(x1, y1, z2, x1, y2, z2, color);
    }

    /** The six faces of a box, as translucent quads. */
    public void boxSides(Box box, Color color) {
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayers.debugQuads());
        MatrixStack.Entry entry = matrices.peek();

        float x1 = (float) (box.minX - camX), y1 = (float) (box.minY - camY), z1 = (float) (box.minZ - camZ);
        float x2 = (float) (box.maxX - camX), y2 = (float) (box.maxY - camY), z2 = (float) (box.maxZ - camZ);

        // down
        quad(buffer, entry, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, color);
        // up
        quad(buffer, entry, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, color);
        // north
        quad(buffer, entry, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, color);
        // south
        quad(buffer, entry, x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2, color);
        // west
        quad(buffer, entry, x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1, color);
        // east
        quad(buffer, entry, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, color);
    }

    private void quad(VertexConsumer buffer, MatrixStack.Entry entry,
                      float x1, float y1, float z1, float x2, float y2, float z2,
                      float x3, float y3, float z3, float x4, float y4, float z4, Color color) {
        buffer.vertex(entry, x1, y1, z1).color(color.r, color.g, color.b, color.a);
        buffer.vertex(entry, x2, y2, z2).color(color.r, color.g, color.b, color.a);
        buffer.vertex(entry, x3, y3, z3).color(color.r, color.g, color.b, color.a);
        buffer.vertex(entry, x4, y4, z4).color(color.r, color.g, color.b, color.a);
    }

    // -- convenience ----------------------------------------------------------

    public void box(Box box, Color lineColor, Color sideColor, ShapeMode mode) {
        if (mode.sides()) boxSides(box, sideColor);
        if (mode.lines()) boxLines(box, lineColor);
    }

    public void blockBox(BlockPos pos, Color lineColor, Color sideColor, ShapeMode mode) {
        box(new Box(pos), lineColor, sideColor, mode);
    }

    /** A line from just below the camera to a world position -- a tracer. */
    public void tracer(Vec3d target, Color color) {
        Vec3d start = new Vec3d(camX, camY, camZ);
        line(start.x, start.y, start.z, target.x, target.y, target.z, color);
    }

    /** Pushes the queued geometry to the GPU. Call once at the end of a render handler. */
    public void flush() {
        vertexConsumers.draw(RenderLayers.debugQuads());
        vertexConsumers.draw(RenderLayers.lines());
    }
}
