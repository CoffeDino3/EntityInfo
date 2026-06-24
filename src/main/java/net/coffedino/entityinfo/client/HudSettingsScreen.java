package net.coffedino.entityinfo.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HudSettingsScreen extends Screen {

    private static final int BTN_W = 160;
    private static final int BTN_H = 20;
    private static final int BTN_GAP = 6;

    public HudSettingsScreen() {
        super(Component.literal("Entity Info Settings"));
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, width, height, 0xC0101010, 0xD0101010);
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int startY  = height / 2 - (3 * (BTN_H + BTN_GAP)) / 2;

        addRenderableWidget(new net.minecraft.client.gui.components.Button.Builder(
                Component.literal("Change Position"),
                btn -> minecraft.setScreen(new HudPositionScreen(this)))
                .bounds(centerX - BTN_W / 2, startY, BTN_W, BTN_H)
                .build());

        addRenderableWidget(new net.minecraft.client.gui.components.Button.Builder(
                Component.literal("Reverse Layout"),
                btn -> {
                    HUDSettings.toggleMirrored();
                    HUDSettings.save();
                })
                .bounds(centerX - BTN_W / 2, startY + BTN_H + BTN_GAP, BTN_W, BTN_H)
                .build());

        addRenderableWidget(new net.minecraft.client.gui.components.Button.Builder(
                Component.literal("Change Size"),
                btn -> minecraft.setScreen(new HudScaleScreen(this)))
                .bounds(centerX - BTN_W / 2, startY + (BTN_H + BTN_GAP) * 2, BTN_W, BTN_H)
                .build());

        addRenderableWidget(new net.minecraft.client.gui.components.Button.Builder(
                Component.literal("Done"),
                btn -> onClose())
                .bounds(centerX - BTN_W / 2, startY + (BTN_H + BTN_GAP) * 3 + 4, BTN_W, BTN_H)
                .build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, title,
                width / 2, height / 2 - (3 * (BTN_H + BTN_GAP)) / 2 - 20, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        HUDSettings.save();
        minecraft.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}