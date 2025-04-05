package org.nakolotnik.wt.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nakolotnik.wt.Watcher;
import org.nakolotnik.wt.client.gui.ChronoTerminalScreen;

@Mod.EventBusSubscriber(modid = Watcher.MOD_ID, value = Dist.CLIENT)
public class ClientKeyHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (Keybinds.OPEN_TERMINAL.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                mc.setScreen(new ChronoTerminalScreen());
            }
        }
    }
}
