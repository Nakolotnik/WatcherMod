package org.nakolotnik.wt.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.nakolotnik.wt.Watcher;

@Mod.EventBusSubscriber(modid = Watcher.MOD_ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Keybinds {
    public static final KeyMapping OPEN_TERMINAL = new KeyMapping(
            "key.wt.open_terminal",
            GLFW.GLFW_KEY_J,
            "key.categories.wt"
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_TERMINAL);
    }
}
