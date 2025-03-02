package org.nakolotnik.wt.init;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class WatcherMessageRegistry {
    private static final Map<String, Integer> LOOK_MESSAGES = new HashMap<>();
    private static final Map<UUID, Long> lastMessageTime = new HashMap<>();
    private static final long COOLDOWN_MILLIS = 10 * 60 * 1000;

    static {
        registerMessage("message.watcher.1", 1);
        registerMessage("message.watcher.2", 2);
        registerMessage("message.watcher.3", 3);
        registerMessage("message.watcher.4", 4);
        registerMessage("message.watcher.5", 6);
        registerMessage("message.watcher.6", 8);
        registerMessage("message.watcher.7", 10);
    }

    public static void registerMessage(String key, int rarityIndex) {
        LOOK_MESSAGES.put(key, Math.max(1, Math.min(rarityIndex, 10)));
    }

    public static boolean canSendMessage(ServerPlayer player) {
        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();

        if (!lastMessageTime.containsKey(playerId) || (currentTime - lastMessageTime.get(playerId) > COOLDOWN_MILLIS)) {
            lastMessageTime.put(playerId, currentTime);
            return true;
        }

        return false;
    }

    public static Component getRandomMessage() {
        if (LOOK_MESSAGES.isEmpty()) {
            return Component.translatable("message.watcher.1").withStyle(style -> style.withColor(0xAAAAAA));
        }

        List<String> weightedMessages = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : LOOK_MESSAGES.entrySet()) {
            int weight = 11 - entry.getValue();
            for (int i = 0; i < weight; i++) {
                weightedMessages.add(entry.getKey());
            }
        }

        Random random = new Random();
        String selectedMessage = weightedMessages.get(random.nextInt(weightedMessages.size()));

        return Component.translatable(selectedMessage).withStyle(style -> style.withColor(0xAAAAAA));
    }
    public static Component getWatcherMessage() {
        return Component.empty()
                .append(Component.literal("[")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7F7F7F))))
                .append(Component.translatable("entity.wt.watcher")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7F7F7F))))
                .append(Component.literal("] ")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7F7F7F))))
                .append(Component.literal(": ")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF))))
                .append(Component.translatable("message.watcher.not_your_time")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF))));
    }
}
