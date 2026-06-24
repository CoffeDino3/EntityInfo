package net.coffedino.entityinfo.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HudPositionScreen extends Screen {

    private final Screen parent;
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    public HudPositionScreen(Screen parent) {
        super(Component.literal("Drag HUD to Reposition"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        EntityHealthHudOverlay.renderPreview(g, minecraft, width, height);

        double scale = HUDSettings.getScale();
        int totalW = (int)(EntityHealthHudOverlay.LOGICAL_TOTAL_WIDTH * scale) + 12;
        int totalH = (int)(EntityHealthHudOverlay.LOGICAL_TOTAL_HEIGHT * scale) + 12;
        int hx = HUDSettings.resolvedX(width, totalW);
        int hy = HUDSettings.resolvedY(height, totalH);

        g.fill(hx, hy, hx + totalW, hy + totalH, 0x66AAAAAA);

        g.drawCenteredString(font,
                Component.literal("Drag the HUD — Esc or Done to save"),
                width / 2, 10, 0xFFFFFFFF);

        g.fill(width / 2 - 40, height - 28, width / 2 + 40, height - 8, 0xFF333333);
        g.fill(width / 2 - 39, height - 27, width / 2 + 39, height - 9, 0xFF555555);
        g.drawCenteredString(font, Component.literal("Done"), width / 2, height - 22, 0xFFFFFFFF);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (mx >= width / 2 - 40 && mx <= width / 2 + 40
                && my >= height - 28 && my <= height - 8) {
            onClose();
            return true;
        }
        double scale = HUDSettings.getScale();
        int totalW = (int)(EntityHealthHudOverlay.LOGICAL_TOTAL_WIDTH * scale) + 12;
        int totalH = (int)(EntityHealthHudOverlay.LOGICAL_TOTAL_HEIGHT * scale) + 12;
        int hx = HUDSettings.resolvedX(width, totalW);
        int hy = HUDSettings.resolvedY(height, totalH);
        if (mx >= hx && mx <= hx + totalW && my >= hy && my <= hy + totalH) {
            dragging = true;
            dragOffsetX = (int) mx - hx;
            dragOffsetY = (int) my - hy;
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging) {
            double scale = HUDSettings.getScale();
            int totalW = (int)(EntityHealthHudOverlay.LOGICAL_TOTAL_WIDTH * scale) + 12;
            int totalH = (int)(EntityHealthHudOverlay.LOGICAL_TOTAL_HEIGHT * scale) + 12;
            int newX = (int) mx - dragOffsetX;
            int newY = (int) my - dragOffsetY;
            newX = Math.max(0, Math.min(width - totalW, newX));
            newY = Math.max(0, Math.min(height - totalH, newY));
            HUDSettings.setAnchorX((int) Math.round(newX * 100.0 / Math.max(1, width - totalW)));
            HUDSettings.setAnchorY((int) Math.round(newY * 100.0 / Math.max(1, height - totalH)));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public void onClose() {
        HUDSettings.save();
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}