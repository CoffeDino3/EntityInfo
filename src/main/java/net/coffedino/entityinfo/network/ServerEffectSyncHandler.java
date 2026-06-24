package net.coffedino.entityinfo.network;

import net.coffedino.entityinfo.EntityInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = EntityInfo.MODID)
public class ServerEffectSyncHandler {

    private static final int CHECK_INTERVAL = 10;
    private static final Map<Integer, String> LAST_SIGNATURE = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        long tick = event.getServer().getTickCount();

        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (Entity raw : level.getAllEntities()) {
                if (!(raw instanceof LivingEntity entity)) continue;
                if ((entity.getId() + tick) % CHECK_INTERVAL != 0) continue;

                List<MobEffectInstance> active = entity.getActiveEffects().stream().toList();
                String signature = buildSignature(active);
                String last = LAST_SIGNATURE.get(entity.getId());

                if (signature.equals(last)) continue;
                if (active.isEmpty()) {
                    LAST_SIGNATURE.remove(entity.getId());
                } else {
                    LAST_SIGNATURE.put(entity.getId(), signature);
                }
                if (active.isEmpty() && last == null) continue;

                PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                        entity, EntityEffectsPayload.of(entity));
            }
        }
    }
    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        LAST_SIGNATURE.remove(event.getEntity().getId());
    }
    private static String buildSignature(List<MobEffectInstance> effects) {
        if (effects.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (MobEffectInstance inst : effects) {
            sb.append(inst.getEffect().value().getDescriptionId())
                    .append(':').append(inst.getAmplifier())
                    .append(':').append(inst.isAmbient())
                    .append(';');
        }
        return sb.toString();
    }
}