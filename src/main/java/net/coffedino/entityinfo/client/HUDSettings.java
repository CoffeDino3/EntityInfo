package net.coffedino.entityinfo.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.coffedino.entityinfo.EntityInfo;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Path;

@OnlyIn(Dist.CLIENT)
public class HUDSettings {

    private static boolean visible   = true;
    private static int     anchorX   = 100;
    private static int     anchorY   = 0;
    private static double  scale     = 1.0;
    private static boolean mirrored  = false;
    private static boolean initialised = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FMLPaths.CONFIGDIR.get().resolve("entityinfo_hud.json");

    public static boolean isInitialised() { return initialised; }
    public static void setInitialised(boolean init) { initialised = init; }

    public static boolean isVisible() { return visible; }
    public static void setVisible(boolean vis) { visible = vis; }
    public static void toggleVisible() { visible = !visible; }

    public static int getAnchorX() { return anchorX; }
    public static void setAnchorX(int x) { anchorX = Math.max(0, Math.min(100, x)); }

    public static int getAnchorY() { return anchorY; }
    public static void setAnchorY(int y) { anchorY = Math.max(0, Math.min(100, y)); }

    public static double getScale() { return scale; }
    public static void setScale(double s) { scale = Math.max(0.5, Math.min(2.0, s)); }

    public static boolean isMirrored() { return mirrored; }
    public static void setMirrored(boolean m) { mirrored = m; }
    public static void toggleMirrored() { mirrored = !mirrored; }

    public static int resolvedX(int screenWidth, int hudWidth) {
        return (int) ((anchorX / 100.0) * (screenWidth - hudWidth));
    }

    public static int resolvedY(int screenHeight, int hudHeight) {
        return (int) ((anchorY / 100.0) * (screenHeight - hudHeight));
    }

    public static void save() {
        try (Writer w = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(new Data(visible, anchorX, anchorY, scale, mirrored), w);
        } catch (IOException e) {
            EntityInfo.LOGGER.error("[EntityInfo] Failed to save HUD settings", e);
        }
    }

    public static void load() {
        File f = CONFIG_PATH.toFile();
        if (!f.exists()) return;
        try (Reader r = new FileReader(f)) {
            Data d = GSON.fromJson(r, Data.class);
            if (d != null) {
                visible  = d.visible;
                anchorX  = Math.max(0, Math.min(100, d.anchorX));
                anchorY  = Math.max(0, Math.min(100, d.anchorY));
                scale    = Math.max(0.5, Math.min(2.0, d.scale));
                mirrored = d.mirrored;
            }
        } catch (IOException e) {
            EntityInfo.LOGGER.error("[EntityInfo] Failed to load HUD settings", e);
        }
    }

    private static class Data {
        boolean visible;
        int anchorX, anchorY;
        double scale;
        boolean mirrored;

        Data(boolean visible, int anchorX, int anchorY, double scale, boolean mirrored) {
            this.visible  = visible;
            this.anchorX  = anchorX;
            this.anchorY  = anchorY;
            this.scale    = scale;
            this.mirrored = mirrored;
        }
    }
}