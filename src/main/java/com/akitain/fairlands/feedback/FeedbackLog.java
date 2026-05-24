package com.akitain.fairlands.feedback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class FeedbackLog {
    private static final String LOG_FILE = "fairlands-feedback.log";

    private FeedbackLog() {
    }

    public static void write(ServerPlayer player, String message) {
        Path logFile = player.level().getServer().getServerDirectory().resolve(LOG_FILE);
        String line = formatLine(player, message);

        try {
            Files.writeString(logFile, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            throw new FeedbackWriteException(exception);
        }
    }

    private static String formatLine(ServerPlayer player, String message) {
        Vec3 position = player.position();
        return "[%s] %s (%s) %s %.2f %.2f %.2f: %s%n".formatted(
                OffsetDateTime.now(ZoneOffset.UTC),
                sanitize(player.getScoreboardName()),
                player.getUUID(),
                player.level().dimension().identifier(),
                position.x,
                position.y,
                position.z,
                sanitize(message)
        );
    }

    private static String sanitize(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }
}
