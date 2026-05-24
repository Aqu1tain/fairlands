package com.akitain.fairlands.mixin;

import com.akitain.fairlands.spawn.SpawnProtectionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WitherSkullBlock.class)
public abstract class WitherSkullBlockMixin {
    @Redirect(
            method = "checkSpawn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/SkullBlockEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;"
            )
    )
    private static Entity fairlands$blockWitherSpawnInProtectedSpawn(EntityType<?> entityType, Level targetLevel, EntitySpawnReason spawnReason, Level level, BlockPos pos, SkullBlockEntity skullBlockEntity) {
        if (SpawnProtectionRules.blocksWitherSpawn(level, pos)) {
            return null;
        }

        return entityType.create(targetLevel, spawnReason);
    }
}
