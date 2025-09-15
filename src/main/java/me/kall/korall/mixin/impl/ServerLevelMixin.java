package me.kall.korall.mixin.impl;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import me.kall.korall.event.BlockChangeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import java.util.Optional;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Shadow @Nonnull public abstract MinecraftServer getServer();

    @WrapMethod(method = "onBlockStateChange")
    private void onChange(BlockPos pos, BlockState oldState, BlockState newState, @NotNull Operation<Void> original) {
        MinecraftForge.EVENT_BUS.post(new BlockChangeEvent(pos, (ServerLevel) (Object) this, oldState, newState, this.getServer().isSameThread()));
        original.call(pos, oldState, newState);
    }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "LocalMayBeArgsOnly"})
    @Inject(method = "onBlockStateChange", at = @At(value = "INVOKE", target = "Ljava/util/Objects;equals(Ljava/lang/Object;Ljava/lang/Object;)Z", remap = false, shift = At.Shift.AFTER))
    private void poiChange(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci, @Local(ordinal = 0) Optional<Holder<PoiType>> oldPoi, @Local(ordinal = 1) Optional<Holder<PoiType>> newPoi) {
        MinecraftForge.EVENT_BUS.post(new BlockChangeEvent.PoiChange(pos, (ServerLevel) (Object) this, oldState, newState, this.getServer().isSameThread(), oldPoi, newPoi));
    }
}
