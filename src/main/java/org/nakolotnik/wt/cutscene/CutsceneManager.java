package org.nakolotnik.wt.cutscene;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "wt", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
@OnlyIn(Dist.CLIENT)
public class CutsceneManager {
    private static final Gson GSON = new Gson();
    private static final Map<String, Cutscene> activeCutscenes = new HashMap<>();
    private static boolean isCutsceneActive = false;
    private static Cutscene currentCutscene;
    private static float progress = 0.0f;
    private static float playbackSpeed = 1.0f;
    private static Vec3 startingPosition;
    private static boolean isRelativeMode = false;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            try {
                Map<ResourceLocation, Resource> resources = resourceManager.listResources("cutscene", rl -> rl.getNamespace().equals("wt") && rl.getPath().endsWith(".json"));
                for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                    ResourceLocation location = entry.getKey();
                    try (InputStreamReader reader = new InputStreamReader(entry.getValue().open())) {
                        JsonObject json = GSON.fromJson(reader, JsonObject.class);
                        loadCutsceneFromJson(json);
                    } catch (Exception e) {
                        System.err.println("Failed to load cutscene from " + location + ": " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to load cutscenes: " + e.getMessage());
            }
        });
    }

    public static void loadCutsceneFromJson(JsonObject json) {
        String name = json.get("name").getAsString();
        JsonArray frames = json.getAsJsonArray("frames");

        List<CutsceneFrame> cutsceneFrames = new ArrayList<>();
        float totalWeight = 0;
        for (int i = 0; i < frames.size(); i++) {
            JsonObject frameJson = frames.get(i).getAsJsonObject();
            Vec3 position = new Vec3(
                    frameJson.get("posX").getAsDouble(),
                    frameJson.get("posY").getAsDouble(),
                    frameJson.get("posZ").getAsDouble()
            );
            float yaw = frameJson.get("yaw").getAsFloat();
            float pitch = frameJson.get("pitch").getAsFloat();
            int duration = frameJson.get("duration").getAsInt();

            totalWeight += duration;

            CutsceneFrame.InterpolationType interpolation = CutsceneFrame.InterpolationType.SMOOTH;
            if (frameJson.has("interpolation")) {
                try {
                    interpolation = CutsceneFrame.InterpolationType.valueOf(frameJson.get("interpolation").getAsString().toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.err.println("Unknown interpolation type, using SMOOTH");
                }
            }

            List<CutsceneEvent> events = new ArrayList<>();
            if (frameJson.has("events")) {
                JsonArray eventArray = frameJson.getAsJsonArray("events");
                for (JsonElement eventElement : eventArray) {
                    JsonObject eventJson = eventElement.getAsJsonObject();
                    String type = eventJson.get("type").getAsString();
                    String value = eventJson.has("value") ? eventJson.get("value").getAsString() : "";
                    events.add(new CutsceneEvent(type, value));
                }
            }

            cutsceneFrames.add(new CutsceneFrame(position, yaw, pitch, duration, interpolation, events));
        }

        for (CutsceneFrame frame : cutsceneFrames) {
            frame.normalizedWeight = frame.duration / totalWeight;
        }

        Cutscene cutscene = new Cutscene(name, cutsceneFrames);
        activeCutscenes.put(name, cutscene);
    }

    public static void startCutscene(String name, boolean relative, float speed) {
        Cutscene cutscene = activeCutscenes.get(name);
        if (cutscene != null) {
            startCutscene(cutscene, relative, speed);
        }
    }

    public static void startCutscene(Cutscene cutscene, boolean relative, float speed) {
        currentCutscene = cutscene;
        isCutsceneActive = true;
        progress = 0.0f;
        playbackSpeed = speed;
        isRelativeMode = relative;

        Minecraft mc = Minecraft.getInstance();
        startingPosition = mc.player.position();
        mc.player.noPhysics = true;
        mc.player.setNoGravity(true);
        mc.player.setInvisible(true);
        mc.options.hideGui = true;
    }

    public static void stopCutscene() {
        isCutsceneActive = false;
        currentCutscene = null;
        progress = 0.0f;
        playbackSpeed = 1.0f;
        isRelativeMode = false;
        Minecraft mc = Minecraft.getInstance();
        mc.player.noPhysics = false;
        mc.player.setNoGravity(false);
        mc.player.setInvisible(false);
        mc.options.hideGui = false;
    }

    public static boolean isCutsceneActive() {
        return isCutsceneActive;
    }

    public static List<String> getCutsceneNames() {
        return new ArrayList<>(activeCutscenes.keySet());
    }

    public static Vec3 getCurrentPosition(Vec3 framePosition) {
        return isRelativeMode && startingPosition != null ? startingPosition.add(framePosition) : framePosition;
    }

    static Cutscene getCurrentCutscene() {
        return currentCutscene;
    }

    static float getProgress() {
        return progress;
    }

    static void incrementProgress(float delta) {
        progress += delta * playbackSpeed;
        if (progress > 1.0f) progress = 1.0f;
    }

    public static class CutsceneFrame {
        Vec3 position;
        float yaw;
        float pitch;
        int duration;
        float normalizedWeight;
        InterpolationType interpolationType;
        List<CutsceneEvent> events;

        public enum InterpolationType {
            LINEAR, SMOOTH, CURVE
        }

        CutsceneFrame(Vec3 position, float yaw, float pitch, int duration, InterpolationType interpolationType, List<CutsceneEvent> events) {
            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
            this.duration = duration;
            this.interpolationType = interpolationType;
            this.events = events;
        }
    }

    public static class CutsceneEvent {
        String type;
        String value;

        CutsceneEvent(String type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    static class Cutscene {
        String name;
        List<CutsceneFrame> frames;

        Cutscene(String name, List<CutsceneFrame> frames) {
            this.name = name;
            this.frames = frames;
        }
    }
}