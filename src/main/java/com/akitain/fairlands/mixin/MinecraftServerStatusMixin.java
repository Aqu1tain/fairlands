package com.akitain.fairlands.mixin;

import com.akitain.fairlands.rule.FairlandsGameRules;
import com.akitain.fairlands.visibility.PlayerVisibility;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerStatusMixin {
    @Redirect(
            method = "buildPlayerStatus",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;getPlayers()Ljava/util/List;")
    )
    private List<ServerPlayer> fairlands$hideInvisibleFromStatus(PlayerList playerList) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        if (!FairlandsGameRules.hideInvisiblePlayersFromCountEnabled(server.overworld())) {
            return playerList.getPlayers();
        }

        return PlayerVisibility.visiblePlayers(server);
    }
}
