package org.nakolotnik.wt.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.nakolotnik.wt.entity.DarkEyesEntity;
import org.nakolotnik.wt.entity.TimeRiftEntity;
import org.nakolotnik.wt.entity.WathcerMob;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public interface ModEntity {
    @RegistryName("watcher")
    EntityType<WathcerMob> WATCHER = EntityType.Builder.of(WathcerMob::new, MobCategory.MONSTER)
            .sized(0.6F, 1.8F)
            .build("watcher");

//    @RegistryName("dark_eye")
//    EntityType<DarkEyesEntity> DARK_EYES = EntityType.Builder.of(DarkEyesEntity::new, MobCategory.MISC)
//            .sized(0.5f, 0.5f)
//            .build("eye");

    @RegistryName("time_rift")
    EntityType<TimeRiftEntity> TIME_RIFT = EntityType.Builder.of(TimeRiftEntity::new, MobCategory.MISC)
            .sized(5.0f, 2.5f)
            .build("time_rift");
}