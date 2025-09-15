package me.kall.korall.mixin.impl;

import me.kall.korall.event.EntityChunkChangeEvent;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "setPosRaw", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;<init>(Lnet/minecraft/core/BlockPos;)V"))
    private void beforeChunkPosUpdate(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new EntityChunkChangeEvent.Pre((Entity) (Object) this));
    }

    @Inject(method = "setPosRaw", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;<init>(Lnet/minecraft/core/BlockPos;)V", shift = At.Shift.AFTER))
    private void afterChunkPosUpdate(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new EntityChunkChangeEvent.Post((Entity) (Object) this));
    }
}
