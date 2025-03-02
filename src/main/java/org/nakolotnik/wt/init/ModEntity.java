package org.nakolotnik.wt.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.nakolotnik.wt.entity.WathcerMob;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public interface ModEntity {

    @RegistryName("watcher")
    EntityType<WathcerMob> WATCHER = EntityType.Builder.of(WathcerMob::new, MobCategory.MONSTER)
            .sized(0.6F, 1.8F)
            .build("watcher");
}
