package net.coffedino.entityinfo.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HudScaleScreen extends Screen {

    private final Screen parent;
    private boolean draggingHandle   = false;
    private boolean draggingHandleTL = false;
    private int hudScreenX, hudScreenY, hudScreenW, hudScreenH;
    private static final int HANDLE = 8;

    public HudScaleScreen(Screen parent) {
        super(Component.literal("Drag Corner to Resize"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    private int[] getHudRect() {
        double scale = HUDSettings.getScale();
        int totalW = (int)(EntityHealthHudOverlay.LOGICAL_TOTAL_WIDTH * scale) + 12;
        int totalH = (int)(EntityHealthHudOverlay.LOGICAL_TOTAL_HEIGHT * scale) + 12;
        int hx = HUDSettings.resolvedX(width, totalW);
        int hy = HUDSettings.resolvedY(height, totalH);
        return new int[]{hx, hy, totalW, totalH};
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        EntityHealthHudOverlay.renderPreview(g, minecraft, width, height);

        int[] r = getHudRect();
        int hx = r[0], hy = r[1], tw = r[2], th = r[3];

        g.fill(hx, hy, hx + tw, hy + th, 0x66AAAAAA);

        // Bottom-right handle
        int hx2 = hx + tw - HANDLE;
        int hy2 = hy + th - HANDLE;
        g.fill(hx2, hy2, hx2 + HANDLE, hy2 + HANDLE, 0xFFFFDD00);
        g.fill(hx2 + 1, hy2 + 1, hx2 + HANDLE - 1, hy2 + HANDLE - 1, 0xFFFFFF88);

        // Top-left handle
        g.fill(hx, hy, hx + HANDLE, hy + HANDLE, 0xFFFFDD00);
        g.fill(hx + 1, hy + 1, hx + HANDLE - 1, hy + HANDLE - 1, 0xFFFFFF88);

        g.drawCenteredString(font,
                Component.literal("Drag yellow corner to resize — Esc or Done to save"),
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
        int[] r = getHudRect();
        int hx = r[0], hy = r[1], tw = r[2], th = r[3];
        int hx2 = hx + tw - HANDLE;
        int hy2 = hy + th - HANDLE;

        if (mx >= hx2 && mx <= hx2 + HANDLE && my >= hy2 && my <= hy2 + HANDLE) {
            draggingHandle = true;
            hudScreenX = hx;
            hudScreenY = hy;
            return true;
        }
        if (mx >= hx && mx <= hx + HANDLE && my >= hy && my <= hy + HANDLE) {
            draggingHandleTL = true;
            hudScreenX = hx;
            hudScreenY = hy;
            hudScreenW = tw;
            hudScreenH = th;
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingHandle) {
            int desiredW = (int) mx - hudScreenX;
            int desiredH = (int) my - hudScreenY;
            int desired  = Math.max(desiredW, desiredH);
            int logicalW = EntityHealthHudOverlay.LOGICAL_TOTAL_WIDTH + 12;
            HUDSettings.setScale(desired / (double) logicalW);
            return true;
        }
        if (draggingHandleTL) {
            int brX = hudScreenX + hudScreenW;
            int brY = hudScreenY + hudScreenH;
            int desiredW = brX - (int) mx;
            int desiredH = brY - (int) my;
            int desired  = Math.max(desiredW, desiredH);
            int logicalW = EntityHealthHudOverlay.LOGICAL_TOTAL_WIDTH + 12;
            HUDSettings.setScale(desired / (double) logicalW);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingHandle   = false;
        draggingHandleTL = false;
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