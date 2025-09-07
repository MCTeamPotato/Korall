package me.kall.korall.api;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import me.kall.korall.data.BlockTracker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;

public interface Trackable {
    boolean trackable$isTracked();
    void trackable$setTracked(boolean tracked);

    boolean worldGen$accepted();
    void worldGen$setAccepted(boolean accepted);

    static boolean isTracked(Block block) {
        return ((Trackable)block).trackable$isTracked();
    }

    static boolean acceptWorldGen(Block block) {
        return ((Trackable)block).worldGen$accepted();
    }

    static void trackBlock(Block block) {
        BlockTracker.TRACKED_BLOCKS.put(Registry.getRegistryName(block), true);
    }

    static void trackBlock(ResourceLocation block) {
        BlockTracker.TRACKED_BLOCKS.put(block, true);
    }

    static void trackBlock(Block block, boolean acceptWorldGen) {
        BlockTracker.TRACKED_BLOCKS.put(Registry.getRegistryName(block), acceptWorldGen);
    }

    static void trackBlock(ResourceLocation block, boolean acceptWorldGen) {
        BlockTracker.TRACKED_BLOCKS.put(block, acceptWorldGen);
    }

    static LongSet getTrackedBlocks(ServerLevel level, ChunkPos chunkPos, ResourceLocation blockId) {
        var chunkMap = BlockTracker.get(level).blockStorage.get(level.dimension().location());
        if (chunkMap == null) return LongSets.emptySet();
        var blockMap = chunkMap.get(chunkPos.toLong());
        if (blockMap == null) return LongSets.emptySet();
        var blocks = blockMap.get(blockId);
        return blocks == null ? LongSets.emptySet() : LongSets.unmodifiable(blocks);
    }
}
