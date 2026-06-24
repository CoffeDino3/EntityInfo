package net.coffedino.entityinfo.network;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.coffedino.entityinfo.EntityInfo;

import java.util.List;
public record EntityEffectsPayload(int entityId, List<Entry> effects) implements CustomPacketPayload {
    public record Entry(ResourceLocation effectId, int amplifier, int duration, boolean ambient) {

        public static Entry fromInstance(MobEffectInstance inst) {
            ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(inst.getEffect().value());
            return new Entry(id, inst.getAmplifier(), inst.getDuration(), inst.isAmbient());
        }

        private static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, Entry::effectId,
                ByteBufCodecs.VAR_INT, Entry::amplifier,
                ByteBufCodecs.VAR_INT, Entry::duration,
                ByteBufCodecs.BOOL, Entry::ambient,
                Entry::new
        );
    }

    public static final Type<EntityEffectsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EntityInfo.MODID, "entity_effects"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityEffectsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, EntityEffectsPayload::entityId,
            Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), EntityEffectsPayload::effects,
            EntityEffectsPayload::new
    );

    public static EntityEffectsPayload of(LivingEntity entity) {
        List<Entry> entries = entity.getActiveEffects().stream()
                .map(Entry::fromInstance)
                .toList();
        return new EntityEffectsPayload(entity.getId(), entries);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}