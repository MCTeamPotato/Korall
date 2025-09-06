package me.kall.korall.mixin;

import me.kall.korall.data.EntityTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "setPosRaw", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;<init>(Lnet/minecraft/core/BlockPos;)V"))
    private void beforeChunkPosUpdate(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level() instanceof ServerLevel serverLevel) {
            EntityTracker.removeEntity(self, serverLevel);
        }
    }

    @Inject(method = "setPosRaw", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;<init>(Lnet/minecraft/core/BlockPos;)V", shift = At.Shift.AFTER))
    private void afterChunkPosUpdate(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level() instanceof ServerLevel serverLevel) {
            EntityTracker.addEntity(self, serverLevel);
        }
    }
}
