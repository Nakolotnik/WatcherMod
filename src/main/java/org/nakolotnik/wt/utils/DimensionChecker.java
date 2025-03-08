

package org.nakolotnik.wt.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class DimensionChecker {

    private static final ResourceLocation TIMELESS_WASTELAND = new ResourceLocation("wt:timeless_wasteland");

    public static boolean isInTimelessWasteland(Entity entity) {
        if (entity == null || entity.level() == null) return false;
        return entity.level().dimension().location().equals(TIMELESS_WASTELAND);
    }

}
