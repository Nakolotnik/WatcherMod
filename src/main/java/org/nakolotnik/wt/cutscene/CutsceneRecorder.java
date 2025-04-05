package org.nakolotnik.wt.cutscene;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CutsceneRecorder {
    private static boolean isRecording = false;
    private static List<CutsceneManager.CutsceneFrame> recordedFrames = new ArrayList<>();
    private static String cutsceneName = "new_cutscene";
    private static int defaultDuration = 20;
    private static boolean useRelativeCoords = false;
    private static Vec3 startPosition;

    public static void startRecording(String name, boolean relative) {
        if (isRecording) {
            sendMessage("Already recording a cutscene!");
            return;
        }
        isRecording = true;
        recordedFrames.clear();
        cutsceneName = name;
        useRelativeCoords = relative;
        startPosition = Minecraft.getInstance().player.position();
        sendMessage("Started recording cutscene: " + name + " (relative: " + relative + ")");
    }

    public static void stopRecording() {
        if (!isRecording) {
            sendMessage("Not currently recording!");
            return;
        }
        isRecording = false;
        saveCutsceneToJson();
        sendMessage("Stopped recording and saved cutscene: " + cutsceneName);
    }

    public static void addDot(double x, double y, double z, int duration, String interpolation) {
        if (!isRecording) {
            sendMessage("Start recording first with /cutscene record <name>!");
            return;
        }
        Player player = Minecraft.getInstance().player;
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        Vec3 position;
        if (useRelativeCoords) {
            position = new Vec3(x, y, z).subtract(startPosition);
        } else {
            position = new Vec3(x, y, z);
        }

        CutsceneManager.CutsceneFrame.InterpolationType interpType;
        try {
            interpType = CutsceneManager.CutsceneFrame.InterpolationType.valueOf(interpolation.toUpperCase());
        } catch (IllegalArgumentException e) {
            interpType = CutsceneManager.CutsceneFrame.InterpolationType.SMOOTH;
            sendMessage("Invalid interpolation type '" + interpolation + "', using SMOOTH");
        }

        recordedFrames.add(new CutsceneManager.CutsceneFrame(position, yaw, pitch, duration, interpType, Collections.emptyList()));
        sendMessage("Added point at (" + position.x + ", " + position.y + ", " + position.z + ") with duration " + duration + " and interpolation " + interpType);
    }

    public static void addDot(double x, double y, double z) {
        addDot(x, y, z, defaultDuration, "SMOOTH");
    }

    public static void preview() {
        if (isRecording) {
            sendMessage("Cannot preview while recording. Use /cutscene stoprecord first.");
            return;
        }
        if (recordedFrames.isEmpty()) {
            sendMessage("No frames recorded to preview!");
            return;
        }
        CutsceneManager.Cutscene tempCutscene = new CutsceneManager.Cutscene(cutsceneName, recordedFrames);
        CutsceneManager.startCutscene(tempCutscene, useRelativeCoords, 1.0f);
        sendMessage("Previewing recorded cutscene: " + cutsceneName);
    }

    private static void saveCutsceneToJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", cutsceneName);
        JsonArray frames = new JsonArray();

        for (CutsceneManager.CutsceneFrame frame : recordedFrames) {
            JsonObject frameJson = new JsonObject();
            frameJson.addProperty("posX", frame.position.x);
            frameJson.addProperty("posY", frame.position.y);
            frameJson.addProperty("posZ", frame.position.z);
            frameJson.addProperty("yaw", frame.yaw);
            frameJson.addProperty("pitch", frame.pitch);
            frameJson.addProperty("duration", frame.duration);
            frameJson.addProperty("interpolation", frame.interpolationType.name());
            frames.add(frameJson);
        }

        json.add("frames", frames);

        try {
            String path = Paths.get("config", "wt", "cutscenes", cutsceneName + ".json").toString();
            java.nio.file.Files.createDirectories(Paths.get("config", "wt", "cutscenes"));
            try (FileWriter writer = new FileWriter(path)) {
                writer.write(new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(json));
            }
            CutsceneManager.loadCutsceneFromJson(json);
        } catch (IOException e) {
            sendMessage("Failed to save cutscene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean isRecording() {
        return isRecording;
    }

    private static void sendMessage(String message) {
        Minecraft.getInstance().player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
    }
}