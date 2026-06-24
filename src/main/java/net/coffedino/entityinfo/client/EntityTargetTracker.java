package net.coffedino.entityinfo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.coffedino.entityinfo.Config;
import net.coffedino.entityinfo.EntityInfo;

import javax.annotation.Nullable;
import java.util.function.Predicate;

@EventBusSubscriber(modid = EntityInfo.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class EntityTargetTracker {

    @Nullable
    private static LivingEntity targetedEntity;

    @Nullable
    public static LivingEntity getTargetedEntity() {
        return targetedEntity;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        targetedEntity = computeTarget();
    }

    @Nullable
    private static LivingEntity computeTarget() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return null;
        }

        Entity camera = mc.getCameraEntity() != null ? mc.getCameraEntity() : mc.player;
        double range = Config.TARGET_RANGE.get();

        Vec3 eyePos = camera.getEyePosition(1.0F);
        Vec3 viewVec = camera.getViewVector(1.0F);
        Vec3 reach = eyePos.add(viewVec.x * range, viewVec.y * range, viewVec.z * range);

        AABB searchBox = camera.getBoundingBox().expandTowards(viewVec.scale(range)).inflate(1.0D, 1.0D, 1.0D);

        Predicate<Entity> predicate = e -> e instanceof LivingEntity
                && !e.isSpectator()
                && e.isPickable()
                && (Config.SHOW_FOR_PLAYERS.get() || !(e instanceof net.minecraft.world.entity.player.Player));

        EntityHitResult hitResult = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                camera.level(),
                camera,
                eyePos,
                reach,
                searchBox,
                predicate,
                0.0F
        );

        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
            return null;
        }

        Entity entity = hitResult.getEntity();
        if (entity instanceof LivingEntity living && living.isAlive()) {
            return living;
        }
        return null;
    }
}