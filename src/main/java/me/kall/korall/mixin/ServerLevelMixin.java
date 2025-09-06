package me.kall.korall.mixin;

import me.kall.korall.api.Registry;
import me.kall.korall.api.Trackable;
import me.kall.korall.data.BlockTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Shadow @Nonnull public abstract MinecraftServer getServer();

    @Unique private BlockTracker block$trackData;

    @Inject(method = "onBlockStateChange", at = @At("HEAD"))
    private void onBlockChange(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        block$track(pos, oldState, newState);
    }

    @Unique
    private void block$track(BlockPos pos, @NotNull BlockState oldState, @NotNull BlockState newState) {
        ServerLevel level = (ServerLevel) (Object) this;

        Block oldBlock = oldState.getBlock();
        Block newBlock = newState.getBlock();

        boolean isWorldGen = !this.getServer().isSameThread();

        if (Trackable.isTracked(oldBlock)) {
            boolean acceptWorldGen = Trackable.acceptWorldGen(oldBlock);
            if (acceptWorldGen || !isWorldGen) {
                this.getServer().execute(() -> this.block$getTrackData(level).removeBlock(level, pos, Registry.getRegistryName(oldBlock)));
            }
        }

        if (Trackable.isTracked(newBlock)) {
            boolean acceptWorldGen = Trackable.acceptWorldGen(newBlock);
            if (acceptWorldGen || !isWorldGen) {
                this.getServer().execute(() -> this.block$getTrackData(level).addBlock(level, pos, Registry.getRegistryName(newBlock)));
            }
        }
    }

    @Unique
    private BlockTracker block$getTrackData(ServerLevel level) {
        if (this.block$trackData == null) this.block$trackData = BlockTracker.get(level);
        return this.block$trackData;
    }
}
