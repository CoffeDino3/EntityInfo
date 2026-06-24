package net.coffedino.entityinfo.network;

import net.coffedino.entityinfo.EntityInfo;
import net.coffedino.entityinfo.client.ClientEffectCache;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = EntityInfo.MODID)
public class ModNetworking {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        EntityInfo.LOGGER.info("[EntityInfo] Registering EntityEffectsPayload network handler");
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                EntityEffectsPayload.TYPE,
                EntityEffectsPayload.STREAM_CODEC,
                ModNetworking::handleOnClient
        );
    }

    private static void handleOnClient(EntityEffectsPayload payload, IPayloadContext context) {
        EntityInfo.LOGGER.info("[EntityInfo] Received EntityEffectsPayload for entityId={} effects={}",
                payload.entityId(), payload.effects());
        context.enqueueWork(() -> ClientEffectCache.update(payload.entityId(), payload.effects()));
    }
}