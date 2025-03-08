package org.nakolotnik.wt.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import org.nakolotnik.wt.Watcher;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class UpdatorChecker {
    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/project/watcher/version";
    private static String localVersion = "Unknown";
    private static String latestVersion = "Unknown";

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new UpdatorChecker());
        localVersion = getLocalModVersion();
        fetchLatestVersion();
    }

    private static String getLocalModVersion() {
        return ModList.get().getModContainerById(Watcher.MOD_ID)
                .map(modContainer -> modContainer.getModInfo().getVersion().toString())
                .orElse("Unknown");
    }

    private static void fetchLatestVersion() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(MODRINTH_API_URL).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Minecraft-Watcher-Mod");

            if (connection.getResponseCode() == 200) {
                Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8);
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                JsonArray versions = JsonParser.parseString(response).getAsJsonArray();
                if (!versions.isEmpty()) {
                    JsonElement latestVersionElement = versions.get(0).getAsJsonObject().get("version_number");
                    latestVersion = latestVersionElement.getAsString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (isOutdated(localVersion, latestVersion)) {
                player.sendSystemMessage(
                        Component.translatable("message.watcher.update_available")
                                .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000)))
                );
            }
        }
    }

    private static boolean isOutdated(String local, String latest) {
        try {
            int[] localParts = parseVersion(local);
            int[] latestParts = parseVersion(latest);

            for (int i = 0; i < 3; i++) {
                if (localParts[i] < latestParts[i]) return true;
                if (localParts[i] > latestParts[i]) return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static int[] parseVersion(String version) {
        String[] parts = version.split("\\.");
        return new int[]{
                parts.length > 0 ? Integer.parseInt(parts[0]) : 0,
                parts.length > 1 ? Integer.parseInt(parts[1]) : 0,
                parts.length > 2 ? Integer.parseInt(parts[2]) : 0
        };
    }
}
