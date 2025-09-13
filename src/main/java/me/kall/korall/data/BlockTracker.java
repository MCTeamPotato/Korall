package me.kall.korall.data;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.korall.Korall;
import me.kall.korall.api.Registry;
import me.kall.korall.api.Trackable;
import me.kall.korall.event.BlockChangeEvent;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockTracker extends SavedData {
    public static final String DATA_NAME = "TrackedBlockData";

    public static final Object2BooleanMap<ResourceLocation> TRACKED_BLOCKS = new Object2BooleanOpenHashMap<>();

    public final Object2ObjectMap<ResourceLocation, Long2ObjectMap<Object2ObjectMap<ResourceLocation, LongSet>>> blockStorage = new Object2ObjectOpenHashMap<>();

    public static void register() {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(BlockTracker::initTracker);
        bus.addListener(BlockTracker::cleanData);
        bus.addListener(BlockTracker::blockChange);
    }

    private static void initTracker(@NotNull ServerStartingEvent event) {
        event.getServer().execute(() -> {
            for (var entry : BlockTracker.TRACKED_BLOCKS.object2BooleanEntrySet()) {
                ResourceLocation blockId = entry.getKey();
                boolean worldGen = entry.getBooleanValue();
                Block block = ForgeRegistries.BLOCKS.getValue(blockId);
                if (block instanceof Trackable trackable) {
                    trackable.trackable$setTracked(true);
                    trackable.worldGen$setAccepted(worldGen);
                    Korall.LOGGER.debug("Successfully registered tracked block: {}", blockId);
                } else {
                    Korall.LOGGER.warn("Failed to register tracked block: {} - Block not found", blockId);
                }
            }
        });
    }

    private static void cleanData(ServerStartedEvent event) {
        event.getServer().execute(() -> {
            for (ServerLevel level : event.getServer().getAllLevels()) {
                BlockTracker blockTracker = BlockTracker.get(level);
                List<LongObjectPair<ResourceLocation>> toRemove = new ArrayList<>();
                ResourceLocation dim = level.dimension().location();
                var dimMap = blockTracker.blockStorage.get(dim);
                if (dimMap == null) continue;
                dimMap.forEach((chunkKey, blockMap) -> {
                    for (ResourceLocation id : blockMap.keySet()) {
                        if (!Trackable.isTracked(ForgeRegistries.BLOCKS.getValue(id))) toRemove.add(new LongObjectImmutablePair<>(chunkKey, id));
                    }
                });
                for (LongObjectPair<ResourceLocation> entry : toRemove) {
                    long chunkKey = entry.firstLong();
                    ResourceLocation block = entry.second();
                    try {
                        dimMap.get(chunkKey).remove(block);
                    } catch (Throwable e) {
                        Korall.LOGGER.error("Failed to remove out-dated tracked block {} from chunk {}", block, new ChunkPos(chunkKey).toString());
                        Korall.LOGGER.error("", e);
                    }
                }
            }
        });
    }

    private static void blockChange(BlockChangeEvent event) {
        BlockPos pos = event.getPos();
        BlockState oldState = event.getOldState();
        BlockState newState = event.getNewState();
        ServerLevel level = event.getLevel();
        Block oldBlock = oldState.getBlock();
        Block newBlock = newState.getBlock();

        boolean isWorldGen = !level.getServer().isSameThread();

        if (Trackable.isTracked(oldBlock)) {
            boolean acceptWorldGen = Trackable.acceptWorldGen(oldBlock);
            if (acceptWorldGen || !isWorldGen) {
                level.getServer().execute(() -> BlockTracker.get(level).removeBlock(level, pos, Registry.getRegistryName(oldBlock)));
            }
        }

        if (Trackable.isTracked(newBlock)) {
            boolean acceptWorldGen = Trackable.acceptWorldGen(newBlock);
            if (acceptWorldGen || !isWorldGen) {
                level.getServer().execute(() -> BlockTracker.get(level).addBlock(level, pos, Registry.getRegistryName(newBlock)));
            }
        }
    }
    public static BlockTracker load(CompoundTag nbt) {
        BlockTracker data = new BlockTracker();
        Korall.LOGGER.info("Loading tracked block data...");

        for (String dimKey : nbt.getAllKeys()) {
            ResourceLocation dimID = ResourceLocation.tryParse(dimKey);
            if (dimID == null) {
                Korall.LOGGER.warn("Skipping invalid dimension key in NBT: {}", dimKey);
                continue;
            }

            CompoundTag dimTag = nbt.getCompound(dimKey);
            var chunkMap = new Long2ObjectOpenHashMap<Object2ObjectMap<ResourceLocation, LongSet>>();

            for (String chunkKeyStr : dimTag.getAllKeys()) {
                long chunkKey;
                try {
                    chunkKey = Long.parseLong(chunkKeyStr);
                } catch (NumberFormatException e) {
                    Korall.LOGGER.warn("Skipping invalid chunk key in dimension {}: {}", dimID, chunkKeyStr);
                    Korall.LOGGER.warn("", e);
                    continue;
                }

                CompoundTag chunkTag = dimTag.getCompound(chunkKeyStr);
                var blockMap = new Object2ObjectOpenHashMap<ResourceLocation, LongSet>();

                for (String blockKey : chunkTag.getAllKeys()) {
                    ResourceLocation blockId = ResourceLocation.tryParse(blockKey);
                    if (blockId == null) {
                        Korall.LOGGER.warn("Skipping invalid block key in chunk {}: {}", chunkKeyStr, blockKey);
                        continue;
                    }

                    ListTag posList = chunkTag.getList(blockKey, Tag.TAG_LONG);
                    LongSet posSet = new LongOpenHashSet();
                    for (Tag tag : posList) {
                        if (tag instanceof LongTag longTag) {
                            posSet.add(longTag.getAsLong());
                        } else {
                            Korall.LOGGER.warn("Skipping invalid position tag in block {}: expected LONG, got {}", blockId, tag.getId());
                        }
                    }
                    blockMap.put(blockId, posSet);
                }

                chunkMap.put(chunkKey, blockMap);
            }

            data.blockStorage.put(dimID, chunkMap);
            Korall.LOGGER.debug("Loaded {} chunks for dimension {}", chunkMap.size(), dimID);
        }

        Korall.LOGGER.info("TrackedBlockData loaded successfully with {} dimensions", data.blockStorage.size());
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        Korall.LOGGER.info("Saving tracked block data...");
        int totalDimensions = 0;
        int totalChunks = 0;
        int totalBlocks = 0;

        for (var dimEntry : blockStorage.object2ObjectEntrySet()) {
            CompoundTag dimTag = new CompoundTag();
            int dimensionChunks = 0;
            int dimensionBlocks = 0;

            for (var chunkEntry : dimEntry.getValue().long2ObjectEntrySet()) {
                CompoundTag chunkTag = new CompoundTag();
                int chunkBlocks = 0;

                for (var blockEntry : chunkEntry.getValue().object2ObjectEntrySet()) {
                    ListTag posList = new ListTag();
                    for (long posLong : blockEntry.getValue()) {
                        posList.add(LongTag.valueOf(posLong));
                    }
                    chunkTag.put(blockEntry.getKey().toString(), posList);
                    chunkBlocks++;
                }

                dimTag.put(Long.toString(chunkEntry.getLongKey()), chunkTag);
                dimensionChunks++;
                dimensionBlocks += chunkBlocks;
            }

            nbt.put(dimEntry.getKey().toString(), dimTag);
            totalDimensions++;
            totalChunks += dimensionChunks;
            totalBlocks += dimensionBlocks;

            Korall.LOGGER.debug("Saved dimension {}: {} chunks, {} blocks", dimEntry.getKey(), dimensionChunks, dimensionBlocks);
        }

        Korall.LOGGER.info("TrackedBlockData saved: {} dimensions, {} chunks, {} blocks", totalDimensions, totalChunks, totalBlocks);
        return nbt;
    }

    public static BlockTracker get(ServerLevel level) {
        try {
            return level.getDataStorage().computeIfAbsent(BlockTracker::load, BlockTracker::new, DATA_NAME);
        } catch (Exception e) {
            Korall.LOGGER.error("Failed to get BlockTracker for level {}", level.dimension().location());
            Korall.LOGGER.error("", e);
            return new BlockTracker();
        }
    }

    public void addBlock(ServerLevel level, BlockPos pos, ResourceLocation blockId) {
        try {
            ResourceLocation dim = level.dimension().location();
            long chunkKey = ChunkPos.asLong(pos);

            if (blockStorage.computeIfAbsent(dim, k -> new Long2ObjectOpenHashMap<>()).computeIfAbsent(chunkKey, k -> new Object2ObjectOpenHashMap<>()).computeIfAbsent(blockId, k -> new LongOpenHashSet()).add(pos.asLong())) {
                setDirty();
                Korall.LOGGER.debug("Added block {} at {} in dimension {}", blockId, pos, dim);
            }
        } catch (Exception e) {
            Korall.LOGGER.error("Failed to add block {} at {} in dimension {}", blockId, pos, level.dimension().location());
            Korall.LOGGER.error("", e);
        }
    }

    public void removeBlock(ServerLevel level, BlockPos pos, ResourceLocation blockId) {
        try {
            ResourceLocation dim = level.dimension().location();
            var chunkMap = blockStorage.get(dim);
            if (chunkMap == null) {
                Korall.LOGGER.debug("Attempted to remove block from non-existent dimension: {}", dim);
                return;
            }

            long chunkKey = ChunkPos.asLong(pos);
            var blockMap = chunkMap.get(chunkKey);
            if (blockMap == null) {
                Korall.LOGGER.debug("Attempted to remove block from non-existent chunk: {} in dimension {}", chunkKey, dim);
                return;
            }

            LongSet posSet = blockMap.get(blockId);
            if (posSet == null) {
                Korall.LOGGER.debug("Attempted to remove non-existent block: {} in chunk {} dimension {}", blockId, chunkKey, dim);
                return;
            }

            if (posSet.remove(pos.asLong())) {
                setDirty();
                Korall.LOGGER.debug("Removed block {} at {} in dimension {}", blockId, pos, dim);

                if (posSet.isEmpty()) {
                    blockMap.remove(blockId);
                    if (blockMap.isEmpty()) {
                        chunkMap.remove(chunkKey);
                        if (chunkMap.isEmpty()) {
                            blockStorage.remove(dim);
                            Korall.LOGGER.debug("Removed empty dimension: {}", dim);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Korall.LOGGER.error("Failed to remove block {} at {} in dimension {}", blockId, pos, level.dimension().location());
            Korall.LOGGER.error("", e);
        }
    }
}
