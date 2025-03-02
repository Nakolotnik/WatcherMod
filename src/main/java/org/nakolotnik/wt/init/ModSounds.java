package org.nakolotnik.wt.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.nakolotnik.wt.Watcher;


public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Watcher.MOD_ID);

    public static final RegistryObject<SoundEvent> WATCHER_DESPAWN_SOUND =
            SOUND_EVENTS.register("whispering",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Watcher.MOD_ID, "whispering")));

    public static final RegistryObject<SoundEvent> WHISP_AMBIENT =
            SOUND_EVENTS.register("four_whispering",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Watcher.MOD_ID, "four_whispering")));
}
