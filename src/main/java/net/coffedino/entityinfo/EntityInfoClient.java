package net.coffedino.entityinfo;

import net.coffedino.entityinfo.client.EntityHealthHudOverlay;
import net.coffedino.entityinfo.client.EntityTargetTracker;
import net.coffedino.entityinfo.client.HUDSettings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = EntityInfo.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = EntityInfo.MODID, value = Dist.CLIENT)
public class EntityInfoClient {

    public EntityInfoClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        HUDSettings.setInitialised(true);
        HUDSettings.load();
        NeoForge.EVENT_BUS.register(EntityHealthHudOverlay.class);
        NeoForge.EVENT_BUS.register(EntityTargetTracker.class);
    }
}