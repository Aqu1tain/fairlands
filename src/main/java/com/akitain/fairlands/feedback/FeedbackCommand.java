package com.akitain.fairlands.feedback;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class FeedbackCommand {
    private FeedbackCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                Commands.literal("feedback")
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> submitFeedback(context.getSource(), StringArgumentType.getString(context, "message"))))
        ));
    }

    private static int submitFeedback(CommandSourceStack source, String message) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.literal("Only players can send feedback."));
            return 0;
        }

        try {
            FeedbackLog.write(player, message);
        } catch (FeedbackWriteException exception) {
            source.sendFailure(Component.literal("Feedback could not be saved. Please tell an admin."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Feedback sent. Thank you."), false);
        return 1;
    }
}
