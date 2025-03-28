package org.nakolotnik.wt.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.nakolotnik.wt.Watcher;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public interface ModSounds {
    @RegistryName("whispering")
    SoundEvent WATCHER_DESPAWN_SOUND  = SoundEvent.createVariableRangeEvent(new ResourceLocation(Watcher.MOD_ID, "whispering"));

    @RegistryName("four_whispering")
    SoundEvent WHISP_AMBIENT  = SoundEvent.createVariableRangeEvent(new ResourceLocation(Watcher.MOD_ID, "four_whispering"));
}
