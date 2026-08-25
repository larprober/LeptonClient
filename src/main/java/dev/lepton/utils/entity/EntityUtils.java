package dev.lepton.utils.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class EntityUtils {
    public enum Group {
        Player,
        Hostile,
        Passive,
        Item,
        Projectile,
        Other
    }

    public static Group groupOf(Entity entity) {
        if (entity instanceof PlayerEntity) return Group.Player;
        if (entity instanceof ItemEntity) return Group.Item;
        if (entity instanceof ProjectileEntity) return Group.Projectile;

        if (entity instanceof Monster) return Group.Hostile;
        if (entity.getType().getSpawnGroup() == SpawnGroup.MONSTER) return Group.Hostile;

        if (entity instanceof LivingEntity) return Group.Passive;

        return Group.Other;
    }

    public static boolean isHostile(Entity entity) {
        return groupOf(entity) == Group.Hostile;
    }

    public static boolean isPassive(Entity entity) {
        return groupOf(entity) == Group.Passive;
    }

    public static boolean isSelf(Entity entity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.player != null && entity == mc.player;
    }

    public static boolean isAttackable(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return false;
        return living.isAlive() && !living.isDead() && !isSelf(entity);
    }

    /**
     * Bounding box interpolated toward the entity's render position, so boxes drawn
     * around moving entities do not lag a tick behind them.
     */
    public static Box interpolatedBox(Entity entity, float tickDelta) {
        Vec3d current = entity.getEntityPos();
        Vec3d previous = new Vec3d(entity.lastX, entity.lastY, entity.lastZ);

        double x = previous.x + (current.x - previous.x) * tickDelta;
        double y = previous.y + (current.y - previous.y) * tickDelta;
        double z = previous.z + (current.z - previous.z) * tickDelta;

        Box box = entity.getBoundingBox();

        return box.offset(x - current.x, y - current.y, z - current.z);
    }

    public static Vec3d interpolatedCenter(Entity entity, float tickDelta) {
        return interpolatedBox(entity, tickDelta).getCenter();
    }
}
