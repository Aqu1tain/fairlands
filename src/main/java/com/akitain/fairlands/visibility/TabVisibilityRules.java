package com.akitain.fairlands.visibility;

import com.akitain.fairlands.mixin.ClientboundPlayerInfoUpdatePacketAccessor;
import com.akitain.fairlands.rule.FairlandsGameRules;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.PlayerModelPart;

public final class TabVisibilityRules {
    private TabVisibilityRules() {
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncJoinedPlayer(handler.getPlayer()));
    }

    public static void syncPlayer(ServerPlayer player) {
        boolean listed = shouldList(player);
        for (ServerPlayer viewer : player.level().getServer().getPlayerList().getPlayers()) {
            if (viewer != player) {
                sendListedStatus(viewer, player, listed);
            }
        }

        sendSelfDisplayName(player, !listed);
    }

    private static void syncJoinedPlayer(ServerPlayer player) {
        for (ServerPlayer other : player.level().getServer().getPlayerList().getPlayers()) {
            if (other != player && !shouldList(other)) {
                sendListedStatus(player, other, false);
            }
        }

        syncPlayer(player);
    }

    private static boolean shouldList(ServerPlayer player) {
        return !FairlandsGameRules.hideInvisiblePlayersFromTabEnabled(player.level()) || !player.hasEffect(MobEffects.INVISIBILITY);
    }

    // A hidden player still sees their own tab entry, shown in italic as a reminder that others cannot see them.
    private static void sendSelfDisplayName(ServerPlayer player, boolean hidden) {
        Component displayName = hidden
                ? player.getName().copy().withStyle(ChatFormatting.ITALIC)
                : player.getTabListDisplayName();

        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                player
        );

        ((ClientboundPlayerInfoUpdatePacketAccessor) packet).fairlands$setEntries(List.of(createEntry(player, true, displayName)));
        player.connection.send(packet);
    }

    private static void sendListedStatus(ServerPlayer viewer, ServerPlayer subject, boolean listed) {
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                subject
        );

        ((ClientboundPlayerInfoUpdatePacketAccessor) packet).fairlands$setEntries(List.of(createEntry(subject, listed)));
        viewer.connection.send(packet);
    }

    private static ClientboundPlayerInfoUpdatePacket.Entry createEntry(ServerPlayer player, boolean listed) {
        return createEntry(player, listed, player.getTabListDisplayName());
    }

    private static ClientboundPlayerInfoUpdatePacket.Entry createEntry(ServerPlayer player, boolean listed, Component displayName) {
        RemoteChatSession.Data chatSession = Optionull.map(player.getChatSession(), RemoteChatSession::asData);
        return new ClientboundPlayerInfoUpdatePacket.Entry(
                player.getUUID(),
                player.getGameProfile(),
                listed,
                player.connection.latency(),
                player.gameMode(),
                displayName,
                player.isModelPartShown(PlayerModelPart.HAT),
                player.getTabListOrder(),
                chatSession
        );
    }
}
