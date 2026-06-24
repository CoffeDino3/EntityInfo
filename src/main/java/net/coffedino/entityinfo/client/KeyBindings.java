package net.coffedino.entityinfo.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.coffedino.entityinfo.EntityInfo;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = EntityInfo.MODID, value = Dist.CLIENT)
public class KeyBindings {

    public static final Lazy<KeyMapping> TOGGLE_HUD = Lazy.of(() ->
            new KeyMapping("key.entityinfo.toggle",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_H,
                    "key.category.entityinfo")
    );

    public static final Lazy<KeyMapping> OPEN_SETTINGS = Lazy.of(() ->
            new KeyMapping("key.entityinfo.settings",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_APOSTROPHE,
                    "key.category.entityinfo")
    );

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (TOGGLE_HUD.get().consumeClick()) {
            HUDSettings.toggleVisible();
            HUDSettings.save();
        }
        if (OPEN_SETTINGS.get().consumeClick()) {
            Minecraft.getInstance().setScreen(new HudSettingsScreen());
        }
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_HUD.get());
        event.register(OPEN_SETTINGS.get());
    }
}