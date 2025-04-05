package org.nakolotnik.wt.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nakolotnik.wt.cutscene.CutsceneManager;
import org.nakolotnik.wt.cutscene.CutsceneRecorder;

@Mod.EventBusSubscriber(modid = "wt")
public class CutsceneCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("cutscene")
                        .then(Commands.literal("play")
                                .then(Commands.argument("cutscene_name", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            for (String name : CutsceneManager.getCutsceneNames()) {
                                                builder.suggest(name);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("relative", BoolArgumentType.bool())
                                                .then(Commands.argument("speed", FloatArgumentType.floatArg(0.1f, 10.0f))
                                                        .executes(context -> {
                                                            String cutsceneName = StringArgumentType.getString(context, "cutscene_name");
                                                            boolean relative = BoolArgumentType.getBool(context, "relative");
                                                            float speed = FloatArgumentType.getFloat(context, "speed");
                                                            if (CutsceneManager.getCutsceneNames().contains(cutsceneName)) {
                                                                CutsceneManager.startCutscene(cutsceneName, relative, speed);
                                                                context.getSource().sendSuccess(() -> Component.literal("Starting cutscene: " + cutsceneName + " (relative: " + relative + ", speed: " + speed + "x)"), false);
                                                                return 1;
                                                            } else {
                                                                context.getSource().sendFailure(Component.literal("Cutscene not found: " + cutsceneName));
                                                                return 0;
                                                            }
                                                        }))
                                                .executes(context -> {
                                                    String cutsceneName = StringArgumentType.getString(context, "cutscene_name");
                                                    boolean relative = BoolArgumentType.getBool(context, "relative");
                                                    if (CutsceneManager.getCutsceneNames().contains(cutsceneName)) {
                                                        CutsceneManager.startCutscene(cutsceneName, relative, 1.0f);
                                                        context.getSource().sendSuccess(() -> Component.literal("Starting cutscene: " + cutsceneName + " (relative: " + relative + ")"), false);
                                                        return 1;
                                                    } else {
                                                        context.getSource().sendFailure(Component.literal("Cutscene not found: " + cutsceneName));
                                                        return 0;
                                                    }
                                                }))
                                        .executes(context -> {
                                            String cutsceneName = StringArgumentType.getString(context, "cutscene_name");
                                            if (CutsceneManager.getCutsceneNames().contains(cutsceneName)) {
                                                CutsceneManager.startCutscene(cutsceneName, false, 1.0f);
                                                context.getSource().sendSuccess(() -> Component.literal("Starting cutscene: " + cutsceneName), false);
                                                return 1;
                                            } else {
                                                context.getSource().sendFailure(Component.literal("Cutscene not found: " + cutsceneName));
                                                return 0;
                                            }
                                        })
                                ))
                        .then(Commands.literal("stop")
                                .executes(context -> {
                                    if (CutsceneManager.isCutsceneActive()) {
                                        CutsceneManager.stopCutscene();
                                        context.getSource().sendSuccess(() -> Component.literal("Cutscene stopped"), false);
                                        return 1;
                                    } else {
                                        context.getSource().sendFailure(Component.literal("No active cutscene to stop"));
                                        return 0;
                                    }
                                }))
                        .then(Commands.literal("record")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .then(Commands.argument("relative", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    String name = StringArgumentType.getString(context, "name");
                                                    boolean relative = BoolArgumentType.getBool(context, "relative");
                                                    CutsceneRecorder.startRecording(name, relative);
                                                    context.getSource().sendSuccess(() -> Component.literal("Started recording: " + name + " (relative: " + relative + ")"), false);
                                                    return 1;
                                                }))
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name");
                                            CutsceneRecorder.startRecording(name, false);
                                            context.getSource().sendSuccess(() -> Component.literal("Started recording: " + name), false);
                                            return 1;
                                        })
                                ))
                        .then(Commands.literal("stoprecord")
                                .executes(context -> {
                                    CutsceneRecorder.stopRecording();
                                    context.getSource().sendSuccess(() -> Component.literal("Stopped recording"), false);
                                    return 1;
                                }))
                        .then(Commands.literal("preview")
                                .executes(context -> {
                                    if (CutsceneRecorder.isRecording()) {
                                        context.getSource().sendFailure(Component.literal("Cannot preview while recording. Use /cutscene stoprecord first."));
                                        return 0;
                                    }
                                    CutsceneRecorder.preview();
                                    return 1;
                                }))
        );

        dispatcher.register(
                Commands.literal("setdot")
                        .then(Commands.argument("coordinates", Vec3Argument.vec3())
                                .then(Commands.argument("duration", IntegerArgumentType.integer(1, 1000))
                                        .then(Commands.argument("interpolation", StringArgumentType.string())
                                                .suggests((context, builder) -> {
                                                    builder.suggest("LINEAR");
                                                    builder.suggest("SMOOTH");
                                                    builder.suggest("CURVE");
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    Vec3 pos = Vec3Argument.getVec3(context, "coordinates");
                                                    int duration = IntegerArgumentType.getInteger(context, "duration");
                                                    String interpolation = StringArgumentType.getString(context, "interpolation").toUpperCase();
                                                    CutsceneRecorder.addDot(pos.x, pos.y, pos.z, duration, interpolation);
                                                    context.getSource().sendSuccess(() -> Component.literal("Added point at (" + pos.x + ", " + pos.y + ", " + pos.z + ")"), false);
                                                    return 1;
                                                }))
                                        .executes(context -> {
                                            Vec3 pos = Vec3Argument.getVec3(context, "coordinates");
                                            int duration = IntegerArgumentType.getInteger(context, "duration");
                                            CutsceneRecorder.addDot(pos.x, pos.y, pos.z, duration, "SMOOTH");
                                            context.getSource().sendSuccess(() -> Component.literal("Added point at (" + pos.x + ", " + pos.y + ", " + pos.z + ")"), false);
                                            return 1;
                                        }))
                                .executes(context -> {
                                    Vec3 pos = Vec3Argument.getVec3(context, "coordinates");
                                    CutsceneRecorder.addDot(pos.x, pos.y, pos.z);
                                    context.getSource().sendSuccess(() -> Component.literal("Added point at (" + pos.x + ", " + pos.y + ", " + pos.z + ")"), false);
                                    return 1;
                                })
                        )
        );
    }
}