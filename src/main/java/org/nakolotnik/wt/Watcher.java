//
// TODO: попасть в Storytelling team
//

package org.nakolotnik.wt;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.nakolotnik.wt.client.entity.ModEntityRenderers;
import org.nakolotnik.wt.entity.WathcerMob;
import org.nakolotnik.wt.init.ModPackets;
import org.nakolotnik.wt.init.ModSounds;
import org.spongepowered.asm.mixin.Mixins;
import org.zeith.hammerlib.core.adapter.LanguageAdapter;

@Mod(Watcher.MOD_ID)
public class Watcher {

	public static final String MOD_ID = "wt";

	public Watcher() {
		MinecraftForge.EVENT_BUS.register(this);
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

		LanguageAdapter.registerMod(MOD_ID);
		ModSounds.SOUND_EVENTS.register(bus);

		bus.addListener(WathcerMob::entityAttributes);
		bus.addListener(this::clientSetup);
		bus.addListener(this::commonSetup);

		ModPackets.register();

		Mixins.addConfiguration("mixins.wt.json");
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		ModEntityRenderers.registerRenderers();
	}

	private void commonSetup(final FMLCommonSetupEvent event) {

	}
}
