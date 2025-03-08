package org.nakolotnik.wt.utils;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

public class ModVersionHelper {
    private static final String MOD_ID = "wt";

    public static String getLocalModVersion() {
        return ModList.get().getMods().stream()
                .filter(modInfo -> modInfo.getModId().equals(MOD_ID))
                .findFirst()
                .map(modInfo -> modInfo.getVersion().toString())
                .orElse("Unknown");
    }
}

