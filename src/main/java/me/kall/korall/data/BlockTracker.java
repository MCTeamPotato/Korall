package me.kall.korall.data;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import me.kall.korall.Korall;
import me.kall.korall.api.Registry;
import me.kall.korall.api.Trackable;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockTracker extends SavedData {
    public static final String DATA_NAME = "TrackedBlockData";

    public static final Object2BooleanMap<ResourceLocation> TRACKED_BLOCKS = new Object2BooleanOpenHashMap<>();

    public final Object2ObjectMap<ResourceLocation, Object2ObjectMap<ResourceLocation, Long2ObjectMap<LongSet>>> blockStorage = new Object2ObjectOpenHashMap<>();

    public static void trackBlock(Block block) {
        TRACKED_BLOCKS.put(Registry.getRegistryName(block), true);
    }

    public static void trackBlock(ResourceLocation block) {
        TRACKED_BLOCKS.put(block, true);
    }

    public static void trackBlock(Block block, boolean acceptWorldGen) {
        TRACKED_BLOCKS.put(Registry.getRegistryName(block), acceptWorldGen);
    }

    public static void trackBlock(ResourceLocation block, boolean acceptWorldGen) {
        TRACKED_BLOCKS.put(block, acceptWorldGen);
    }

    public static void register() {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(BlockTracker::initTracker);
        bus.addListener(BlockTracker::cleanData);
    }

    private static void initTracker(@NotNull ServerStartingEvent event) {
        event.getServer().execute(() -> {
            for (Object2BooleanMap.Entry<ResourceLocation> entry : BlockTracker.TRACKED_BLOCKS.object2BooleanEntrySet()) {
                ResourceLocation blockId = entry.getKey();
                boolean worldGen = entry.getBooleanValue();
                Block block = ForgeRegistries.BLOCKS.getValue(blockId);
                if (block instanceof Trackable trackable) {
                    trackable.trackable$setTracked(true);
                    trackable.worldGen$setAccepted(worldGen);
                }
            }
        });
    }

    private static void cleanData(@NotNull ServerStartedEvent event) {
        event.getServer().execute(() -> {
            for (ServerLevel level : event.getServer().getAllLevels()) {
                List<ResourceLocation> toRemove = new ArrayList<>();
                BlockTracker data = BlockTracker.get(level);
                for (ResourceLocation id : data.blockStorage.keySet()) {
                    if (Trackable.isTracked(ForgeRegistries.BLOCKS.getValue(id))) continue;
                    toRemove.add(id);
                }
                toRemove.forEach(data.blockStorage::remove);
                if (toRemove.isEmpty()) continue;
                Korall.LOGGER.info("[Korall] Out-dated tracked blocks in `{}` are cleaned: {}", level.dimension().location(), toRemove.toArray());
            }
        });
    }

    public static BlockTracker load(CompoundTag nbt) {
        BlockTracker data = new BlockTracker();
        for (String registryIdStr : nbt.getAllKeys()) {
            CompoundTag registryTag = nbt.getCompound(registryIdStr);
            ResourceLocation registryId = ResourceLocation.tryParse(registryIdStr);
            if (registryId == null) continue;

            Object2ObjectMap<ResourceLocation, Long2ObjectMap<LongSet>> dimMap = new Object2ObjectOpenHashMap<>();

            for (String dimKey : registryTag.getAllKeys()) {
                CompoundTag dimTag = registryTag.getCompound(dimKey);
                ResourceLocation dimID = ResourceLocation.tryParse(dimKey);
                if (dimID == null) continue;

                Long2ObjectMap<LongSet> chunkMap = new Long2ObjectOpenHashMap<>();

                for (String chunkKeyStr : dimTag.getAllKeys()) {
                    long chunkKey = Long.parseLong(chunkKeyStr);
                    ListTag posList = dimTag.getList(chunkKeyStr, Tag.TAG_LONG);

                    LongSet posSet = new LongOpenHashSet();
                    for (Tag tag : posList) {
                        posSet.add(((LongTag) tag).getAsLong());
                    }

                    chunkMap.put(chunkKey, posSet);
                }

                dimMap.put(dimID, chunkMap);
            }

            data.blockStorage.put(registryId, dimMap);
        }
        Korall.LOGGER.debug("TrackedBlockData loaded: {}", data.blockStorage);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        for (Object2ObjectMap.Entry<ResourceLocation, Object2ObjectMap<ResourceLocation, Long2ObjectMap<LongSet>>> registryEntry : blockStorage.object2ObjectEntrySet()) {
            CompoundTag blockTag = new CompoundTag();
            for (Object2ObjectMap.Entry<ResourceLocation, Long2ObjectMap<LongSet>> dimEntry : registryEntry.getValue().object2ObjectEntrySet()) {
                CompoundTag dimTag = new CompoundTag();
                for (Long2ObjectMap.Entry<LongSet> chunkEntry : dimEntry.getValue().long2ObjectEntrySet()) {
                    ListTag posList = new ListTag();
                    for (long posLong : chunkEntry.getValue()) {
                        posList.add(LongTag.valueOf(posLong));
                    }
                    dimTag.put(Long.toString(chunkEntry.getLongKey()), posList);
                }

                blockTag.put(dimEntry.getKey().toString(), dimTag);
            }
            nbt.put(registryEntry.getKey().toString(), blockTag);
        }
        Korall.LOGGER.debug("TrackedBlockData saved: {}", blockStorage);
        return nbt;
    }

    public static BlockTracker get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BlockTracker::load, BlockTracker::new, DATA_NAME);
    }

    public void addBlock(ServerLevel level, BlockPos pos, ResourceLocation registryId) {
        ResourceLocation dim = level.dimension().location();
        long chunkKey = ChunkPos.asLong(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        if (blockStorage.computeIfAbsent(registryId, k -> new Object2ObjectOpenHashMap<>()).computeIfAbsent(dim, k -> new Long2ObjectOpenHashMap<>()).computeIfAbsent(chunkKey, k -> new LongOpenHashSet()).add(pos.asLong())) {
            setDirty();
        }
    }

    public void removeBlock(ServerLevel level, BlockPos pos, ResourceLocation registryId) {
        ResourceLocation dim = level.dimension().location();
        Object2ObjectMap<ResourceLocation, Long2ObjectMap<LongSet>> dimMap = blockStorage.get(registryId);
        if (dimMap == null) return;

        Long2ObjectMap<LongSet> chunkMap = dimMap.get(dim);
        if (chunkMap == null) return;

        long chunkKey = ChunkPos.asLong(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));

        LongSet posSet = chunkMap.get(chunkKey);
        if (posSet == null) return;

        if (posSet.remove(pos.asLong())) {
            if (posSet.isEmpty()) {
                chunkMap.remove(chunkKey);
                if (chunkMap.isEmpty()) {
                    dimMap.remove(dim);
                    if (dimMap.isEmpty()) {
                        blockStorage.remove(registryId);
                    }
                }
            }
            setDirty();
        }
    }

    public LongSet getBlocks(ServerLevel level, ChunkPos chunkPos, ResourceLocation blockId) {
        return LongSets.unmodifiable(blockStorage.getOrDefault(blockId, Object2ObjectMaps.emptyMap()).getOrDefault(level.dimension().location(), Long2ObjectMaps.emptyMap()).getOrDefault(chunkPos.toLong(), LongSets.emptySet()));
    }
}
