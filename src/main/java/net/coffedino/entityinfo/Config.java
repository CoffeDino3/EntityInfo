package net.coffedino.entityinfo;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {

    public static class Client {
        public final ModConfigSpec.DoubleValue TARGET_RANGE;
        public final ModConfigSpec.BooleanValue SHOW_FOR_PLAYERS;
        public final ModConfigSpec.BooleanValue SHOW_EFFECTS;

        Client(ModConfigSpec.Builder builder) {
            builder.push("general");

            TARGET_RANGE = builder
                    .comment("Maximum range to detect entities")
                    .defineInRange("targetRange", 32.0D, 1.0D, 64.0D);

            SHOW_FOR_PLAYERS = builder
                    .comment("Show HUD for other players")
                    .define("showForPlayers", false);

            SHOW_EFFECTS = builder
                    .comment("Show potion effects on the HUD")
                    .define("showEffects", true);

            builder.pop();
        }
    }

    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    static {
        final Pair<Client, ModConfigSpec> specPair =
                new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = specPair.getLeft();
        CLIENT_SPEC = specPair.getRight();
    }
    public static ModConfigSpec.DoubleValue TARGET_RANGE = CLIENT.TARGET_RANGE;
    public static ModConfigSpec.BooleanValue SHOW_FOR_PLAYERS = CLIENT.SHOW_FOR_PLAYERS;
    public static ModConfigSpec.BooleanValue SHOW_EFFECTS = CLIENT.SHOW_EFFECTS;
}