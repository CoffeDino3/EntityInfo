package net.coffedino.entityinfo.client;

import net.coffedino.entityinfo.network.EntityEffectsPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class ClientEffectCache {

    private static final Map<Integer, List<MobEffectInstance>> CACHE = new HashMap<>();

    private ClientEffectCache() {}

    public static void update(int entityId, List<EntityEffectsPayload.Entry> entries) {
        net.coffedino.entityinfo.EntityInfo.LOGGER.info(
                "[EntityInfo] ClientEffectCache.update entityId={} entries={}", entityId, entries);

        if (entries.isEmpty()) {
            CACHE.remove(entityId);
            return;
        }

        List<MobEffectInstance> instances = entries.stream()
                .map(ClientEffectCache::toInstance)
                .filter(java.util.Objects::nonNull)
                .toList();

        if (instances.isEmpty()) {
            CACHE.remove(entityId);
        } else {
            CACHE.put(entityId, instances);
        }

        net.coffedino.entityinfo.EntityInfo.LOGGER.info(
                "[EntityInfo] ClientEffectCache now has {} entries for id={}",
                CACHE.getOrDefault(entityId, List.of()).size(), entityId);
    }

    @Nullable
    private static MobEffectInstance toInstance(EntityEffectsPayload.Entry entry) {
        Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.getHolder(entry.effectId()).orElse(null);
        if (holder == null) return null;
        return new MobEffectInstance(holder, entry.duration(), entry.amplifier(), entry.ambient(), true);
    }

    public static List<MobEffectInstance> getEffects(LivingEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (entity == mc.player) {
            return entity.getActiveEffects().stream().toList();
        }
        return CACHE.getOrDefault(entity.getId(), Collections.emptyList());
    }

    public static void remove(int entityId) {
        CACHE.remove(entityId);
    }

    public static void clear() {
        CACHE.clear();
    }
}