//
// TODO: попасть в Storytelling team
//

package org.nakolotnik.wt;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.nakolotnik.wt.config.ClientConfig;
import org.nakolotnik.wt.init.ModEntityRenderers;
import org.nakolotnik.wt.entity.WathcerMob;
import org.spongepowered.asm.mixin.Mixins;
import org.zeith.hammerlib.core.adapter.LanguageAdapter;

@Mod(Watcher.MOD_ID)
public class Watcher {

	public static final String MOD_ID = "wt";

	public Watcher() {
		MinecraftForge.EVENT_BUS.register(this);
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

		LanguageAdapter.registerMod(MOD_ID);


		bus.addListener(WathcerMob::entityAttributes);
		bus.addListener(this::clientSetup);
		bus.addListener(this::commonSetup);

		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

		Mixins.addConfiguration("mixins.wt.json");
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		ModEntityRenderers.registerRenderers();
	}

	private void commonSetup(final FMLCommonSetupEvent event) {

	}
}
