package com.akitain.fairlands.visibility;

import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;

/**
 * Public API for the visible (non-invisible) player count Fairlands exposes.
 * Other mods can call these; the server-list ping count is adjusted to match
 * when {@code fairlands:hide_invisible_players_from_count} is enabled.
 */
public final class PlayerVisibility {
    private PlayerVisibility() {
    }

    public static boolean isInvisible(ServerPlayer player) {
        return player.hasEffect(MobEffects.INVISIBILITY);
    }

    public static List<ServerPlayer> visiblePlayers(MinecraftServer server) {
        return server.getPlayerList().getPlayers().stream()
                .filter(player -> !isInvisible(player))
                .toList();
    }

    public static int visiblePlayerCount(MinecraftServer server) {
        return (int) server.getPlayerList().getPlayers().stream()
                .filter(player -> !isInvisible(player))
                .count();
    }
}
