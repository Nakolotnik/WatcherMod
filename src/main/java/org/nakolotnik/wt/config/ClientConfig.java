package org.nakolotnik.wt.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ClientConfig {

    public static final ClientConfig INSTANCE;
    public static final ForgeConfigSpec SPEC;

    static {
        Pair<ClientConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ForgeConfigSpec.IntValue riftLayers;
    public final ForgeConfigSpec.DoubleValue riftZOffset;
    public final ForgeConfigSpec.DoubleValue riftMinOpacity;
    public final ForgeConfigSpec.DoubleValue riftMaxOpacity;
    public final ForgeConfigSpec.BooleanValue vanillaOnly;

    private ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Watcher: Client Rendering Config").push("rift");

        riftLayers = builder
                .comment("How many visual layers the rift should have")
                .defineInRange("riftLayers", 4, 1, 32);

        riftZOffset = builder
                .comment("Z-fighting offset per layer (small positive number)")
                .defineInRange("riftZOffset", 0.0015, 0.0, 0.1);

        riftMinOpacity = builder
                .comment("Minimum opacity (alpha) of inner rift layers")
                .defineInRange("riftMinOpacity", 0.6, 0.0, 1.0);

        riftMaxOpacity = builder
                .comment("Maximum opacity (alpha) of outer rift layers")
                .defineInRange("riftMaxOpacity", 1.0, 0.0, 1.0);

        vanillaOnly = builder
                .comment("Force fallback to vanilla rendering if shaders aren't working")
                .define("vanillaOnly", false);

        builder.pop();
    }
}
