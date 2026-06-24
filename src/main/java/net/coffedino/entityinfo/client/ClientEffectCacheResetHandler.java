package net.coffedino.entityinfo.client;

import net.coffedino.entityinfo.EntityInfo;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = EntityInfo.MODID, value = Dist.CLIENT)
public class ClientEffectCacheResetHandler {

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientEffectCache.clear();
    }
}