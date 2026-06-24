package net.coffedino.entityinfo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.coffedino.entityinfo.Config;
import net.coffedino.entityinfo.EntityInfo;

import java.util.List;

@EventBusSubscriber(modid = EntityInfo.MODID, value = Dist.CLIENT)
public class EntityHealthHudOverlay {

    private static final int MARGIN = 6;
    private static final int GAP = 4;

    private static final int INFO_WIDTH = 96;
    private static final int NAME_HEIGHT = 14;
    private static final int HEALTH_HEIGHT = 13;
    private static final int EFFECTS_HEIGHT = 15;
    private static final int ROW_GAP = 2;
    private static final int NAME_HEALTH_GAP = 3;
    private static final int MODEL_SIZE = 50;
    private static final int PANEL_BORDER = 1;

    public static final int LOGICAL_TOTAL_WIDTH = INFO_WIDTH + GAP + MODEL_SIZE;
    public static final int LOGICAL_TOTAL_HEIGHT = MODEL_SIZE;

    private static final int COL_NAME_BG = 0xE60A0A0A;
    private static final int COL_HEALTH_BG = 0xE60A0A0A;
    private static final int COL_EFFECTS_BG = 0xE60A0A0A;
    private static final int COL_MODEL_BG = 0xF0000000;

    private static final int COL_BORDER = 0xFFFFFFFF;

    private static final int COL_NAME_TEXT = 0xFFFFFFFF;
    private static final int COL_HEALTH_TEXT = 0xFFFFFFFF;

    private static final int COL_HEALTH_FULL = 0xFF3DCC6A;
    private static final int COL_HEALTH_MID = 0xFFE8A020;
    private static final int COL_HEALTH_LOW = 0xFFCC2222;

    private static int lastNameWidth = -1;
    private static long marqueeStartTime = 0L;
    private static String lastEntityName = "";

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        if (!HUDSettings.isInitialised()) return;
        if (!HUDSettings.isVisible()) return;

        LivingEntity target = EntityTargetTracker.getTargetedEntity();
        if (target == null) {
            lastEntityName = "";
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        renderHud(graphics, mc, target, graphics.guiWidth(), graphics.guiHeight());
    }

    public static void renderPreview(GuiGraphics graphics, Minecraft mc, int screenW, int screenH) {
        LivingEntity target = EntityTargetTracker.getTargetedEntity();
        if (target != null) {
            renderHud(graphics, mc, target, screenW, screenH);
        } else {
            renderEmptyPreview(graphics, mc, screenW, screenH);
        }
    }

    private static void renderHud(GuiGraphics graphics, Minecraft mc,
                                  LivingEntity target, int screenW, int screenH) {
        double scale = HUDSettings.getScale();
        boolean mirrored = HUDSettings.isMirrored();

        List<MobEffectInstance> effects = Config.SHOW_EFFECTS.get()
                ? ClientEffectCache.getEffects(target) : List.of();

        int logicalTotalW = LOGICAL_TOTAL_WIDTH;
        int logicalTotalH = getLogicalTotalHeight(effects.size(), mirrored);
        int scaledTotalW = (int) (logicalTotalW * scale) + MARGIN * 2;
        int scaledTotalH = (int) (logicalTotalH * scale) + MARGIN * 2;
        int anchorScreenX = HUDSettings.resolvedX(screenW, scaledTotalW) + MARGIN;
        int anchorScreenY = HUDSettings.resolvedY(screenH, scaledTotalH) + MARGIN;

        int infoX, modelX;
        if (!mirrored) { infoX = 0; modelX = INFO_WIDTH + GAP; }
        else { modelX = 0; infoX = MODEL_SIZE + GAP; }

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(anchorScreenX, anchorScreenY, 0);
        pose.scale((float) scale, (float) scale, 1.0f);

        renderInfoColumn(graphics, mc, target, infoX, 0);
        drawPanel(graphics, modelX, 0, MODEL_SIZE, MODEL_SIZE, COL_MODEL_BG);

        if (!effects.isEmpty()) {
            renderEffectIconsLayoutAware(graphics, mc, effects, infoX, modelX, mirrored);
        }

        pose.popPose();

        int inner = MODEL_SIZE - PANEL_BORDER * 2;
        int innerX = modelX + PANEL_BORDER;
        int innerY = PANEL_BORDER;
        int screenX1 = (int) Math.round(anchorScreenX + innerX * scale);
        int screenY1 = (int) Math.round(anchorScreenY + innerY * scale);
        int screenX2 = (int) Math.round(anchorScreenX + (innerX + inner) * scale);
        int screenY2 = (int) Math.round(anchorScreenY + (innerY + inner) * scale);

        graphics.enableScissor(screenX1, screenY1, screenX2, screenY2);
        renderEntityModel(graphics, mc, target, screenX1, screenY1, screenX2, screenY2);
        drawVignette(graphics, screenX1, screenY1, screenX2 - screenX1, screenY2 - screenY1);
        graphics.disableScissor();
    }

    private static void renderEmptyPreview(GuiGraphics graphics, Minecraft mc, int screenW, int screenH) {
        double scale = HUDSettings.getScale();
        boolean mirrored = HUDSettings.isMirrored();

        int scaledTotalW = (int) (LOGICAL_TOTAL_WIDTH * scale) + MARGIN * 2;
        int scaledTotalH = (int) (LOGICAL_TOTAL_HEIGHT * scale) + MARGIN * 2;
        int anchorScreenX = HUDSettings.resolvedX(screenW, scaledTotalW) + MARGIN;
        int anchorScreenY = HUDSettings.resolvedY(screenH, scaledTotalH) + MARGIN;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(anchorScreenX, anchorScreenY, 0);
        pose.scale((float) scale, (float) scale, 1.0f);

        int infoX, modelX;
        if (!mirrored) {
            infoX = 0;
            modelX = INFO_WIDTH + GAP;
        } else {
            modelX = 0;
            infoX = MODEL_SIZE + GAP;
        }

        int nameY = 0;
        int healthY = nameY + NAME_HEIGHT + NAME_HEALTH_GAP;
        int textY = healthY + HEALTH_HEIGHT + ROW_GAP;

        drawPanel(graphics, infoX, nameY, INFO_WIDTH, NAME_HEIGHT, COL_NAME_BG);
        drawPanel(graphics, infoX, healthY, INFO_WIDTH, HEALTH_HEIGHT, COL_HEALTH_BG);
        drawPanel(graphics, infoX, textY, INFO_WIDTH, EFFECTS_HEIGHT, COL_EFFECTS_BG);
        drawPanel(graphics, modelX, 0, MODEL_SIZE, MODEL_SIZE, COL_MODEL_BG);

        pose.popPose();
    }

    private static void renderInfoColumn(GuiGraphics g, Minecraft mc,
                                         LivingEntity target, int x, int y) {
        int nameY = y;
        int healthY = nameY + NAME_HEIGHT + NAME_HEALTH_GAP;

        drawPanel(g, x, nameY, INFO_WIDTH, NAME_HEIGHT, COL_NAME_BG);
        renderNameMarquee(g, mc, target, x + PANEL_BORDER + 2, nameY + PANEL_BORDER,
                INFO_WIDTH - (PANEL_BORDER + 2) * 2, NAME_HEIGHT - PANEL_BORDER * 2);

        drawPanel(g, x, healthY, INFO_WIDTH, HEALTH_HEIGHT, COL_HEALTH_BG);
        renderHealthBar(g, mc, target, x + PANEL_BORDER, healthY + PANEL_BORDER,
                INFO_WIDTH - PANEL_BORDER * 2, HEALTH_HEIGHT - PANEL_BORDER * 2);
    }

    private static void renderNameMarquee(GuiGraphics g, Minecraft mc, LivingEntity target,
                                          int x, int y, int width, int height) {
        Component nameComp = target.getDisplayName();
        String name = nameComp.getString();
        int textWidth = mc.font.width(nameComp);

        if (!name.equals(lastEntityName)) {
            lastEntityName = name;
            lastNameWidth = textWidth;
            marqueeStartTime = System.currentTimeMillis();
        }

        int textY = y + (height - mc.font.lineHeight) / 2 + 1;

        if (textWidth <= width) {
            int textX = x + (width - textWidth) / 2;
            g.drawString(mc.font, nameComp, textX + 1, textY + 1, 0x77000000, false);
            g.drawString(mc.font, nameComp, textX, textY, COL_NAME_TEXT, false);
            return;
        }

        int overflow = textWidth - width;
        long elapsed = System.currentTimeMillis() - marqueeStartTime;
        long pause = 900L;
        long pxPerSec = 28L;
        long travel = (overflow * 1000L) / pxPerSec;
        long cycle = pause + travel + pause + travel;
        long t = elapsed % Math.max(cycle, 1);
        int offset;
        if (t < pause) offset = 0;
        else if (t < pause + travel) offset = (int) (((t - pause) * pxPerSec) / 1000L);
        else if (t < pause + travel + pause) offset = overflow;
        else {
            long bt = t - pause - travel - pause;
            offset = (int) (overflow - ((bt * pxPerSec) / 1000L));
        }
        g.enableScissor(x, y, x + width, y + height);
        g.drawString(mc.font, nameComp, x - offset, textY, COL_NAME_TEXT, false);
        g.disableScissor();
    }

    private static void renderHealthBar(GuiGraphics g, Minecraft mc, LivingEntity target,
                                        int x, int y, int width, int height) {
        float health = Math.max(target.getHealth(), 0F);
        float maxHealth = Math.max(target.getMaxHealth(), 1F);
        float pct = Math.min(health / maxHealth, 1F);
        int filled = Math.round(width * pct);

        if (filled > 0) {
            int colour = lerpHealthColour(pct);
            g.fill(x, y, x + filled, y + height, colour);
            g.fill(x, y, x + filled, y + 1, 0x33FFFFFF);
        }

        String txt = formatHealth(health) + "/" + formatHealth(maxHealth);
        int tw = mc.font.width(txt);
        int tx = x + (width - tw) / 2;
        int ty = y + (height - mc.font.lineHeight) / 2;
        g.drawString(mc.font, txt, tx + 1, ty + 1, 0x88000000, false);
        g.drawString(mc.font, txt, tx, ty, COL_HEALTH_TEXT, false);
    }

    private static int lerpHealthColour(float pct) {
        if (pct >= 0.5f) {
            float t = (pct - 0.5f) * 2f;
            return lerpColour(COL_HEALTH_MID, COL_HEALTH_FULL, t);
        } else {
            float t = pct * 2f;
            return lerpColour(COL_HEALTH_LOW, COL_HEALTH_MID, t);
        }
    }

    private static int lerpColour(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF, aa = (a >> 24) & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF, ba = (b >> 24) & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int gr = (int) (ag + (bg - ag) * t);
        int bl2 = (int) (ab + (bb - ab) * t);
        int al = (int) (aa + (ba - aa) * t);
        return (al << 24) | (r << 16) | (gr << 8) | bl2;
    }

    private static String formatHealth(float v) {
        if (Math.abs(v - Math.round(v)) < 0.05F) return String.valueOf(Math.round(v));
        return String.format("%.1f", v);
    }

    private static void renderEffectIconsLayoutAware(GuiGraphics g, Minecraft mc,
                                                     List<MobEffectInstance> effects,
                                                     int infoX, int modelX, boolean mirrored) {
        int squareSize = EFFECTS_HEIGHT;
        int innerIcon = squareSize - PANEL_BORDER * 2;
        int spacing = 3;

        int firstRowW = INFO_WIDTH;
        int firstRowCap = Math.max(1, firstRowW / (squareSize + spacing));
        int overflowW = LOGICAL_TOTAL_WIDTH;
        int perOverflowRow = Math.max(1, overflowW / (squareSize + spacing));

        int i = 0;
        for (MobEffectInstance inst : effects) {
            int dx, dy;

            if (i < firstRowCap) {
                dy = NAME_HEIGHT + NAME_HEALTH_GAP + HEALTH_HEIGHT + ROW_GAP;
                if (!mirrored) {
                    int rightEdge = infoX + INFO_WIDTH;
                    dx = rightEdge - (i + 1) * (squareSize + spacing) + spacing;
                } else {
                    dx = infoX + i * (squareSize + spacing);
                }
            } else {
                int oi = i - firstRowCap;
                int col = oi % perOverflowRow;
                int row = oi / perOverflowRow;
                dy = MODEL_SIZE + ROW_GAP + row * (squareSize + ROW_GAP);
                if (!mirrored) {
                    dx = LOGICAL_TOTAL_WIDTH - (col + 1) * (squareSize + spacing) + spacing;
                } else {
                    dx = col * (squareSize + spacing);
                }
            }

            drawPanel(g, dx + PANEL_BORDER, dy + PANEL_BORDER, innerIcon, innerIcon, COL_EFFECTS_BG);
            TextureAtlasSprite sprite = mc.getMobEffectTextures().get(inst.getEffect());
            if (sprite != null) {
                g.blit(dx + PANEL_BORDER, dy + PANEL_BORDER, 0, innerIcon, innerIcon, sprite);
            }

            if (inst.getAmplifier() > 0) {
                String numeral = toRoman(inst.getAmplifier() + 1);
                int textW = mc.font.width(numeral);
                int textX = dx + PANEL_BORDER + (innerIcon - textW) / 2;
                int borderBottomY = dy + PANEL_BORDER + innerIcon;
                int textY = borderBottomY - mc.font.lineHeight / 2;
                g.drawString(mc.font, numeral, textX + 1, textY + 1, 0xFF000000, false);
                g.drawString(mc.font, numeral, textX, textY, 0xFFFFFFFF, false);
            }

            i++;
        }
    }

    private static String toRoman(int n) {
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        if (n <= 0 || n > 39) return String.valueOf(n);
        return thousands[n / 1000] + hundreds[(n % 1000) / 100] + tens[(n % 100) / 10] + ones[n % 10];
    }

    private static void renderEntityModel(GuiGraphics g, Minecraft mc, LivingEntity target,
                                          int x1, int y1, int x2, int y2) {
        int boxSize = Math.min(x2 - x1, y2 - y1);
        int entityScale = (int) computeScaleForEntity(target, boxSize);

        float fakeMouseX = x1 + (x2 - x1) * 0.25F;
        float fakeMouseY = (y1 + y2) / 2.0F;

        InventoryScreen.renderEntityInInventoryFollowsMouse(
                g,
                x1, y1, x2, y2,
                entityScale,
                0.0625F,
                fakeMouseX,
                fakeMouseY,
                target
        );
    }

    private static void renderModelBox(GuiGraphics g, Minecraft mc, LivingEntity target, int x, int y,
                                       int anchorScreenX, int anchorScreenY, double hudScale, boolean mirrored) {
        drawPanel(g, x, y, MODEL_SIZE, MODEL_SIZE, COL_MODEL_BG);

        int inner = MODEL_SIZE - PANEL_BORDER * 2;
        int innerX = x + PANEL_BORDER;
        int innerY = y + PANEL_BORDER;

        g.enableScissor(innerX, innerY, innerX + inner, innerY + inner);
        if (target != null) {
            int screenX1 = (int) Math.round(anchorScreenX + innerX * hudScale);
            int screenY1 = (int) Math.round(anchorScreenY + innerY * hudScale);
            int screenX2 = (int) Math.round(anchorScreenX + (innerX + inner) * hudScale);
            int screenY2 = (int) Math.round(anchorScreenY + (innerY + inner) * hudScale);
            renderEntityModel(g, mc, target, screenX1, screenY1, screenX2, screenY2);
        }

        drawVignette(g, innerX, innerY, inner, inner);

        g.disableScissor();
    }

    private static void drawVignette(GuiGraphics g, int x, int y, int w, int h) {
        int shade = 0x66000000;
        int edge = 5;
        g.fill(x, y, x + edge, y + h, shade);
        g.fill(x + w - edge, y, x + w, y + h, shade);
        g.fill(x, y, x + w, y + edge, shade);
        g.fill(x, y + h - edge, x + w, y + h, shade);
    }

    private static float computeScaleForEntity(LivingEntity t, float boxSize) {
        var bb = t.getBoundingBox();
        float bbH = (float) bb.getYsize();
        float bbW = (float) Math.max(bb.getXsize(), bb.getZsize());
        float dim = Math.max(bbH, bbW);
        if (dim <= 0.01F) dim = 1F;

        boolean isBoss = (t instanceof EnderDragon || t instanceof WitherBoss);
        float fitFraction = isBoss ? 0.95F : 0.6F;

        float s = (boxSize * fitFraction) / dim;

        float maxScale = boxSize * 0.6F;
        s = Math.min(s, maxScale);

        float minScale = boxSize * 0.05F;
        return Math.max(s, minScale);
    }

    private static void drawPanel(GuiGraphics g, int x, int y, int w, int h, int fill) {
        int b = PANEL_BORDER;

        g.fill(x - b, y - b, x + w + b, y + h + b, COL_BORDER);
        g.fill(x, y, x + w, y + h, fill);
    }

    public static int getLogicalTotalHeight(int effectCount, boolean mirrored) {
        if (effectCount == 0) return MODEL_SIZE;

        int squareSize = EFFECTS_HEIGHT;
        int spacing = 3;
        int firstRowCap = Math.max(1, INFO_WIDTH / (squareSize + spacing));

        if (effectCount <= firstRowCap) return MODEL_SIZE;

        int overflow = effectCount - firstRowCap;
        int perOverflowRow = Math.max(1, LOGICAL_TOTAL_WIDTH / (squareSize + spacing));
        int overflowRows = (int) Math.ceil((double) overflow / perOverflowRow);
        return MODEL_SIZE + ROW_GAP + overflowRows * (squareSize + ROW_GAP);
    }

    private static int getEffectsPerRow() {
        int squareSize = EFFECTS_HEIGHT;
        int spacing = 3;
        return (LOGICAL_TOTAL_WIDTH) / (squareSize + spacing);
    }
}